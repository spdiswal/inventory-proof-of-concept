package dk.spdiswal.inventory.item.service

import dk.spdiswal.inventory.item.model.Inventory

interface InventoryStorage {
    suspend fun get(): Inventory
    
    suspend fun set(inventory: Inventory)
}
