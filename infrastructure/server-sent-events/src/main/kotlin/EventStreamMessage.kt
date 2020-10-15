package dk.spdiswal.inventory.infrastructure.sse

import io.ktor.util.cio.write
import io.ktor.utils.io.ByteWriteChannel

sealed class EventStreamMessage {
    internal abstract suspend fun writeTo(output: ByteWriteChannel)
}

data class DataEventStreamMessage(
    val id: String? = null,
    val event: String? = null,
    val data: String
) : EventStreamMessage() {
    
    override suspend fun writeTo(output: ByteWriteChannel) = with(output) {
        if (id != null) {
            write("id:$id\n")
        }
        if (event != null) {
            write("event:$event\n")
        }
        for (dataLine in data.lines()) {
            write("data:$dataLine\n")
        }
        write("\n")
        flush()
    }
}

internal object CommentEventStreamMessage : EventStreamMessage() {
    override suspend fun writeTo(output: ByteWriteChannel) = with(output) {
        write(":\n")
        flush()
    }
}
