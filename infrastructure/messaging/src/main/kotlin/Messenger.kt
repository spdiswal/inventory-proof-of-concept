package dk.spdiswal.inventory.infrastructure.messaging

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface Messenger {
    fun <Topic : Message> subscribe(topicClass: KClass<Topic>): Flow<Publication<Topic>>
    
    suspend fun publish(message: Message)
}

inline fun <reified Topic : Message> Messenger.subscribe(): Flow<Publication<Topic>> =
    subscribe(Topic::class)
