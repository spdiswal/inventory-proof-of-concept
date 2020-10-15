package dk.spdiswal.inventory.item.model

import dk.spdiswal.inventory.infrastructure.invariants.Invalid
import dk.spdiswal.inventory.infrastructure.invariants.Invariant
import dk.spdiswal.inventory.infrastructure.invariants.Precondition
import dk.spdiswal.inventory.infrastructure.invariants.Validated
import dk.spdiswal.inventory.infrastructure.invariants.validate

class Inventory private constructor(private val items: Set<Item>) {
    private object ItemsHaveUniqueNames : Invariant<Inventory>({
        items.distinctBy { it.name }.count() == items.count()
    })
    
    companion object {
        fun of(items: Set<Item>) = Inventory(items).validate(
            ItemsHaveUniqueNames,
        )
    }
    
    private object ItemMustBeNew : Precondition
    private object ItemMustExist : Precondition
    private object ItemMustHaveZeroQuantity : Precondition
    
    fun enumerateItems(): Set<Item> = items
    
    fun addItem(itemToAdd: Item): Validated<Inventory> {
        if (getItemByName(itemToAdd.name) != null) {
            return Invalid(ItemMustBeNew)
        }
        
        return of(items + itemToAdd)
    }
    
    fun removeItem(itemToRemove: ItemName): Validated<Inventory> {
        val item = getItemByName(itemToRemove) ?: return Invalid(ItemMustExist)
        
        if (item.quantity > 0) {
            return Invalid(ItemMustHaveZeroQuantity)
        }
        
        return of(items - item)
    }
    
    fun adjustItem(itemToAdjust: Item): Validated<Inventory> {
        val item = getItemByName(itemToAdjust.name) ?: return Invalid(ItemMustExist)
        return of(items - item + itemToAdjust)
    }
    
    private fun getItemByName(itemName: ItemName) = items.firstOrNull { it.name == itemName }
    
    override fun equals(other: Any?) = when {
        this === other                -> true
        javaClass != other?.javaClass -> false
        else                          -> {
            other as Inventory
            items == other.items
        }
    }
    
    override fun hashCode() = items.hashCode()
    
    override fun toString() = "Inventory { $items }"
}
