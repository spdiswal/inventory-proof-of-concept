package dk.spdiswal.inventory.gateway.api

import dk.spdiswal.inventory.infrastructure.messaging.PublicationId
import dk.spdiswal.inventory.infrastructure.messaging.PublicationIdGenerator
import java.util.concurrent.atomic.AtomicInteger

/**
 * Generates [PublicationId]s with numerically incrementing keys.
 */
class IncrementalPublicationIdGenerator : PublicationIdGenerator {
    private val counter = AtomicInteger()
    
    override fun generate() = PublicationId(counter.incrementAndGet().toString())
}
