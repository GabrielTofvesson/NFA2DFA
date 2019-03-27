package dev.w1zzrd.automata

import kotlin.collections.ArrayList

class Language<T> private constructor(private val language: List<T>) {

    val elements: List<T>
        get() = ArrayList(language)

    infix fun hasVerb(verb: T) = language.contains(verb)
    infix fun hasVerbs(string: Iterable<T>) = string.firstOrNull { !(this hasVerb it) } == null
    infix fun hasVerbs(string: Array<out T>) = string.firstOrNull { !(this hasVerb it) } == null

    companion object {
        fun <T> makeLanguage(vararg language: T): Language<T> {
            language.forEachIndexed { outerIndex, outerValue ->
                language.forEachIndexed { innerIndex, innerValue ->
                    if(outerIndex != innerIndex && (outerValue?.equals(innerValue) == true || outerValue == innerValue))
                        throw IllegalArgumentException("Elements in language must be unique!")
                }
            }
            return Language(language.toList())
        }
    }
}