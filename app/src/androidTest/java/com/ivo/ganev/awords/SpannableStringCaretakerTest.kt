package com.ivo.ganev.awords

import android.text.style.ClickableSpan
import android.view.View
import com.ivo.ganev.awords.text.SpannableStringCaretaker
import org.junit.Test
import kotlin.test.assertEquals


class SpannableStringCaretakerTest {
    fun  clickableSpan() =
        object : ClickableSpan() {
            override fun onClick(widget: View) {
                TODO("Not yet implemented")
            }
        }

    @Suppress("SpellCheckingInspection")
    @Test
    fun test() {
        val one = "one"
        val two = "two"
        val three = "three"
        val four = "four"

        //      col    1             2              3            4
        // test
        // case                                                           "one two three four"
        // 1.repl      (one)->two   two             three        four     "two two three four"
        // 2.repl      (two)->three two             three        four     "three two three four"
        // 3.repl      three        (two)->three    three        four     "three three three four"
        // 4.undo      three        (three)->two    three        four     "three two three four"
        // 5           three->(two) two             three        four
        // 6           two->(one)   two
        // 7           one->(two)   two
        // 8           two->(three) two
        // 9           three        (two)->three
        val sentence = "$one $two $three $four"


        val caretaker = SpannableStringCaretaker(sentence) { clickableSpan() }

        caretaker.undo()

        //1.
        caretaker.replace(0, 3, two)
        assertEquals(caretaker.toString(), "$two $two $three $four")

        //2.
        caretaker.replace(0, 3, three)
        assertEquals("$three $two $three $four", caretaker.toString())
        //3.
        caretaker.replace(6, 9, three)
        assertEquals("$three $three $three $four", caretaker.toString())

//        println(caretaker.partitionedState)

        //4.
        caretaker.undo()
        assertEquals("$three $two $three $four", caretaker.toString())

        println(caretaker.undo())
        assertEquals("$two $two $three $four", caretaker.toString())

        caretaker.undo()
        assertEquals("$one $two $three $four", caretaker.toString())

        caretaker.redo()
        assertEquals("$two $two $three $four", caretaker.toString())

        caretaker.redo()
        assertEquals("$three $two $three $four", caretaker.toString())

        caretaker.redo()
        assertEquals("$three $three $three $four", caretaker.toString())

        caretaker.redo()
        assertEquals("$three $three $three $four", caretaker.toString())
    }
}