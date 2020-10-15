package dk.spdiswal.inventory.gateway.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class InventoryGatewayCommandDto

@Serializable
@SerialName("enumerate-items")
object EnumerateItemsDto : InventoryGatewayCommandDto()

@Serializable
@SerialName("submit-item")
data class SubmitItemDto(
    val item: String,
    val quantity: Int,
) : InventoryGatewayCommandDto()

@Serializable
@SerialName("remove-item")
data class RemoveItemDto(
    val item: String,
) : InventoryGatewayCommandDto()

@Serializable
@SerialName("adjust-quantity")
data class AdjustQuantityDto(
    val item: String,
    val quantity: Int,
) : InventoryGatewayCommandDto()
