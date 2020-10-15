package dk.spdiswal.inventory.infrastructure.sse

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCaseConfig
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.time.milliseconds

class RespondEventStreamSpecs : FreeSpec({
    val messageInterval = 250.milliseconds
    val commentMessageInterval = 700.milliseconds
    
    defaultTestConfig = TestCaseConfig(timeout = messageInterval * 18)
    
    val serverPort = 58382
    
    fun Application.subject() {
        routing {
            get("/event-stream") {
                val events = flowOf<EventStreamMessage>(
                    DataEventStreamMessage(id = "250ms", event = "apples-delivered", data = "Ambrosia"),
                    DataEventStreamMessage(id = "500ms", event = "apples-delivered", data = "Belle de Boskoop"),
                    // (700 ms) comment message
                    DataEventStreamMessage(event = "intermission-observed", data = "First line\nSecond line\n\nFourth line"),
                    DataEventStreamMessage(id = "1000ms", event = "apples-delivered", data = "Crimson Delight"),
                    DataEventStreamMessage(data = "Loose message!"),
                    // (1400 ms) comment message
                    DataEventStreamMessage(id = "1500ms", event = "plums-delivered", data = "Damson"),
                    DataEventStreamMessage(id = "1750ms", event = "apples-delivered", data = "Golden Delicious"),
                    DataEventStreamMessage(id = "2000ms", event = "plums-delivered", data = "Mirabelle"),
                    /* (2100 ms) comment message */
                    DataEventStreamMessage(event = "intermission-observed", data = "\n\nThird line\n\nFifth line\n"),
                    DataEventStreamMessage(id = "2500ms", event = "apples-delivered", data = "Royal Gala"),
                    DataEventStreamMessage(id = "2750ms", data = "Another loose message!"),
                    /* (2800 ms) comment message */
                    DataEventStreamMessage(id = "3000ms", event = "apples-delivered", data = "Spartan"),
                    DataEventStreamMessage(id = "3250ms", data = "Not observed"),
                    DataEventStreamMessage(id = "3500ms", data = "Not observed"),
                    /* (3500 ms) comment message not observed */
                    DataEventStreamMessage(id = "3750ms", data = "Not observed"),
                ).onEach { delay(messageInterval) }
                
                call.respondEventStream(events, commentMessageInterval)
            }
        }
    }
    
    val httpServer = embeddedServer(Netty, serverPort, module = Application::subject).start(wait = false)
    val httpClient = HttpClient(CIO)
    
    "when requesting an endpoint of an event stream" - {
        val (body, headers) = httpClient.get<HttpStatement>("http://localhost:$serverPort/event-stream/").execute { response ->
            val requestDuration = 100.milliseconds
            delay(messageInterval * 12 - requestDuration)
            
            val body = ByteArray(response.content.availableForRead)
                .apply { response.content.readAvailable(this) }
                .decodeToString()
            
            body to response.headers
        }
        
        "the 'Cache-Control' response header is 'no-cache'" {
            expectThat(headers["Cache-Control"]).isEqualTo("no-cache")
        }
        
        "the 'Content-Type' response header is 'text/event-stream'" {
            expectThat(headers["Content-Type"]).isEqualTo("text/event-stream")
        }
        
        "the response body is a stream of messages" {
            expectThat(body).isEqualTo("""
                |id:250ms
                |event:apples-delivered
                |data:Ambrosia
                |
                |id:500ms
                |event:apples-delivered
                |data:Belle de Boskoop
                |
                |:
                |event:intermission-observed
                |data:First line
                |data:Second line
                |data:
                |data:Fourth line
                |
                |id:1000ms
                |event:apples-delivered
                |data:Crimson Delight
                |
                |data:Loose message!
                |
                |:
                |id:1500ms
                |event:plums-delivered
                |data:Damson
                |
                |id:1750ms
                |event:apples-delivered
                |data:Golden Delicious
                |
                |id:2000ms
                |event:plums-delivered
                |data:Mirabelle
                |
                |:
                |event:intermission-observed
                |data:
                |data:
                |data:Third line
                |data:
                |data:Fifth line
                |data:
                |
                |id:2500ms
                |event:apples-delivered
                |data:Royal Gala
                |
                |id:2750ms
                |data:Another loose message!
                |
                |:
                |id:3000ms
                |event:apples-delivered
                |data:Spartan
                |
                |""".trimMargin()
            )
        }
    }
    
    afterSpec {
        httpClient.close()
        httpServer.stop(gracePeriodMillis = 50, timeoutMillis = 50)
    }
})
