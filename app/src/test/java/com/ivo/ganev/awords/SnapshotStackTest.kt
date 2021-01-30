package com.ivo.ganev.awords

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.fail


class SnapshotStackTest {
    class StringSnapshot(val string: String) : Snapshot<String> {
        override fun storedState(): String {
            return string
        }
    }

    private val first = StringSnapshot("one")
    private val second = StringSnapshot("two")
    private val third = StringSnapshot("three")

    fun `test undoable stack's undo and redo implementations`() {
        // ***************************************** Test *************************************
        //             cp       backward                    forward
        //                      stack                       stack
        // 1. store(a)          { a }                       { }
        // 2. store(b)          { b, a }                    { }
        // 3. undo              { a }                       { b }
        // ** Wrong **
        // 4. store(c)          { c, a }                    { b } <- if we don't clear the forward stack on pushing
        //                                                  then we'll end up with really awkward history. See bellow:
        // 5. undo              { push(c), a }  -- c ->    { c, b }
        // 6. redo              { c, a }        <- c --    { pop(c), b  }
        // 7. redo              { b, c, a }     <- b --    { pop(b) }
        //
        // ** Correct **
        // 4. store(c)          { c, a }                   { clear() }  <- this time the forward stack is cleared.
        // 5. undo              { push(c), a }  -- c ->    { c }
        // 6. redo              { c, a }        <- c --    { pop(c) }
        // 7. redo              { c, a }                   { }
        // 8. undo              { pop(c), a }   -- c ->    { c }
        // 9. undo              { pop(a) }      -- a ->    { c, a}
        // 10.undo              { }                        { c, a }

        // on undo -> push
    }
//    @Test
//    fun `test undoable stack's undo and redo implementations`() {
//        // Testing implementations:
//        // undo()  - backward stack transfers its last element to the forward stack giving it as a result.
//        // push()  - stores the element in the backward stack and clears the forward stack otherwise the history would be quite awkward.
//        // redo()  - moves the last element of the forward stack to the backward stack and return the value.
//
//        // ***************************************** Test *************************************
//        //             backward                    forward
//        //             stack                       stack
//        // 1. store(a) { a }                       { }
//        // 2. store(b) { b, a }                    { }
//        // 3. undo     { a }                       { b }
//        // ** Wrong **
//        // 4. store(c) { c, a }                    { b } <- if we don't clear the forward stack on pushing
//        //                                                  then we'll end up with really awkward history. See bellow:
//        // 5. undo     { push(c), a }  -- c ->    { c, b }
//        // 6. redo     { c, a }        <- c --    { pop(c), b  }
//        // 7. redo     { b, c, a }     <- b --    { pop(b) }
//        //
//        // ** Correct **
//        // 4. store(c) { c, a }                   { clear() }  <- this time the forward stack is cleared.
//        // 5. undo     { push(c), a }  -- c ->    { c }
//        // 6. redo     { c, a }        <- c --    { pop(c) }
//        // 7. redo     { c, a }                   { }
//        // 8. undo     { pop(c), a }   -- c ->    { c }
//        // 9. undo     { pop(a) }      -- a ->    { c, a}
//        // 10.undo     { }                        { c, a }
//
//        // 1.
//        val snapshotStack = SnapshotStack<String>(3)
//        snapshotStack.store(first)
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 1 &&
//                        forwardStack.isEmpty() &&
//                        backwardStack[0].storedState() == first.storedState()
//            }
//        }
//
//        // 2.
//        snapshotStack.store(second)
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 2 &&
//                        forwardStack.isEmpty() &&
//                        backwardStack[0].storedState() == first.storedState() &&
//                        backwardStack[1].storedState() == second.storedState()
//            }
//        }
//
//        // 3.
//        snapshotStack.undo { }
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 1 &&
//                        forwardStack.size == 1 &&
//                        backwardStack[0].storedState() == first.storedState() &&
//                        forwardStack[0].storedState() == second.storedState()
//            }
//        }
//
//        // 4.
//        snapshotStack.store(third)
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 2 &&
//                        forwardStack.isEmpty() &&
//                        backwardStack[1].storedState() == third.storedState() &&
//                        backwardStack[0].storedState() == first.storedState()
//            }
//        }
//
//        // 5.
//        snapshotStack.undo { }
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 1 &&
//                        forwardStack.size == 1 &&
//                        backwardStack[0].storedState() == first.storedState() &&
//                        forwardStack[0].storedState() == third.storedState()
//            }
//        }
//
//        // 6.
//        snapshotStack.redo { }
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 2 &&
//                        forwardStack.isEmpty() &&
//                        backwardStack[1].storedState() == third.storedState() &&
//                        backwardStack[0].storedState() == first.storedState()
//            }
//        }
//
//        // 7. Assert that redo was not successful
//        assert(snapshotStack.redo { fail() } == false)
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 2 &&
//                        backwardStack[1].storedState() == third.storedState() &&
//                        backwardStack[0].storedState() == first.storedState()
//            }
//        }
//
//        // 8.
//        snapshotStack.undo {
//            assert(it.storedState() == third.storedState())
//        }
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.size == 1 &&
//                        forwardStack.size == 1 &&
//                        backwardStack[0].storedState() == first.storedState() &&
//                        forwardStack[0].storedState() == third.storedState()
//            }
//        }
//
//        // 9.
//        val success = snapshotStack.undo {
//            assert(it.storedState() == first.storedState())
//        }
//        assertTrue(success)
//
//        assertTrue {
//            with(snapshotStack) {
//                backwardStack.isEmpty() &&
//                        forwardStack.size == 2 &&
//                        forwardStack[1].storedState() == first.storedState() &&
//                        forwardStack[0].storedState() == third.storedState()
//            }
//        }
//
//
//        // 10.
//        snapshotStack.undo {
//            fail()
//        }
// }

}