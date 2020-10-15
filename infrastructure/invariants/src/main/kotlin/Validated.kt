package dk.spdiswal.inventory.infrastructure.invariants

sealed class Validated<Subject>

data class Valid<Subject>(val subject: Subject) : Validated<Subject>()

data class Invalid<Subject>(
    val violatedContracts: Set<Contract>,
) : Validated<Subject>() {
    constructor(violatedContract: Contract) : this(setOf(violatedContract))
}

fun <A, B, Merged> mergeValidated(a: Validated<A>, b: Validated<B>, merger: (A, B) -> Merged): Validated<Merged> {
    return if (a is Valid && b is Valid) {
        Valid(merger(a.subject, b.subject))
    } else {
        val invalid = setOf(a, b).filterIsInstance<Invalid<*>>()
        Invalid(invalid.flatMap { it.violatedContracts }.toSet())
    }
}
