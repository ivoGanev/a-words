package com.ivo.ganev.awords

import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import com.ivo.ganev.awords.SpannableStringCaretaker.WordSelection
import org.junit.Test

class SpannableStringCaretakerTest {
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


        // Test if we getting the correct output from the caretaker.
        // ************************ From the table: ************************
        //   Op                               From:                       To:
        //1. replace (two) with (three) | x | "one two three four"   ->  "one three three four"
        //2. replace (four) with (one)  | x | "one three three four" ->  "one three three one"
        //3. undo(one) with(four)       | x | "one three three one"  ->  "one three three four"
        //4. undo(three) with(two)      | x | "one three three four" ->  "one two three four"
        //5. redo(two) with (three)     | x | "one two three four"   ->  "one three three four"
        // ************************ We are testing the state: ************************
        //                          bs                           fs
        // undo                     four, two
        //                                                       three, one
        // undo



        val sentence = "$one $two $three $four"
        val caretaker = SpannableStringCaretaker(sentence)
        caretaker.replaceAndStore(
            WordSelection(span, three, 4, 7)
        )
        Log.d("System:", "Replace: $caretaker \n${caretaker.printStack()}")
        assert(caretaker.toString() == "$one $three $three $four")

        caretaker.replaceAndStore(WordSelection(span, one, 16, 20))
        assert(caretaker.toString() == "$one $three $three $one")
        Log.d("System:", "Replace: $caretaker \n${caretaker.printStack()}")


        //3. We roll back to "one three three four"
        caretaker.undo()
        Log.d("System:", "Undo: $caretaker \n${caretaker.printStack()}")
        assert(caretaker.toString() == "$one $three $three $four")


        caretaker.undo()
        Log.d("System:", "Undo: $caretaker ${caretaker.printStack()}")
        assert(caretaker.toString() == "$one $two $three $four")


        caretaker.redo()
        Log.d("System", caretaker.toString())
        Log.d("System", caretaker.printStack())
        // getting the right word with wrong coordinates
        assert(caretaker.toString() == "$one $three $three $four")

    }
}