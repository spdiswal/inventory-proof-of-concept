package dk.spdiswal.inventory.infrastructure.sse

import io.ktor.application.ApplicationCall
import io.ktor.http.CacheControl.NoCache
import io.ktor.http.ContentType.Text.EventStream
import io.ktor.response.cacheControl
import io.ktor.response.respondBytesWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration
import kotlin.time.seconds

suspend fun ApplicationCall.respondEventStream(
    events: Flow<EventStreamMessage>,
    commentMessageInterval: Duration = 30.seconds,
) {
    val comments = infiniteFlow { CommentEventStreamMessage }
        .onEach { delay(commentMessageInterval) }
    
    val eventStreamMessages = merge(events, comments)
    
    response.cacheControl(NoCache(null))
    
    respondBytesWriter(contentType = EventStream) {
        eventStreamMessages.collect { event -> event.writeTo(this) }
    }
}

private fun <Value> infiniteFlow(valueProducer: () -> Value) = flow {
    while (true) {
        emit(valueProducer())
    }
}
