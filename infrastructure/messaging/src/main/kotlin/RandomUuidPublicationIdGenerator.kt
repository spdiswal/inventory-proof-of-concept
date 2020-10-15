package dk.spdiswal.inventory.infrastructure.messaging

import java.util.UUID

/**
 * Generates [PublicationId]s with [pseudo random][java.util.UUID.randomUUID]
 * [UUID]s as keys.
 */
object RandomUuidPublicationIdGenerator : PublicationIdGenerator {
    override fun generate() = PublicationId(UUID.randomUUID().toString())
}
