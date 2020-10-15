package dk.spdiswal.inventory.gateway.api

import dk.spdiswal.inventory.infrastructure.messaging.BufferedBroadcastChannelMessenger
import dk.spdiswal.inventory.infrastructure.messaging.Publication
import dk.spdiswal.inventory.infrastructure.messaging.PublicationId
import dk.spdiswal.inventory.infrastructure.messaging.subscribe
import dk.spdiswal.inventory.item.api.AdjustQuantity
import dk.spdiswal.inventory.item.api.ItemAdded
import dk.spdiswal.inventory.item.api.ItemRemoved
import dk.spdiswal.inventory.item.api.QuantityAdjusted
import dk.spdiswal.inventory.item.api.RemoveItem
import dk.spdiswal.inventory.item.api.SubmitItem
import io.kotest.core.spec.IsolationMode.InstancePerLeaf
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCaseConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import kotlin.time.seconds

class InventoryGatewayMessageMapperSpecs : FreeSpec({
    defaultTestConfig = TestCaseConfig(timeout = 1.seconds)
    isolationMode = InstancePerLeaf
    
    val messenger = PublishDelayMessenger(
        BufferedBroadcastChannelMessenger(
            IncrementalPublicationIdGenerator()
        )
    )
    
    val subject = InventoryGatewayMessageMapper(messenger)
    
    "on publishing 'enumerate-items' messages" - {
        val subscriber = async {
            messenger.subscribe<EnumerateItems>().take(2).toList()
        }
        
        subject.publish(EnumerateItemsDto)
        subject.publish(EnumerateItemsDto)
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).isEqualTo(
                listOf(
                    Publication(PublicationId("1"), message = EnumerateItems),
                    Publication(PublicationId("2"), message = EnumerateItems),
                )
            )
        }
    }
    
    "on publishing 'submit-item' messages" - {
        val subscriber = async {
            messenger.subscribe<SubmitItem>().take(2).toList()
        }
        
        subject.publish(SubmitItemDto(item = "apple", quantity = 2))
        subject.publish(SubmitItemDto(item = "orange", quantity = 4))
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).containsExactly(
                Publication(PublicationId("1"), message = SubmitItem(
                    itemName = "apple",
                    initialQuantity = 2,
                )),
                Publication(PublicationId("2"), message = SubmitItem(
                    itemName = "orange",
                    initialQuantity = 4,
                )),
            )
        }
    }
    
    "on publishing 'remove-item' messages" - {
        val subscriber = async {
            messenger.subscribe<RemoveItem>().take(2).toList()
        }
        
        subject.publish(RemoveItemDto(item = "apple"))
        subject.publish(RemoveItemDto(item = "orange"))
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).containsExactly(
                Publication(PublicationId("1"), message = RemoveItem(
                    itemName = "apple",
                )),
                Publication(PublicationId("2"), message = RemoveItem(
                    itemName = "orange",
                )),
            )
        }
    }
    
    "on publishing 'adjust-quantity' messages" - {
        val subscriber = async {
            messenger.subscribe<AdjustQuantity>().take(2).toList()
        }
        
        subject.publish(AdjustQuantityDto(item = "apple", quantity = 10))
        subject.publish(AdjustQuantityDto(item = "orange", quantity = 8))
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).containsExactly(
                Publication(PublicationId("1"), message = AdjustQuantity(
                    itemName = "apple",
                    adjustedQuantity = 10,
                )),
                Publication(PublicationId("2"), message = AdjustQuantity(
                    itemName = "orange",
                    adjustedQuantity = 8,
                )),
            )
        }
    }
    
    "on subscribing to 'items-enumerated' messages" - {
        val subscriber = async {
            subject.subscribeToEvents().take(2).toList()
        }
        
        messenger.publish(ItemsEnumerated(setOf(
            EnumeratedItem(item = "apple", quantity = 5),
            EnumeratedItem(item = "orange", quantity = 3),
        )))
        messenger.publish(ItemsEnumerated(setOf(
            EnumeratedItem(item = "apple", quantity = 6),
            EnumeratedItem(item = "orange", quantity = 2),
        )))
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).containsExactly(
                InventoryGatewayEventDto("1", ItemsEnumeratedDto(setOf(
                    EnumeratedItemDto(item = "apple", quantity = 5),
                    EnumeratedItemDto(item = "orange", quantity = 3),
                ))),
                InventoryGatewayEventDto("2", ItemsEnumeratedDto(setOf(
                    EnumeratedItemDto(item = "apple", quantity = 6),
                    EnumeratedItemDto(item = "orange", quantity = 2),
                ))),
            )
        }
    }
    
    "on subscribing to 'item-added' messages" - {
        val subscriber = async {
            subject.subscribeToEvents().take(2).toList()
        }
        
        messenger.publish(ItemAdded(itemName = "apple", initialQuantity = 4))
        messenger.publish(ItemAdded(itemName = "orange", initialQuantity = 7))
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).containsExactly(
                InventoryGatewayEventDto("1", ItemAddedDto(item = "apple", quantity = 4)),
                InventoryGatewayEventDto("2", ItemAddedDto(item = "orange", quantity = 7)),
            )
        }
    }
    
    "on subscribing to 'item-removed' messages" - {
        val subscriber = async {
            subject.subscribeToEvents().take(2).toList()
        }
        
        messenger.publish(ItemRemoved(itemName = "apple"))
        messenger.publish(ItemRemoved(itemName = "orange"))
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).containsExactly(
                InventoryGatewayEventDto("1", ItemRemovedDto(item = "apple")),
                InventoryGatewayEventDto("2", ItemRemovedDto(item = "orange")),
            )
        }
    }
    
    "on subscribing to 'quantity-adjusted' messages" - {
        val subscriber = async {
            subject.subscribeToEvents().take(2).toList()
        }
        
        messenger.publish(QuantityAdjusted(itemName = "apple", adjustedQuantity = 1))
        messenger.publish(QuantityAdjusted(itemName = "orange", adjustedQuantity = 6))
        
        "it notifies the subscriber of both messages 1..2" {
            val messages = subscriber.await()
            
            expectThat(messages).containsExactly(
                InventoryGatewayEventDto("1", QuantityAdjustedDto(item = "apple", quantity = 1)),
                InventoryGatewayEventDto("2", QuantityAdjustedDto(item = "orange", quantity = 6)),
            )
        }
    }
})
