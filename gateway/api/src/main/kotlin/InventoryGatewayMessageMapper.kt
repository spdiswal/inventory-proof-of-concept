package dk.spdiswal.inventory.gateway.api

import dk.spdiswal.inventory.infrastructure.messaging.Messenger
import dk.spdiswal.inventory.infrastructure.messaging.Publication
import dk.spdiswal.inventory.infrastructure.messaging.subscribe
import dk.spdiswal.inventory.item.api.AdjustQuantity
import dk.spdiswal.inventory.item.api.ItemAdded
import dk.spdiswal.inventory.item.api.ItemRemoved
import dk.spdiswal.inventory.item.api.QuantityAdjusted
import dk.spdiswal.inventory.item.api.RemoveItem
import dk.spdiswal.inventory.item.api.SubmitItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class InventoryGatewayMessageMapper(
    private val messenger: Messenger,
) {
    fun subscribeToEvents(): Flow<InventoryGatewayEventDto> {
        fun Publication<ItemsEnumerated>.toEventDto() = InventoryGatewayEventDto(
            messageId = id.key,
            payload = ItemsEnumeratedDto(
                items = message.items.map {
                    EnumeratedItemDto(item = it.item, quantity = it.quantity)
                }.toSet()
            ))
        
        fun Publication<ItemAdded>.toEventDto() = InventoryGatewayEventDto(
            messageId = id.key,
            payload = ItemAddedDto(
                item = message.itemName,
                quantity = message.initialQuantity
            ))
        
        fun Publication<ItemRemoved>.toEventDto() = InventoryGatewayEventDto(
            messageId = id.key,
            payload = ItemRemovedDto(
                item = message.itemName
            ))
        
        fun Publication<QuantityAdjusted>.toEventDto() = InventoryGatewayEventDto(
            messageId = id.key,
            payload = QuantityAdjustedDto(
                item = message.itemName,
                quantity = message.adjustedQuantity
            ))
        
        return merge(
            messenger.subscribe<ItemsEnumerated>().map { it.toEventDto() },
            messenger.subscribe<ItemAdded>().map { it.toEventDto() },
            messenger.subscribe<ItemRemoved>().map { it.toEventDto() },
            messenger.subscribe<QuantityAdjusted>().map { it.toEventDto() },
        )
    }
    
    suspend fun publish(command: InventoryGatewayCommandDto) {
        val messageToPublish = when (command) {
            is EnumerateItemsDto -> EnumerateItems
            is SubmitItemDto -> SubmitItem(
                itemName = command.item,
                initialQuantity = command.quantity
            )
            is RemoveItemDto -> RemoveItem(
                itemName = command.item
            )
            is AdjustQuantityDto -> AdjustQuantity(
                itemName = command.item,
                adjustedQuantity = command.quantity
            )
        }
        
        messenger.publish(messageToPublish)
    }
}
