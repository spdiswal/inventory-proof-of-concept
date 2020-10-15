package dk.spdiswal.inventory.item.service

import dk.spdiswal.inventory.infrastructure.invariants.Invalid
import dk.spdiswal.inventory.infrastructure.invariants.Valid
import dk.spdiswal.inventory.infrastructure.invariants.Validated
import dk.spdiswal.inventory.infrastructure.invariants.mergeValidated
import dk.spdiswal.inventory.infrastructure.messaging.Messenger
import dk.spdiswal.inventory.infrastructure.messaging.Publication
import dk.spdiswal.inventory.infrastructure.messaging.subscribe
import dk.spdiswal.inventory.item.api.AdjustQuantity
import dk.spdiswal.inventory.item.api.ItemAdded
import dk.spdiswal.inventory.item.api.ItemRemovalRejected
import dk.spdiswal.inventory.item.api.ItemRemoved
import dk.spdiswal.inventory.item.api.ItemSubmissionRejected
import dk.spdiswal.inventory.item.api.QuantityAdjusted
import dk.spdiswal.inventory.item.api.QuantityAdjustmentRejected
import dk.spdiswal.inventory.item.api.RemoveItem
import dk.spdiswal.inventory.item.api.SubmitItem
import dk.spdiswal.inventory.item.model.Inventory
import dk.spdiswal.inventory.item.model.Item
import dk.spdiswal.inventory.item.model.ItemName
import dk.spdiswal.inventory.item.model.Quantity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ItemService(
    private val messenger: Messenger,
    private val storage: InventoryStorage,
) {
    suspend fun start() {
        coroutineScope {
            launch {
                messenger.subscribe<SubmitItem>().collect(::submitItem)
            }
            launch {
                messenger.subscribe<RemoveItem>().collect(::removeItem)
            }
            launch {
                messenger.subscribe<AdjustQuantity>().collect(::adjustQuantity)
            }
        }
    }
    
    private suspend fun submitItem(publication: Publication<SubmitItem>) {
        suspend fun <Subject> reject(invalid: Invalid<Subject>) {
            messenger.publish(ItemSubmissionRejected(
                inReplyTo = publication.id,
                reason = "Violated invariants: " + invalid.violatedContracts
            ))
        }
    
        val (itemName, initialQuantity) = publication.message
        
        val itemToAdd = mergeValidated(
            ItemName.of(itemName),
            Quantity.of(initialQuantity)
        ) { name, quantity ->
            Item(name, quantity)
        }
        
        if (itemToAdd !is Valid) {
            return reject(itemToAdd as Invalid)
        }
        
        editInventory(
            { addItem(itemToAdd.subject) },
            { messenger.publish(ItemAdded(itemName, initialQuantity)) },
            { reject(it) },
        )
    }
    
    private suspend fun removeItem(publication: Publication<RemoveItem>) {
        suspend fun <Subject> reject(invalid: Invalid<Subject>) {
            messenger.publish(ItemRemovalRejected(
                inReplyTo = publication.id,
                reason = "Violated invariants: " + invalid.violatedContracts
            ))
        }
    
        val (itemName) = publication.message
        val validatedItemName = ItemName.of(itemName)
        
        if (validatedItemName !is Valid) {
            return reject(validatedItemName as Invalid)
        }
        
        editInventory(
            { removeItem(validatedItemName.subject) },
            { messenger.publish(ItemRemoved(itemName)) },
            { reject(it) },
        )
    }
    
    private suspend fun adjustQuantity(publication: Publication<AdjustQuantity>) {
        suspend fun <Subject> reject(invalid: Invalid<Subject>) {
            messenger.publish(QuantityAdjustmentRejected(
                inReplyTo = publication.id,
                reason = "Violated invariants: " + invalid.violatedContracts
            ))
        }
    
        val (itemName, adjustedQuantity) = publication.message
        
        val itemToAdjust = mergeValidated(
            ItemName.of(itemName),
            Quantity.of(adjustedQuantity)
        ) { name, quantity ->
            Item(name, quantity)
        }
        
        if (itemToAdjust !is Valid) {
            return reject(itemToAdjust as Invalid)
        }
        
        editInventory(
            { adjustItem(itemToAdjust.subject) },
            { messenger.publish(QuantityAdjusted(itemName, adjustedQuantity)) },
            { reject(it) },
        )
    }
    
    private suspend fun editInventory(
        transformer: Inventory.() -> Validated<Inventory>,
        succeed: suspend (Inventory) -> Unit,
        reject: suspend (Invalid<Inventory>) -> Unit,
    ) {
        val inventory = storage.get()
        val validatedInventory = transformer(inventory)
        
        if (validatedInventory !is Valid) {
            return reject(validatedInventory as Invalid)
        }
        
        val editedInventory = validatedInventory.subject
        
        storage.set(editedInventory)
        succeed(editedInventory)
    }
}
