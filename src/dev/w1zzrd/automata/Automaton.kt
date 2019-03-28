package dev.w1zzrd.automata

/**
 * A finite automaton that accepts elements from the given language and
 * which is either deterministic or nondeterministic
 */
class Automaton<T>(val language: Language<T>, val deterministic: Boolean){
    /**
     * All states of the automaton
     */
    private val states = ArrayList<State<T>>()

    /**
     * Which state the automaton should start at.
     *
     * @exception IllegalArgumentException If there is an attempt to set the initial state to one that does not exist in
     * the set of possible states for this automaton.
     */
    var entryPoint: State<T>? = null
        set(value){
            if(!states.contains(value))
                throw IllegalArgumentException("State not valid for this automaton!")
            field = value
        }

    /**
     * Add states to the automaton (if, for example, they weren't automatically created with [makeState])
     * Note that nondeterministic states can only be added to nondeterministic automata. Deterministic states have
     * no similar or corresponding restrictions.
     *
     * @param states States to add to the automaton
     *
     * @exception IllegalArgumentException If a nondeterministic state was passed to a deterministic automaton.
     *
     * @see Automaton.makeState
     */
    fun addStates(vararg states: State<T>){
        states.forEach { state ->
            if(addState(state))
                language.elements.forEach { verb ->
                    addStates(*state.getConnective(verb).toTypedArray())
                }
        }
    }

    /**
     * Add a single state to the automaton (if, for example, it wasn't automatically created with [makeState]).
     * Note that a nondeterministic state can only be added to a nondeterministic automaton. Deterministic states have
     * no similar or corresponding restrictions.
     *
     * @param state State to add to the automaton
     *
     * @return True if the state was successfully added, else false.
     * @exception IllegalArgumentException If a nondeterministic state was passed to a deterministic automaton.
     *
     * @see Automaton.makeState
     */
    fun addState(state: State<T>): Boolean{
        if(deterministic && !state.isDeterministic)
            throw IllegalArgumentException("Deterministic automaton can only contain deterministic states!")

        if(!states.contains(state)){
            states.add(state)
            return true
        }
        return false
    }

    /**
     * Create a new state in this automaton. The generated state will be deterministic if the automaton represented by
     * this object is deterministic, otherwise it will be nondeterministic.
     *
     * @param name The [State.name] of the State. Note that this must be unique for this automaton
     * @param acceptState Whether or not the generated State should be an final/accept state of the automaton
     * @return The generated state.
     *
     * @see State.acceptState
     * @see State.name
     */
    fun makeState(name: String, acceptState: Boolean = false): State<T> {
        val state = State.make(name, acceptState, language, deterministic) // Create the state

        // Ensure that the state is successfully added to the automaton
        if(!addState(state))
            throw IllegalArgumentException("Duplicate state detected!")

        return state
    }

    /**
     * Checks whether the given input string will leave the automaton in an accept-state after processing the string.
     * This happens when, after the final element in the string is processed, the automaton is currently in an accept-
     * state - in the case of deterministic automata - or the set of current states contains an accept-state.
     *
     * @param string The string of values governing the state transitions in the automaton starting at the specified
     * initial state.
     *
     * @return True if the current state representation is (or contains) an accept state.
     *
     * @see Automaton.entryPoint
     * @see State.acceptState
     */
    fun accepts(vararg string: T): Boolean {
        if(!(language hasVerbs string))
            throw IllegalArgumentException("All verbs in string must be part of the language!")

        if(entryPoint == null) return false

        val traverser = StateTraverser(entryPoint!!)
        traverser.traverse(*string)

        return traverser.accepted
    }

    /**
     * Converts the automaton represented by this object into its equivalent DFA representation. If this object already
     * is a DFA (i.e. if [deterministic] is true), then no action is performed.
     *
     *
     *
     * An example of how this is done:
     *
     *   Assume we have the language {0, 1} and states 'a' and 'b'
     *
     *   Assume we start at 'a' and that 'b' is an accept state.
     *
     *   Assume 'a' transitions to 'a' or 'b' on input 0, and transitions to 'b' on input 1
     *
     *   Assume 'b' transitions to 'a' on input 0 and does nothing on input '1'
     *
     *   In this case, we can construct the following transition table:
     *
     * ..............|.0........|.1...
     *
     * ->.{a}....|.{a, b}.|.{b}
     *
     * F..{a, b}.|.{a, b}.|.{b}
     *
     * F..{b}.....|.{a}.....|.∅
     *
     * The keys (leftmost table elements) can then be used as the names of the new states in the DFA representation, and
     * the states in the corresponding table entries represent the connectives for the DFA. In the case of the empty set
     * (∅), this must become its own state that has no outgoing connectives.
     *
     *
     * @param printTable Whether or not to print the generated NFA state-input mappings to STDOUT
     *
     * @return A deterministic automaton which processes the same language as the automaton represented by this object.
     * When the object called already is deterministic, it simply returns itself.
     *
     * @exception IllegalStateException If [entryPoint] is null, as no DFA can be generated if there is no entry point.
     *
     * @see Automaton.deterministic
     * @see Automaton.entryPoint
     */
    fun toDeterministicAutomaton(printTable: Boolean = false): Automaton<T> {
        if(deterministic) return this
        if(entryPoint == null)
            throw IllegalStateException("Entry point state must be defined!")

        // Maps a state-collection to the results of applying all values in the language to it (individually)
        val tableEntries = HashMap<MutableList<State<T>>, HashMap<T, MutableList<State<T>>>>()

        // Check if a table entry is completely populated
        fun HashMap<T, MutableList<State<T>>>.getUnpopulatedMapping() = language.elements.firstOrNull { !keys.contains(it) }
        fun HashMap<MutableList<State<T>>, HashMap<T, MutableList<State<T>>>>.findUnpopulatedMapping(): Pair<MutableList<State<T>>, T>? {
            var find: T? = null
            val result = keys.firstOrNull {
                find = this[it]!!.getUnpopulatedMapping()
                find != null
            }
            return if(result == null) null else result to find!!
        }

        // Simple function for converting states to a corresponding string
        // The corresponding string will describe a set of the included states
        fun Iterable<State<T>>.toReadableString(): String {
            val stringComparator = Comparator.naturalOrder<String>()
            val sorted = sortedWith(Comparator{ state1, state2 -> stringComparator.compare(state1.name, state2.name) })

            // If the set is empty, return the empty set ;)
            if(sorted.isEmpty()) return "∅"

            // A non-empty set starts with a '{'
            val builder = StringBuilder("{")

            // Append the name of each state, followed by a comma
            for(state in sorted) builder.append(state.name).append(',')

            // The last character should be a '}', not a comma, so replace the above appended comma accordingly
            builder.setCharAt(builder.length - 1, '}')

            return builder.toString()
        }

        // Create a traverser object for this automaton and get the initial state of it
        val traverser = StateTraverser(entryPoint!!)
        val startingState = traverser.currentState

        // Initialize the table with an empty map
        tableEntries[startingState] = HashMap()

        var currentMapping: Pair<MutableList<State<T>>, T>? = null

        // Continue to populate table until all columns of all rows are populated
        while(tableEntries.run {
                    currentMapping = findUnpopulatedMapping()
                    currentMapping != null
                }){

            // Set the state of the traverser to the state for which we are populating the table entry
            traverser.currentState = currentMapping!!.first
            traverser.traverse(currentMapping!!.second)

            // Save the result of the traversal
            tableEntries[currentMapping!!.first]!![currentMapping!!.second] = traverser.currentState

            // If the resulting state is one for which we don't have a row in the table, create it!
            if(tableEntries.keys.firstOrNull { traverser.currentState.contentsEquals(it) } == null)
                tableEntries[traverser.currentState] = HashMap()
        }

        // Print the table, if requested
        if(printTable)
            for(key in tableEntries.keys.sortedBy { if(it.contentsEquals(startingState)) 0 else if (it.isAcceptState()) 2 else 1  }){
                print(
                        (if(key.contentsEquals(startingState)) "→  " else "   ") +
                                (if(key.isAcceptState()) "F" else " ") +
                                " " +
                                key.toReadableString() +
                                " : "
                )
                for (verb in language.elements)
                    print(verb.toString() + "(" + tableEntries[key]!![verb]!!.toReadableString() + ") ")
                println()
            }

        // Create a one-to-one mapping between the set of reachable state-sets of the NFA to states in the DFA
        val oldToNew = HashMap<MutableList<State<T>>, State<T>>()

        // Create the DFA
        val dfa = Automaton(language, true)

        // Generate the NFA-DFA state-set-to-state mappings, as well as populating the DFA with the necessary states
        for(tableState in tableEntries.keys)
            oldToNew[tableState] = dfa.makeState(tableState.toReadableString(), tableState.isAcceptState())

        // Apply the mapping schema determined by the generated state-table to the DFA
        for(oldState in oldToNew.keys)
            for(mapping in tableEntries[oldState]!!)
                oldToNew[oldState]!!.addConnective(mapping.key, oldToNew[mapping.value]!!)

        // Set the start of the DFA to be the state corresponding to the starting state of the NFA
        dfa.entryPoint = oldToNew[startingState]

        return dfa
    }

    /**
     * check if the contents of two collections is equal.
     *
     * @param other The other collection to compare against
     *
     * @return True if the length of the two collections is equal, and each element in one collection has an equivalent
     * element in the other
     */
    private infix fun <V> Collection<V>.contentsEquals(other: Collection<V>) =
            size == other.size &&
            firstOrNull { other.firstOrNull { check -> check == it } == null } == null

    /**
     * Whether or not the given set of states is an accept-state-set.
     *
     * @return True if one or more of the states in the list is an accept state, otherwise false.
     */
    private fun MutableList<State<T>>.isAcceptState() = firstOrNull { it.acceptState } != null

    /**
     * Create a [StateTraverser] for this automaton.
     *
     * @return A [StateTraverser] starting at the set [entryPoint].
     *
     * @exception IllegalStateException If the [entryPoint] for this automaton is null.
     */
    fun makeTraverser(): StateTraverser {
        if(entryPoint == null)
            throw IllegalStateException("Entry point state must be defined!")
        return StateTraverser(entryPoint!!)
    }

    /**
     * A class that allows simulation of an automaton.
     *
     * @param entryPoint Which state to start at when simulating the automaton.
     */
    inner class StateTraverser(entryPoint: State<T>) {
        /**
         * A list describing the set of currently active states. In the case of a deterministic automaton, this will at
         * most have a size of 1.
         */
        var currentState: MutableList<State<T>> = ArrayList()

        /**
         * Whether or not the current state(s) is an accept state
         */
        val accepted: Boolean
            get() = currentState.isAcceptState()

        init {
            // The initial state is determined by epsilon-traversal
            currentState.traverseEpsilon(entryPoint)
        }

        /**
         * Traverse states according to the given string
         *
         * @param verbs The string to traverse according to. All elements of the string must be part of the language of
         * the automaton!
         *
         * @see Automaton.language
         */
        fun traverse(vararg verbs: T){
            for(verb in verbs)
                transformState(verb)
        }

        /**
         * Transform the state according to a single element from the language
         *
         * @param verb Element to traverse according to
         */
        private fun transformState(verb: T){
            val nextState = ArrayList<State<T>>()

            // Loop through all active states
            for(state in currentState)
                // Get all states that each state transitions to for the given element
                for(traverseState in state.getConnective(verb))
                    // Perform epsilon transitions for each newly discovered state
                    nextState.traverseEpsilon(traverseState)

            // Update the state of the traverser
            currentState = nextState
        }

        /**
         * Perform an epsilon transition from the given state and apply it to the list represented by this object.
         */
        private fun MutableList<State<T>>.traverseEpsilon(state: State<T>){
            // Bad way of avoiding duplicates, but hey, I wrote this in a couple of hours
            if(!contains(state)) add(state)

            // Recursively traverse epsilon connectives for new connectives (i.e. for states not already traversed)
            state.getEpsilon()
                    .filterNot { contains(it) }
                    .forEach { traverseEpsilon(it) }

        }
    }
}