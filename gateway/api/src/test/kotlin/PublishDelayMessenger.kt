package dk.spdiswal.inventory.gateway.api

import dk.spdiswal.inventory.infrastructure.messaging.Message
import dk.spdiswal.inventory.infrastructure.messaging.Messenger
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.milliseconds

/**
 * A [Messenger] decorator that introduces an artificial delay before publishing
 * messages. This gives unit tests a chance to launch subscribers in separate
 * coroutines and have them ready in time to receive the message.
 */
class PublishDelayMessenger(
    private val delegate: Messenger,
    private val publishDelayDuration: Duration = 20.milliseconds,
) : Messenger by delegate {
    
    override suspend fun publish(message: Message) {
        delay(publishDelayDuration)
        delegate.publish(message)
    }
}
