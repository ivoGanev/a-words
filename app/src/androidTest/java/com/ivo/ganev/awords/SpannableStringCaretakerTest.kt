package com.ivo.ganev.awords

import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import org.junit.Test

class SpannableStringCaretakerTest {
    @Suppress("SpellCheckingInspection")
    @Test
    fun test() {
        val span = object : ClickableSpan() {
            override fun onClick(widget: View) {
                TODO("Not yet implemented")
            }
        }

        val one = "one"
        val two = "two"
        val three = "three"
        val four = "four"

        val sentence = "$one $two $three $four"
        val caretaker = SpannableStringCaretaker(sentence)

        // 1.repl "one $two three four" -> "one $three three four"
        // 2.repl "one three three $four" -> "one three three $one"
        // 3.undo "one three three $one" -> "one three three $four"
        // 3.undo and skip "one three three $four" to "one $three three four"
        //
        // repl: $two[4..7]    "one $two three four"   with $three[4..9] "one $three three four"  |x|   Undo to: $two -> remove $three[4..9]
        // repl: $four[16..20] "one three three $four" with $one[16..19] "one three three $one"   |x|   Redo to: $three -> remove $two[4..7]
        // undo: "one three three $one->$four"

        // Replace "one $two three four" with "one $three three four"
        caretaker.replaceAndStore(4, 7, three)
       // assert(caretaker.toString() == "$one $three $three $four")

        //Log.d("System:", "Undo: $caretaker ${caretaker.printStack()}")
        // Replace "one three three $four" with "one three three $one"
        caretaker.replaceAndStore(16, 20, one)

        //"one three three one"
        caretaker.undo()
        Log.d("System", caretaker.toString())
        Log.d("System", caretaker.printStack())
        caretaker.undo()
        Log.d("System", caretaker.toString())
        Log.d("System", caretaker.printStack())
        caretaker.undo()
        Log.d("System", caretaker.toString())
        Log.d("System", caretaker.printStack())

        // undo
        // two
        // three
        // four
        // one

        caretaker.redo()
        caretaker.redo()
        caretaker.redo()
        caretaker.redo()

       // assert(caretaker.toString() == "$one $three $three $one")
//
//        // Undo from "one three three $one" to "one three three $four"
//        Log.d("System:", "Undo: $caretaker ${caretaker.printStack()}")
//        caretaker.undo()
//        Log.d("System:", "Undo: $caretaker ${caretaker.printStack()}")
//        assert(caretaker.toString() == "$one $three $three $four")
//
//        Log.d("System:", "Undo: $caretaker ${caretaker.printStack()}")
//        // Undo from "one three three $four" to "one $three three four"
//        caretaker.undo()
//        Log.d("System:", "Undo: $caretaker ${caretaker.printStack()}")
//        assert(caretaker.toString() == "$one $three $three $four")
//
//        // Undo from "one $three three four" to "one $two three four"
//        caretaker.redo()

        // getting the right word with wrong coordinates
        //    assert(caretaker.toString() == "$one $three $three $four")

    }
}