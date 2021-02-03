package com.ivo.ganev.awords

import java.util.*

/**
 * Interface for representing a non-nullable snapshot.
 * */
interface Snapshot<T : Any> {
    fun storedState(): T
}

class SnapshotStack<T : Any>(private val maxSnapshots: Int = 10) {
    private val _forwardStack = Stack<Snapshot<T>>()
    private val _backwardStack = Stack<Snapshot<T>>()

    /**
     * Looks at the object at the top of the backward stack without removing it
     * from the stack.
     **/
    fun peekBackward(backward: (Snapshot<T>)->Unit) : Boolean {
        return try {
            backward(_backwardStack.peek())
            true
        } catch (ex: EmptyStackException) {
            false
        }
    }

    fun peekForward(forward: (Snapshot<T>)->Unit) : Boolean {
        return try {
            forward(_forwardStack.peek())
            true
        } catch (ex: EmptyStackException) {
            false
        }
    }

    /**
     * Stores a single [Snapshot] inside the stack until the [maxSnapshots]
     * limit is exceeded.
     * */
    fun store(element: Snapshot<T>): Boolean {
        if (_backwardStack.size < maxSnapshots) {
            _backwardStack.push(element)
            _forwardStack.clear()
            return true
        }
        return false
    }

    /**
     * Use [block] to operate over the last [Snapshot] of the stack.
     * This function is silent: it won't throw any exceptions.
     *
     * @return true if the function succeeds, false if it doesn't.
     * */
    fun undo(block: (Snapshot<T>) -> Unit): Boolean {
        return try {
            val element = _backwardStack.pop()
            _forwardStack.push(element)
            block(element)
            true
        } catch (ex: EmptyStackException) {
            false
        }
    }


    /**
     * This function will redo the last stack operation.
     * This function is silent and won't throw any exceptions.
     *
     * @return true if the function succeeds, false if it doesn't.
     * */
    fun redo(block: (Snapshot<T>) -> Unit): Boolean {
        return try {
            val element = _forwardStack.pop()
            _backwardStack.push(element)
            block(element)
            true
        } catch (ex: EmptyStackException) {
            false
        }
    }

    /**
     * Clears the backward and forward stack.
     * */
    fun clear() {
        _backwardStack.clear()
        _forwardStack.clear()
    }

    override fun toString() = buildString {
        appendLine("------------- Backward stack (size: ${_backwardStack.size}) -------------")
        for (i in  _backwardStack.size-1 downTo 0)
            appendLine("$i: ${_backwardStack[i].storedState()}")
        appendLine("------------- Forward  stack (size: ${_forwardStack.size})-------------")
        for (i in _forwardStack.size-1 downTo 0 )
            appendLine("$i: ${_forwardStack[i].storedState()}")
    }
}
