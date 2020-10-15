package dk.spdiswal.inventory.infrastructure.messaging

import kotlinx.serialization.Serializable

@Serializable
data class Publication<out Topic : Message>(
    val id: PublicationId,
    val message: Topic,
)

@Serializable
data class PublicationId(val key: String) {
    init {
        if (key.isBlank()) {
            throw IllegalArgumentException("The publication id must be non-blank")
        }
    }
    
    override fun toString() = key
}

interface Message
