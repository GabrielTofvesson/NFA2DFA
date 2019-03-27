package dev.w1zzrd.automata

import kotlin.collections.ArrayList

/**
 * A language for an automaton. This defines what input an automaton can accept.
 *
 * @param language A raw list of what elements are contained in this language.
 */
class Language<T> private constructor(private val language: List<T>) {

    /**
     * The elements of the language. This cannot contain duplicates.
     */
    val elements: List<T>
        get() = ArrayList(language)

    /**
     * Whether or not this language contains a given element.
     *
     * @param verb Element to check for
     *
     * @return True if the given element is in the language set, else false.
     */
    infix fun hasVerb(verb: T) = language.contains(verb)

    /**
     * Whether or not this language contains a set of given elements.
     *
     * @param string Elements to check for
     *
     * @return True if all of the given elements are in the language set, else false.
     */
    infix fun hasVerbs(string: Iterable<T>) = string.firstOrNull { !(this hasVerb it) } == null

    /**
     * Whether or not this language contains a set of given elements.
     *
     * @param string Elements to check for
     *
     * @return True if all of the given elements are in the language set, else false.
     */
    infix fun hasVerbs(string: Array<out T>) = string.firstOrNull { !(this hasVerb it) } == null

    /**
     * Companion object acting as a factory (of sorts) for Languages.
     */
    companion object {

        /**
         * Create a language from a given set of elements.
         *
         * @param language The set of elements defining the language
         *
         * @return A [Language] object if the set of language elements comprise a valid language.
         *
         * @exception IllegalArgumentException If the given set of language elements do not comprise a valid language (
         * i.e. if there are duplicate elements).
         */
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