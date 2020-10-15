package dk.spdiswal.inventory.item.adapters

import dk.spdiswal.inventory.infrastructure.invariants.Invalid
import dk.spdiswal.inventory.infrastructure.invariants.Valid
import dk.spdiswal.inventory.item.model.Inventory
import dk.spdiswal.inventory.item.model.Item
import dk.spdiswal.inventory.item.service.InventoryStorage

class InMemoryInventoryStorage(initialItems: Set<Item>) : InventoryStorage {
    private var inventory: Inventory
    
    init {
        val validatedInventory = Inventory.of(initialItems)
        
        if (validatedInventory is Invalid) {
            throw IllegalArgumentException("Violated invariants: " + validatedInventory.violatedContracts)
        }
        
        inventory = (validatedInventory as Valid).subject
    }
    
    override suspend fun get() = inventory
    
    override suspend fun set(inventory: Inventory) {
        this.inventory = inventory
    }
}
