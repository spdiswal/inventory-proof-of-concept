package dk.spdiswal.inventory.gateway.application

import dk.spdiswal.inventory.gateway.api.InventoryGatewayCommandDto
import dk.spdiswal.inventory.gateway.api.InventoryGatewayEventDto
import dk.spdiswal.inventory.gateway.api.InventoryGatewayMessageMapper
import dk.spdiswal.inventory.infrastructure.sse.DataEventStreamMessage
import dk.spdiswal.inventory.infrastructure.sse.respondEventStream
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.util.error
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class InventoryGatewayKtorHttpResponder(
    private val messageMapper: InventoryGatewayMessageMapper,
) {
    private val topicDiscriminatorProperty = "topic"
    
    private val json = Json {
        classDiscriminator = topicDiscriminatorProperty
    }
    
    suspend fun getMessages(call: ApplicationCall) {
        fun InventoryGatewayEventDto.toEventStreamMessage(): DataEventStreamMessage {
            val jsonElement = json.encodeToJsonElement(this)
            val topic = jsonElement.jsonObject["payload"]!!
                .jsonObject[topicDiscriminatorProperty]!!
                .jsonPrimitive
                .content
            
            return DataEventStreamMessage(
                id = messageId,
                event = topic,
                data = jsonElement.toString()
            )
        }
        
        try {
            val events = messageMapper.subscribeToEvents().map {
                it.toEventStreamMessage()
            }
            
            call.respondEventStream(events)
        } catch (e: SerializationException) {
            call.application.environment.log.error(e)
            return call.respond(InternalServerError, "Failed to serialise the event stream: " + e.message)
        }
    }
    
    suspend fun postMessage(call: ApplicationCall) {
        try {
            val commandDto: InventoryGatewayCommandDto = json.decodeFromString(
                InventoryGatewayCommandDto.serializer(),
                call.receiveText()
            )
            
            messageMapper.publish(commandDto)
            call.respond(NoContent)
            
        } catch (e: SerializationException) {
            return call.respond(BadRequest, "Invalid message structure: " + e.message)
        }
    }
}
