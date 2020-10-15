package dk.spdiswal.inventory.infrastructure.messaging

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

class BufferedBroadcastChannelMessenger(
    private val idGenerator: PublicationIdGenerator,
) : Messenger {
    
    override fun <Topic : Message> subscribe(topicClass: KClass<Topic>) =
        mainChannel.asFlow()
            .filter { it.message::class == topicClass }
            .map {
                @Suppress("UNCHECKED_CAST")
                it as Publication<Topic>
            }
            .cancellable()
    
    override suspend fun publish(message: Message) =
        mainChannel.send(Publication(id = idGenerator.generate(), message))
    
    private val mainChannel =
        BroadcastChannel<Publication<*>>(BUFFERED)
}
