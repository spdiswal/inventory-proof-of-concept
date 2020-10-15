package dk.spdiswal.inventory.item.model

import dk.spdiswal.inventory.infrastructure.invariants.Invariant
import dk.spdiswal.inventory.infrastructure.invariants.validate

class ItemName private constructor(private val name: String) : Comparable<ItemName> {
    companion object {
        fun of(name: String) = ItemName(name).validate(
            NameIsValid,
        )
    }
    
    private object NameIsValid : Invariant<ItemName>({
        name.matches(Regex("[-_A-Za-z0-9]+"))
    })
    
    override fun compareTo(other: ItemName) = name.compareTo(other.name)
    
    override fun equals(other: Any?) = when {
        this === other                -> true
        javaClass != other?.javaClass -> false
        else                          -> {
            other as ItemName
            name == other.name
        }
    }
    
    override fun hashCode() = name.hashCode()
    
    override fun toString() = name
}
