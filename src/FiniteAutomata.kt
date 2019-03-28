import dev.w1zzrd.automata.*

private val testString = arrayOf(1, 0, 0, 1 ,0)

fun main(args: Array<String>){
    // Create a language with the set of elements {0, 1}
    val language = Language.makeLanguage(0, 1)

    // Create a nondeterministic automaton
    val nfa = Automaton(language, false)

    // Declare states of the NFA
    val stateS = nfa.makeState("s")
    val stateA = nfa.makeState("a")
    val stateB = nfa.makeState("b", true)
    val stateC = nfa.makeState("c")
    val stateD = nfa.makeState("d")
    val stateE = nfa.makeState("e", true)

    stateS.addEpsilon(stateA, stateC)

    stateA.addConnective(1, stateB)

    stateB.addConnective(0, stateB)

    stateC.addConnective(1, stateB, stateD)
    stateC.addConnective(0, stateB)

    stateD.addConnective(0, stateE)

    stateE.addConnective(arrayOf(0, 1), stateC)


    // Declare S as the initial state
    nfa.entryPoint = stateS

    // Convert the NFA into an equivalent DFA
    val dfa = nfa.toDeterministicAutomaton(true)
    val minimal = dfa.toMinimalDFA()

    // Get a traverser for the DFA and manually traverse the string "1100", then print the resulting state
    val dtraverser = dfa.makeTraverser()
    dtraverser.traverse(*testString)
    println("\nDFA simulation:\n\t${dtraverser.currentState}\n\tAccepts: ${dfa.accepts(*testString)}\n")

    // Do the same as above but for the NFA
    val ntraverser = nfa.makeTraverser()
    ntraverser.traverse(*testString)
    println("\nNFA simulation:\n\t${ntraverser.currentState}\n\tAccepts: ${nfa.accepts(*testString)}")

    // Do the same as above but for the minimal DFA
    val mtraverser = minimal.makeTraverser()
    mtraverser.traverse(*testString)
    println("\nminimal DFA simulation:\n\t${mtraverser.currentState}\n\tAccepts: ${minimal.accepts(*testString)}\n")
}