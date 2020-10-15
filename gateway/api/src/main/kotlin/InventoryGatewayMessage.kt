package dk.spdiswal.inventory.gateway.api

import dk.spdiswal.inventory.infrastructure.messaging.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("enumerate-items")
object EnumerateItems : Message

@Serializable
@SerialName("items-enumerated")
data class ItemsEnumerated(val items: Set<EnumeratedItem>) : Message

@Serializable
data class EnumeratedItem(
    val item: String,
    val quantity: Int,
)
