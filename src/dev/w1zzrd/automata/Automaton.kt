package dev.w1zzrd.automata

class Automaton<T>(val language: Language<T>, val deterministic: Boolean){
    private val states = ArrayList<State<T>>()
    private val factory = StateFactory(language, deterministic)
    var entryPoint: State<T>? = null

    fun addStates(vararg states: State<T>){
        states.forEach { state ->
            if(addState(state))
                language.elements.forEach { verb ->
                    addStates(*state.getConnective(verb).toTypedArray())
                }
        }
    }

    fun addState(state: State<T>): Boolean{
        if(deterministic && !state.isDeterministic)
            throw IllegalArgumentException("Deterministic automaton can only contain deterministic states!")

        if(!states.contains(state)){
            states.add(state)
            return true
        }
        return false
    }

    fun makeState(name: String, acceptState: Boolean = false): State<T> {
        val state = factory.make(name, acceptState)
        if(!addState(state))
            throw IllegalArgumentException("Duplicate state detected!")
        return state
    }

    fun accepts(vararg string: T): Boolean {
        if(!(language hasVerbs string))
            throw IllegalArgumentException("All verbs in string must be part of the language!")

        if(entryPoint == null) return false

        val traverser = StateTraverser(entryPoint!!)
        traverser.traverse(*string)

        return traverser.accepted
    }

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
        fun Iterable<State<T>>.toReadableString(): String {
            val builder = StringBuilder("{")
            val stringComparator = Comparator.naturalOrder<String>()
            val sorted = sortedWith(Comparator{ state1, state2 -> stringComparator.compare(state1.name, state2.name) })
            if(sorted.isEmpty()) return "∅"
            for(state in sorted)
                builder.append(state.name).append(',')
            if(sorted.isNotEmpty()) builder.setCharAt(builder.length - 1, '}')
            else builder.append('}')
            return builder.toString()
        }

        val traverser = StateTraverser(entryPoint!!)
        val startingState = traverser.currentState

        // Initialize table
        tableEntries[startingState] = HashMap()

        var currentMapping: Pair<MutableList<State<T>>, T>? = null
        while(tableEntries.run {
                    currentMapping = findUnpopulatedMapping()
                    currentMapping != null
                }){
            traverser.currentState = currentMapping!!.first
            traverser.traverse(currentMapping!!.second)

            if(tableEntries[currentMapping!!.first] == null)
                tableEntries[currentMapping!!.first] = HashMap()

            tableEntries[currentMapping!!.first]!![currentMapping!!.second] = traverser.currentState
            if(tableEntries.keys.firstOrNull { traverser.currentState.contentsEquals(it) } == null)
                tableEntries[traverser.currentState] = HashMap()
        }

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

        val oldToNew = HashMap<MutableList<State<T>>, State<T>>()

        val dfa = Automaton(language, true)
        for(tableState in tableEntries.keys)
            oldToNew[tableState] = dfa.makeState(tableState.toReadableString(), tableState.contentsEquals(startingState))

        for(oldState in oldToNew.keys)
            for(mapping in tableEntries[oldState]!!)
                oldToNew[oldState]!!.addConnective(mapping.key, oldToNew[mapping.value]!!)

        dfa.entryPoint = oldToNew[startingState]

        return dfa
    }

    private infix fun <V> Collection<V>.contentsEquals(other: Collection<V>) =
            size == other.size &&
            firstOrNull { other.firstOrNull { check -> check == it } == null } == null

    private fun MutableList<State<T>>.isAcceptState() = firstOrNull { it.acceptState } != null

    fun makeTraverser(): StateTraverser{
        if(entryPoint == null)
            throw IllegalStateException("Entry point state must be defined!")
        return StateTraverser(entryPoint!!)
    }

    inner class StateTraverser(entryPoint: State<T>) {
        var currentState: MutableList<State<T>> = ArrayList()

        val accepted: Boolean
            get() = currentState.isAcceptState()

        init {
            currentState.traverseEpsilon(entryPoint)
        }

        fun traverse(vararg verbs: T){
            for(verb in verbs)
                transformState(verb)
        }

        private fun transformState(verb: T){
            val nextState = ArrayList<State<T>>()

            for(state in currentState)
                for(traverseState in state.getConnective(verb)) {
                    nextState.traverseEpsilon(traverseState)
                }

            currentState = nextState
        }

        private fun MutableList<State<T>>.traverseEpsilon(state: State<T>){
            if(!contains(state)) add(state)
            for(epsilonState in state.getEpsilon())
                if(!contains(epsilonState))
                    traverseEpsilon(epsilonState)

        }
    }
}