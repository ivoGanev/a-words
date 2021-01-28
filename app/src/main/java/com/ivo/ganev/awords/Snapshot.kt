package com.ivo.ganev.awords

import java.lang.IndexOutOfBoundsException
import java.util.*

/**
 * Interface for representing a non-nullable snapshot.
 * */
interface Snapshot<T : Any> {
    fun restore(): T
}

data class Word(
        val text: String = "",
        val selectionStart: Int,
        val selectionEnd: Int
) : Snapshot<Word> {
    override fun restore(): Word {
        return this
    }
}

open class SnapshotStack<T : Any>(private val maxSnapshots: Int = 10) {
    private val snapshots: Stack<Snapshot<T>> = Stack()

    open val size get() = snapshots.size

    /**
     * Pushes a [Snapshot] inside the stack until it becomes full.
     * */
    open fun push(snapshot: Snapshot<T>): Boolean {
        if (size < maxSnapshots) {
            snapshots.push(snapshot)
            return true
        }
        return false
    }

    /**
     *  Tries to pop a single [Snapshot] from the stack. If the stack has no items
     *  the [block] will not get executed.
     *
     *  @return True - if the stack has items. False - if there are no items in the stack.
     *
     * */
    open fun pop(block: (Snapshot<T>) -> Unit): Boolean {
        return try {
            val popped = snapshots.pop()
            block(popped)
            true
        } catch (ex: EmptyStackException) {
            false
        }
    }

    /**
     * Removes all elements from the stack.
     * */
    open fun clear() {
        snapshots.clear()
    }

    open val isNotEmpty: Boolean
        get() = snapshots.isNotEmpty()


}

class UndoableSnapshotStack<T : Any>(maxSnapshots: Int = 10) : SnapshotStack<T>(maxSnapshots) {
    private val undoStack = SnapshotStack<T>()

    /**
     * Undoes the last stack push. Example:
     * If the stack was: { Hello, World } and after popping is : { Hello }
     * then using this command will make the stack { Hello, World } again.
     * */
    fun undo(block: (Snapshot<T>) -> Unit): Boolean {
        return undoStack.pop {
            super.push(it)
            block(it)
        }
    }

    /**
     * This function will pop the last [Snapshot] and it will record it for
     * undoing with [undo].
     * */
    override fun pop(block: (Snapshot<T>) -> Unit): Boolean {
        return super.pop {
            undoStack.push(it)
            block(it)
        }
    }

    /**
     * Removes all elements from the stack. This will wipe out all
     * the elements that are kept in memory for the [undo] operation
     * as well.
     * */
    override fun clear() {
        undoStack.clear()
        super.clear()
    }
}