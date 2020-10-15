package dk.spdiswal.inventory.gateway.application

import dk.spdiswal.inventory.gateway.api.InventoryGatewayMessageMapper
import dk.spdiswal.inventory.gateway.composition.container
import dk.spdiswal.inventory.gateway.composition.startLocalServices
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.maxAgeDuration
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Text
import io.ktor.http.ContentType.Text.EventStream
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.Accept
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Head
import io.ktor.http.HttpMethod.Companion.Options
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.content.files
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.response.respondText
import io.ktor.routing.accept
import io.ktor.routing.contentType
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import org.kodein.di.instance
import org.slf4j.event.Level.INFO
import java.io.File
import kotlin.time.days
import kotlin.time.minutes
import kotlin.time.seconds

fun Application.module() {
    startLocalServices()
    
    val messageMapper: InventoryGatewayMessageMapper by container.instance()
    val httpResponder = InventoryGatewayKtorHttpResponder(messageMapper)
    
    install(CallLogging) { level = INFO }
    
    routing {
        static {
            resource("/", "dist/index.html")
            resources("dist")
        }
        
        get("/log") {
            //language=HTML
            call.respondText(
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Inventory - Message Log</title>
                </head>
                <body>
                    <ul id="events">
                    </ul>
                    <script type="text/javascript">
                        const source = new EventSource("/messages")
                        const eventsUl = document.getElementById("events")

                        window.onbeforeunload = function() {
                          source.close()
                        }

                        function logEvent(label, event) {
                            const payload = !!event ? JSON.parse(event.data).payload : {}
                            const li = document.createElement("li")
                            li.innerText = label + ": " + JSON.stringify(payload)
                            eventsUl.appendChild(li)
                        }

                        source.addEventListener("items-enumerated", function (e) {
                            logEvent("Items enumerated", e)
                        })

                        source.addEventListener("item-added", function (e) {
                            logEvent("Item added", e)
                        })

                        source.addEventListener("item-removed", function (e) {
                            logEvent("Item removed", e)
                        })

                        source.addEventListener("quantity-adjusted", function (e) {
                            logEvent("Quantity adjusted", e)
                        })

                        source.addEventListener("open", function (e) {
                            logEvent("open")
                        })
                        
                        source.addEventListener("error", function (e) {
                            if (source.readyState === EventSource.CLOSED) {
                                logEvent("closed")
                            } else {
                                logEvent("error")
                                console.log(e)
                            }
                        })
                    </script>
                </body>
                </html>
            """.trimIndent(),
                contentType = Text.Html
            )
        }
        
        route("/messages") {
            accept(EventStream) {
                get {
                    httpResponder.getMessages(call)
                }
            }
            
            contentType(Json) {
                post {
                    httpResponder.postMessage(call)
                }
            }
        }
    }
}
