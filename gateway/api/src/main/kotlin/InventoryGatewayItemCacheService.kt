package dk.spdiswal.inventory.gateway.api

import dk.spdiswal.inventory.infrastructure.messaging.Messenger
import dk.spdiswal.inventory.infrastructure.messaging.Publication
import dk.spdiswal.inventory.infrastructure.messaging.subscribe
import dk.spdiswal.inventory.item.api.ItemAdded
import dk.spdiswal.inventory.item.api.ItemRemoved
import dk.spdiswal.inventory.item.api.QuantityAdjusted
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InventoryGatewayItemCacheService(
    private val messenger: Messenger,
) {
    private val quantitiesByItemName = mutableMapOf<String, Int>()
    
    suspend fun start() {
        coroutineScope {
            launch {
                messenger.subscribe<EnumerateItems>().collect { enumerateItems() }
            }
            launch {
                messenger.subscribe<ItemAdded>().collect(::onItemAdded)
            }
            launch {
                messenger.subscribe<ItemRemoved>().collect(::onItemRemoved)
            }
            launch {
                messenger.subscribe<QuantityAdjusted>().collect(::onQuantityAdjusted)
            }
        }
    }
    
    private suspend fun enumerateItems() {
        val items = quantitiesByItemName.map { (item, quantity) ->
            EnumeratedItem(item, quantity)
        }.toSet()
        
        messenger.publish(ItemsEnumerated(items))
    }
    
    private fun onItemAdded(publication: Publication<ItemAdded>) {
        val (itemName, initialQuantity) = publication.message
        quantitiesByItemName[itemName] = initialQuantity
    }
    
    private fun onItemRemoved(publication: Publication<ItemRemoved>) {
        val itemName = publication.message.itemName
        quantitiesByItemName -= itemName
    }
    
    private fun onQuantityAdjusted(publication: Publication<QuantityAdjusted>) {
        val (itemName, adjustedQuantity) = publication.message
        quantitiesByItemName[itemName] = adjustedQuantity
    }
}
