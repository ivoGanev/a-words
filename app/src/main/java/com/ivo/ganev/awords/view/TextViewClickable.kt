package com.ivo.ganev.awords.view

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.toSpannable
import androidx.core.text.toSpanned
import com.ivo.ganev.awords.extensions.matchNoSpace
import timber.log.Timber


class TextViewClickable :
    AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * With spannable factory we avoid object creation
     * */
    private val spannableFactory = object : Spannable.Factory() {
        override fun newSpannable(source: CharSequence?): Spannable {
            return source as Spannable
        }
    }

    init {
        setLinkMovementMethod()
        setText(text, BufferType.SPANNABLE)
        setSpannableFactory(spannableFactory)
    }

    private fun setLinkMovementMethod() {
        movementMethod = LinkMovementMethod.getInstance()
    }


    /**
     * This listener will be notified when any part of the TextView's
     * text gets clicked.
     * */
    var onWordClickedListener: OnWordClickedListener? = null

    private val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onWordClickedListener?.onWordClick(selectedWord())
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.bgColor = Color.WHITE
        }
    }

    private fun clickableSpan() = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onWordClickedListener?.onWordClick(selectedWord())
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.bgColor = Color.WHITE
        }
    }


    /**
     * Will return the currently selected word by the pointer
     * or selection which the user made.
     * */
    private fun selectedWord(): String =
        when (isLegalSelection()) {
            true -> text.subSequence(selectionStart, selectionEnd).toString()
            false -> ""
        }

    fun makeTextClickable(text: String) {
        val spannableString = SpannableString(text)
        super.setText(createSpan(spannableString))
    }

    // Mutates spannableText to include click listeners
    private fun createSpan(spannableText: SpannableString): Spannable {
        val matcher = spannableText.matchNoSpace()
        while (matcher.find()) {
            // set all words from the text to be clickable
            spannableText.setSpan(
                        clickableSpan (), matcher.start(), matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannableText
    }

    /**
     * Replaces the selected word from the text view
     * */
    fun replaceSelectedWord(replacement: String) {
        val spannableStringBuilder = SpannableStringBuilder(text)

        // inside the spannable string builder
        // replace start and end of the selection -> removes the span as well
        // the spannableBuilder will be without the text and spans
        // get the start position and add another SpannableString at the start position
        // which wo
        if (isLegalSelection()) {
            // TODO: Incredibly inefficient. Have to find a way to fix
            //       all annoying copying of Strings and SpannableStrings
                println("$selectionStart :: $selectionEnd ")
            text = spannableStringBuilder.apply {
                replace(selectionStart, selectionEnd, replacement)
                // but the thing here is I have to loop through the replacement string
                // and add those spans separately
                val matcher = replacement.matchNoSpace() // ?!?!?
                // how do you add spans separately?
                // how do you get positions separately?
                // "my really" if the replacements is "big  elephant"
                // so span 1 starts at selectionStart and ends at matcher1.size
                // then span 2 starts at (selectionStart + matcher1.size + spaceInterval(some offset)
                // and end at (selectionStart + matcher1.size + spaceInterval(some offset) + matcher2.size)
             //   setSpan() // 1
              //  setSpan() // 2
                clearSpans()
            }
            text = createSpan(SpannableString(text))
        }
    }

    /**
     * Sometimes selectionStart and selectionEnd return -1
     * for no apparent reason which throws OutOfBoundsException.
     * The function will make sure we don't end up with it.
     */
    private fun isLegalSelection(): Boolean = (selectionStart or selectionEnd) != -1

    /**
     * Implement this method to listen for word clicks by setting a:
     * [onWordClickedListener]
     * */
    interface OnWordClickedListener {
        fun onWordClick(word: String)
    }
}
