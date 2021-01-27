package com.ivo.ganev.awords

import java.util.*


class SnapshotStack<T : Any>(
        private val snapshots: Stack<Snapshot<T>> = Stack()
) {
    private lateinit var lastSnapshot: Snapshot<T>

    fun pushLast(): Snapshot<T> {
        return snapshots.push(lastSnapshot)
    }

    fun push(snapshot: Snapshot<T>) {
        snapshots.push(snapshot)
    }

    fun pop(): Snapshot<T> {
        lastSnapshot = snapshots.pop()
        return lastSnapshot
    }

    val isNotEmpty: Boolean
        get() = snapshots.isNotEmpty()
}

class StringSnapshot(string: String) : Snapshot<String> {
    var text: String = string

    override fun restore(): String {
        return text
    }
}

interface Snapshot<T> {
    fun restore(): T
}