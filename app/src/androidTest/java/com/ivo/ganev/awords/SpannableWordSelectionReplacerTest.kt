package com.ivo.ganev.awords

import android.text.style.ClickableSpan
import android.view.View
import com.ivo.ganev.awords.SpannableStringBuilderUndoable.WordSelection
import org.junit.Test
import kotlin.test.assertTrue

class SpannableStringBuilderUndoableTest {
    @Test
    fun test() {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                TODO("Not yet implemented")
            }
        }

        val replacement1 = "Glamorous"
        val replacement2 = "girl"
        val replacement3 = "friendly"
        val replacement4 = "The"


        val sentence = "Hello beautiful world!"
        val expected1 = "Glamorous beautiful world!"
        val expected2 = "Hello beautiful girl!"
        val expected3 = "Hello friendly girl!"
        val expected4 = "The friendly girl!"

        val undoableSpanBuilder = SpannableStringBuilderUndoable(sentence)


        undoableSpanBuilder.replaceAndRecordUndo(WordSelection(clickableSpan, replacement1, 0, 5))
        println("Should be: $expected1 <-> $undoableSpanBuilder")
        assertTrue { undoableSpanBuilder.toString() == expected1 }

        undoableSpanBuilder.undo()
        println("Should be: $sentence <-> $undoableSpanBuilder")
        assertTrue { undoableSpanBuilder.toString() == sentence }

        // undo stack should be empty
        println("Stack should be empty")
        println(undoableSpanBuilder.printStack())

        undoableSpanBuilder.replaceAndRecordUndo(WordSelection(clickableSpan, replacement2, 16, 21))
        println("Should be: $expected2 <-> $undoableSpanBuilder")
        assertTrue { undoableSpanBuilder.toString() == expected2 }

        undoableSpanBuilder.replaceAndRecordUndo(WordSelection(clickableSpan, replacement3, 6, 15))
        println("Should be: $expected3 <-> $undoableSpanBuilder")
        assertTrue { undoableSpanBuilder.toString() == expected3 }

        undoableSpanBuilder.replaceAndRecordUndo(WordSelection(clickableSpan, replacement4, 0, 5))
        println("Should be: $expected4 <-> $undoableSpanBuilder")
        assertTrue { undoableSpanBuilder.toString() == expected4 }

        println("0000000000000000000000000000000000")
        println("The text is now: $undoableSpanBuilder")
        println("Stack before mass undo: " + undoableSpanBuilder.printStack())
        println("0000000000000000000000000000000000")

        undoableSpanBuilder.undo()

        println("0000000000000000000000000000000000")
        println("The text is now: $undoableSpanBuilder")
        println(undoableSpanBuilder.printStack())
        println("0000000000000000000000000000000000")

        undoableSpanBuilder.undo()

        println("0000000000000000000000000000000000")
        println("The text is now: $undoableSpanBuilder")
        println(undoableSpanBuilder.printStack())
        println("0000000000000000000000000000000000")

        undoableSpanBuilder.undo()

        println("0000000000000000000000000000000000")
        println("The text is now: $undoableSpanBuilder")
        println(undoableSpanBuilder.printStack())
        println("0000000000000000000000000000000000")
    }
}