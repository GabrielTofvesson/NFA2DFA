import dev.w1zzrd.automata.*

fun main(args: Array<String>){
    val language = Language.makeLanguage(0, 1, 2)
    val automaton = Automaton(language, false)

    val stateA = automaton.makeState("a")
    val stateB = automaton.makeState("b")
    val stateC = automaton.makeState("c")
    val stateD = automaton.makeState("d", true)

    stateA.addConnective(arrayOf(0, 1), stateB)
    stateA.addConnective(arrayOf(1, 2), stateC)

    stateB.addConnective(arrayOf(0, 2), stateD)

    stateC.addConnective(arrayOf(0, 1), stateD)

    stateD.addConnective(0, stateA)

    automaton.entryPoint = stateA

    val dfa = automaton.toDeterministicAutomaton(true)
}