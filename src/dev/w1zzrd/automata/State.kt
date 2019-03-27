package dev.w1zzrd.automata

/**
 * A state in a finite automaton
 *
 * @param name The unique name of the state used to identify it
 * @param language The acceptable language for this state (used for mapping transitions)
 * @param isDeterministic Whether or not this state allows epsilon-transitions and/or multiple transition targets for an
 * input
 * @param acceptState Whether or not this state is a final (accept) state for an automaton
 */
class State<T>(
        val name: String,
        val language: Language<T>,
        val isDeterministic: Boolean,
        val acceptState: Boolean
){
    /**
     * A transition table for a given set of elements from the language
     */
    private val connective = HashMap<T, MutableList<State<T>>>()

    /**
     * Direct epsilon-transitions possible from this state
     */
    private val epsilon = ArrayList<State<T>>()

    /**
     * Declare that a given set of elements from the language result in a transition to the given states.
     *
     * @param verbs Elements that result in the transitions to the given states
     * @param state The states to transition to
     */
    fun addConnective(verbs: Array<T>, vararg state: State<T>) = verbs.forEach { addConnective(it, *state) }

    /**
     * Declare that a given element from the language results in a transition to the given states.
     *
     * @param verb Element that results in the transition to the given states
     * @param state The states to transition to
     *
     * @exception IllegalArgumentException If a transition was declared that is characteristic of a nondeterministic
     * transition (i.e. more than one target was specified, or more than one target would be specified upon successful
     * declaration of this connective), but the state represented by this object is deterministic.
     *
     * @exception IllegalArgumentException If the given element is not a part of the declared language.
     */
    fun addConnective(verb: T, vararg state: State<T>){
        // Ensure nondeterministic behaviour is only possible for nondeterministic states
        if(isDeterministic && (state.size > 1 || connective[verb]?.contains(state[0]) == false))
            throw IllegalArgumentException("Deterministic states can only contain one-to-one connectives!")

        // Ensure element is indeed a part of the language
        if(language hasVerb verb){
            // Create the mapping if necessary, otherwise just add it the the mapped connective list
            if(connective[verb] == null) connective[verb] = mutableListOf(*state)
            else connective[verb]!!.addAll(state)
        }
        else throw IllegalArgumentException("Verb must be in language!")
    }

    /**
     * Declared an epsilon transition from this state to a given set of states.
     *
     * @exception IllegalStateException If the state represented by this object is deterministic
     */
    fun addEpsilon(vararg state: State<T>){
        if(isDeterministic)
            throw IllegalStateException("Epsilon-transitions are not possible in DFA models!")

        epsilon.addAll(state)
    }

    /**
     * "Simulate" a transition from this state for a given element from the language.
     *
     * @return The set of states that result from applying the element to this state (if any).
     */
    fun getConnective(verb: T) =
            ArrayList(connective[verb] ?: listOf<State<T>>())

    /**
     * Gets all immediate epsilon transitions from this state. This will NOT get all indirect epsilon transitions from
     * this state. I.e., state A has an epsilon transition to state B, and B has an epsilon transition to state C,
     * A.getEpsilon() will only return a set containing B, since C is only indirectly connected to a through epsilon
     * transitions.
     */
    fun getEpsilon() = ArrayList(epsilon)

    override fun equals(other: Any?) = other is State<*> && other.name == name
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }

    /**
     * Factory class for creating [State] object. Really just here for convenience.
     */
    companion object {
        fun <T> make(name: String, acceptState: Boolean, language: Language<T>, deterministic: Boolean) =
                State(name, language, deterministic, acceptState)
    }
}