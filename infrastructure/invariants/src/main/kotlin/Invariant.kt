package dk.spdiswal.inventory.infrastructure.invariants

interface Contract

interface Precondition : Contract

abstract class Invariant<Subject>(private val predicate: Subject.() -> Boolean) : Contract {
    fun isSatisfiedBy(subject: Subject): Boolean = predicate(subject)
}

fun <Subject> Subject.validate(vararg invariants: Invariant<Subject>): Validated<Subject> {
    val violatedInvariants = invariants.filterNot { it.isSatisfiedBy(this) }.toSet()
    
    return when {
        violatedInvariants.isNotEmpty() -> Invalid(violatedInvariants)
        else                            -> Valid(this)
    }
}
