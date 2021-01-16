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
                clickableSpan(), matcher.start(), matcher.end(),
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
        if (isLegalSelection()) {
            // TODO: Incredibly inefficient. Have to find a way to fix
            //       all annoying copying of Strings and SpannableStrings
            text = spannableStringBuilder.apply {
                replace(selectionStart, selectionEnd, replacement)
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
