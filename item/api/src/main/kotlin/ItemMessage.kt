package dk.spdiswal.inventory.item.api

import dk.spdiswal.inventory.infrastructure.messaging.Message
import dk.spdiswal.inventory.infrastructure.messaging.PublicationId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("submit-item")
data class SubmitItem(
    val itemName: String,
    val initialQuantity: Int,
) : Message

@Serializable
@SerialName("item-submission-rejected")
data class ItemSubmissionRejected(
    val inReplyTo: PublicationId,
    val reason: String,
) : Message

@Serializable
@SerialName("item-added")
data class ItemAdded(
    val itemName: String,
    val initialQuantity: Int,
) : Message

@Serializable
@SerialName("remove-item")
data class RemoveItem(
    val itemName: String,
) : Message

@Serializable
@SerialName("item-removal-rejected")
data class ItemRemovalRejected(
    val inReplyTo: PublicationId,
    val reason: String,
) : Message

@Serializable
@SerialName("item-removed")
data class ItemRemoved(
    val itemName: String,
) : Message

@Serializable
@SerialName("adjust-quantity")
data class AdjustQuantity(
    val itemName: String,
    val adjustedQuantity: Int,
) : Message

@Serializable
@SerialName("quantity-adjustment-rejected")
data class QuantityAdjustmentRejected(
    val inReplyTo: PublicationId,
    val reason: String,
) : Message

@Serializable
@SerialName("quantity-adjusted")
data class QuantityAdjusted(
    val itemName: String,
    val adjustedQuantity: Int,
) : Message
