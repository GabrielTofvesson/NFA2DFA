package dev.w1zzrd.automata

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

    fun addConnective(verbs: Array<T>, vararg state: State<T>) = verbs.forEach { addConnective(it, *state) }
    fun addConnective(verb: T, vararg state: State<T>){
        if(isDeterministic && (state.size > 1 || connective[verb]?.contains(state[0]) == false))
            throw IllegalArgumentException("Deterministic states can only contain one-to-one connectives!")

        if(language hasVerb verb){
            if(connective[verb] == null) connective[verb] = mutableListOf(*state)
            else connective[verb]!!.addAll(state)
        }
        else throw IllegalArgumentException("Verb must be in language!")
    }

    fun addEpsilon(vararg state: State<T>){
        if(isDeterministic)
            throw IllegalStateException("Epsilon-transitions are not possible in DFA models!")

        epsilon.addAll(state)
    }

    fun getConnective(verb: T) =
            ArrayList(connective[verb] ?: listOf<State<T>>())

    fun getEpsilon() = ArrayList(epsilon)

    override fun equals(other: Any?) = other is State<*> && other.name == name
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }
}

class StateFactory<T>(val language: Language<T>, val deterministic: Boolean){
    fun make(name: String, acceptState: Boolean) = State(name, language, deterministic, acceptState)
}