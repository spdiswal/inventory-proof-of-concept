package dk.spdiswal.inventory.infrastructure.messaging

interface PublicationIdGenerator {
    fun generate(): PublicationId
}
