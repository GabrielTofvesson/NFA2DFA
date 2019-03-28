package dev.w1zzrd.automata

internal typealias Partition<T> = MutableList<State<T>>
internal typealias PartitionSet<T> = MutableList<Partition<T>>

/**
 * Ensure that the partition set is valid.
 */
internal fun <T> PartitionSet<T>.ensureCorrectness(){
    for(set in this){
        for(element in set){
            this
                    .filter { it != set && it.contains(element) }
                    .forEach { throw IllegalStateException("Two partitions cannot share a state!") }
        }
    }
}

/**
 * Check if two states share he same partition
 *
 * @param state1 The first state to compare against
 * @param state2 The second state to compare against
 *
 * @return True if [state1] and [state2] are contained within the same partition in the set
 */
internal fun <T> PartitionSet<T>.sharePartition(state1: State<T>, state2: State<T>) =
        this.none { it.contains(state1) && !it.contains(state2) }

/**
 * Check if the contents of this set is functionally equivalent to another set
 */
internal fun <T> PartitionSet<T>.isEquivalentTo(other: PartitionSet<T>) =
        size == other.size &&
                firstOrNull { other.firstOrNull { otherIt -> it.contentsEquals(otherIt) } == null } == null

/**
 * Find a [Partition] containing a given state
 *
 * @param state Which state to find the owning partition for
 *
 * @return A corresponding partition if there is one that contains the given state, if no such partition exists, null.
 */
internal fun <T> PartitionSet<T>.getAssociatedPartition(state: State<T>) =
        firstOrNull { it.firstOrNull { checkState -> checkState == state } != null }

/**
 * Check if this [PartitionSet] contains a [Partition] which in turn contains the given [state].
 *
 * @param state The state to search for.
 *
 * @return True if there is a [Partition] which contains the given [state], else false.
 */
internal fun <T> PartitionSet<T>.hasAssociatedSet(state: State<T>) = getAssociatedPartition(state) != null

/**
 * Create a blank [PartitionSet].
 */
internal fun <T> makeBlankPartitionSet(): PartitionSet<T> = ArrayList()