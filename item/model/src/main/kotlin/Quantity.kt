package dk.spdiswal.inventory.item.model

import dk.spdiswal.inventory.infrastructure.invariants.Invariant
import dk.spdiswal.inventory.infrastructure.invariants.validate

class Quantity private constructor(private val amount: Int) : Comparable<Quantity> {
    private object AmountIsNonNegative : Invariant<Quantity>({ amount >= 0 })
    
    companion object {
        fun of(amount: Int) = Quantity(amount).validate(
            AmountIsNonNegative,
        )
    }
    
    operator fun compareTo(otherAmount: Int) = amount - otherAmount
    
    override fun compareTo(other: Quantity) = compareTo(other.amount)
    
    override fun equals(other: Any?) = when {
        this === other                -> true
        javaClass != other?.javaClass -> false
        else                          -> {
            other as Quantity
            amount == other.amount
        }
    }
    
    override fun hashCode() = amount
    
    override fun toString() = amount.toString()
}
