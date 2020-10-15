package dk.spdiswal.inventory.gateway.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class InventoryGatewayEventDto(
    val messageId: String,
    val payload: InventoryGatewayEventPayloadDto
)

@Serializable
sealed class InventoryGatewayEventPayloadDto

@Serializable
@SerialName("items-enumerated")
data class ItemsEnumeratedDto(
    val items: Set<EnumeratedItemDto>
) : InventoryGatewayEventPayloadDto()

@Serializable
data class EnumeratedItemDto(
    val item: String,
    val quantity: Int
)

@Serializable
@SerialName("item-added")
data class ItemAddedDto(
    val item: String,
    val quantity: Int
) : InventoryGatewayEventPayloadDto()

@Serializable
@SerialName("item-removed")
data class ItemRemovedDto(
    val item: String
) : InventoryGatewayEventPayloadDto()

@Serializable
@SerialName("quantity-adjusted")
data class QuantityAdjustedDto(
    val item: String,
    val quantity: Int
) : InventoryGatewayEventPayloadDto()
