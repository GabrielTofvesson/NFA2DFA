import dev.w1zzrd.automata.*

fun main(args: Array<String>){
    // Create a language with the set of elements {0, 1}
    val language = Language.makeLanguage(0, 1)

    // Create a nondeterministic automaton
    val nfa = Automaton(language, false)

    // Declare states of the NFA
    val stateS = nfa.makeState("s")
    val stateQ1 = nfa.makeState("q1")
    val stateQ2 = nfa.makeState("q2", true)
    val stateP = nfa.makeState("p")
    val stateQ = nfa.makeState("q")
    val stateR = nfa.makeState("r", true)

    // Add epsilon transition from S to Q1 and P
    stateS.addEpsilon(stateQ1, stateP)

    // Add regular state-transition connectives
    stateQ1.addConnective(0, stateQ1)
    stateQ1.addConnective(1, stateQ2)

    stateQ2.addConnective(0, stateQ1)

    stateP.addConnective(arrayOf(0, 1), stateP)
    stateP.addConnective(1, stateQ)

    stateQ.addConnective(arrayOf(0, 1), stateR)

    // Declare S as the initial state
    nfa.entryPoint = stateS

    // Convert the NFA into an equivalent DFA
    val dfa = nfa.toDeterministicAutomaton(true)

    // Get a traverser for the DFA and manually traverse the string "1100", then print the resulting state
    val dtraverser = dfa.makeTraverser()
    dtraverser.traverse(1, 1, 0, 0)
    println(dtraverser.currentState.toString())

    // Do the same as above but for the NFA
    val ntraverser = nfa.makeTraverser()
    ntraverser.traverse(1, 1, 0, 0)
    println(ntraverser.currentState.toString())
}