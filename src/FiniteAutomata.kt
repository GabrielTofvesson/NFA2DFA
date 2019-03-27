import dev.w1zzrd.automata.*

fun main(args: Array<String>){
    val language = Language.makeLanguage(0, 1)
    val nfa = Automaton(language, false)

    val stateS = nfa.makeState("s")
    val stateQ1 = nfa.makeState("q1")
    val stateQ2 = nfa.makeState("q2", true)
    val stateP = nfa.makeState("p")
    val stateQ = nfa.makeState("q")
    val stateR = nfa.makeState("r", true)

    stateS.addEpsilon(stateQ1, stateP)

    stateQ1.addConnective(0, stateQ1)
    stateQ1.addConnective(1, stateQ2)

    stateQ2.addConnective(0, stateQ1)

    stateP.addConnective(arrayOf(0, 1), stateP)
    stateP.addConnective(1, stateQ)

    stateQ.addConnective(arrayOf(0, 1), stateR)

    nfa.entryPoint = stateS

    val dfa = nfa.toDeterministicAutomaton(true)

    val dtraverser = dfa.makeTraverser()
    dtraverser.traverse(1, 1, 0, 0)
    println(dtraverser.currentState.toString())

    val ntraverser = nfa.makeTraverser()
    ntraverser.traverse(1, 1, 0, 0)
    println(ntraverser.currentState.toString())
}