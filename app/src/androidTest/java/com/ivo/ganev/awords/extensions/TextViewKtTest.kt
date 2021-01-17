package com.ivo.ganev.awords.extensions

import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.getSpans
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TextViewKtTest {
    @Test
    fun testToNoSpaceClickableSpan() {
        val selectionStart = 6
        // selectionEnd is the buffer offset meaning we have to compensate +1
        val selectionEnd = 12

        // We will replace part of the original text with replacement.
        // The actual string "World." will be replaced with "little  world."
        // to create "Hello little  world."
        val original = SpannableStringBuilder("Hello World.")
        val replacement = "little  world. "

        val text = original.apply {
            replace(selectionStart, selectionEnd, replacement)
            clearSpans()
            toNoSpaceClickableSpan {
                object : ClickableSpan() {
                    override fun onClick(widget: View) {}
                }
            }
        }
        val spans = text.getSpans<ClickableSpan>(0, text.length)

        text.toString() shouldBeEqualTo "Hello little  world. "

        val spansStartEnd = mutableListOf<Pair<Int, Int>>()
        for (e in spans) {
            spansStartEnd.add(Pair(text.getSpanStart(e), text.getSpanEnd(e)))
        }

        spansStartEnd[0] shouldBeEqualTo Pair(0, 5)
        spansStartEnd[1] shouldBeEqualTo Pair(6, 12)
        spansStartEnd[2] shouldBeEqualTo Pair(14, 20)
        spansStartEnd.size shouldBeEqualTo 3
    }
}