package com.ivo.ganev.awords

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.fail


class SnapshotStackTest {
    private val first = Word("Hello", 1, 6)
    private val second = Word("World", 7, 12)
    private val third = Word("!", 13, 14)
    private val fourth = Word("One More Time", 1, 1)

    @Test
    // Problem: Make a snapshot of the object and store it in a stacked fashion for later retrieval.
    fun `test pop of word snapshot`() {
        val snapshotStack = SnapshotStack<Word>(3)
        // We shouldn't be able to pop because there are no Snapshots yet.
        snapshotStack.pop {
            fail()
        }

        snapshotStack.push(first)
        snapshotStack.push(second)
        snapshotStack.push(third)

        // Try and push over the limit
        snapshotStack.push(third)
        assertTrue(snapshotStack.size == 3)

        val snapshots = mutableListOf<Snapshot<Word>>()

        // Start popping the stack
        var success: Boolean = snapshotStack.pop {
            snapshots.add(it)
            assertTrue { it == third && snapshotStack.size == 2 }
        }
        assertTrue(success)
        success = snapshotStack.pop {
            snapshots.add(it)
            assertTrue { it == second && snapshotStack.size == 1 }
        }
        assertTrue(success)
        success = snapshotStack.pop {
            snapshots.add(it)
            assertTrue { it == first && snapshotStack.size == 0 }
        }
        assertTrue(success)
        assertTrue { snapshots.count() == 3 }

        success = snapshotStack.pop {
            fail()
        }
        assertTrue(success == false)
    }

    @Test
    fun `test undoable stack`() {
        // The undoable stack is made of two stacks one to process the stack the other one
        // to hold undo operation
        val undoableStack = UndoableSnapshotStack<Word>(4)

        undoableStack.push(first)
        undoableStack.push(second)
        undoableStack.push(third)

        // Tries to pop the third Snapshot from the undo stack { "first", "second", "third" } and pushes it into the redo stack.
        // After the pop we should have { "first", "second "} in the undo stack and { "third" } in the redo stack
        var success = undoableStack.pop {
            assertTrue {
                it.restore() == third && undoableStack.size == 2
            }
        }
        assertTrue(success)

        // Tries to pop a single item from the redo stack { "third" } and pushes it into the undoStack.
        // Now the undo stack should be  { "first, "second", "third" } and the redo stack should be empty
        success = undoableStack.undo {
            assertTrue {
                it.restore() == third && undoableStack.size == 3
            }
        }

        assertTrue(success)
        // undo stack is empty. Do nothing..
        success = undoableStack.undo {
            assertTrue { fail() }
        }
        assertTrue(success == false)

        // Now the stack should be { "first", "second", "third", "fourth" }
        undoableStack.push(fourth)


        success = undoableStack.pop {
            assertTrue {
                it.restore() == fourth && undoableStack.size == 3
            }
        }
        assertTrue(success)

        undoableStack.undo { }

        // Try and push over the limit
        undoableStack.push(fourth)
        undoableStack.push(fourth)
        assertTrue(undoableStack.size == 4)

        // clear the stack
        undoableStack.clear()

        assertTrue(undoableStack.pop {  fail() } == false)
        assertTrue(undoableStack.undo { fail() } == false)

        assertTrue {
            undoableStack.size == 0
        }
    }
}