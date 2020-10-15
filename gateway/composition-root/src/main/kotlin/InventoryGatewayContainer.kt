package dk.spdiswal.inventory.gateway.composition

import dk.spdiswal.inventory.gateway.api.InventoryGatewayItemCacheService
import dk.spdiswal.inventory.gateway.api.InventoryGatewayMessageMapper
import dk.spdiswal.inventory.infrastructure.messaging.BufferedBroadcastChannelMessenger
import dk.spdiswal.inventory.infrastructure.messaging.Messenger
import dk.spdiswal.inventory.infrastructure.messaging.PublicationIdGenerator
import dk.spdiswal.inventory.infrastructure.messaging.RandomUuidPublicationIdGenerator
import dk.spdiswal.inventory.item.adapters.InMemoryInventoryStorage
import dk.spdiswal.inventory.item.service.InventoryStorage
import dk.spdiswal.inventory.item.service.ItemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val container = DI {
    bind<PublicationIdGenerator>() with singleton { RandomUuidPublicationIdGenerator }
    bind<Messenger>() with singleton { BufferedBroadcastChannelMessenger(instance()) }
    
    bind<InventoryGatewayItemCacheService>() with singleton { InventoryGatewayItemCacheService(instance()) }
    bind<InventoryGatewayMessageMapper>() with singleton { InventoryGatewayMessageMapper(instance()) }
    
    bind<InventoryStorage>() with singleton { InMemoryInventoryStorage(emptySet()) }
    
    bind<ItemService>() with singleton { ItemService(instance(), instance()) }
}

fun CoroutineScope.startLocalServices() {
    launch {
        val itemService: ItemService by container.instance()
        itemService.start()
    }
    
    launch {
        val itemCacheService: InventoryGatewayItemCacheService by container.instance()
        itemCacheService.start()
    }
}
