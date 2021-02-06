package com.ivo.ganev.awords.view

import android.content.Context
import android.graphics.Color
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.ivo.ganev.awords.SpannableStringCaretaker
import com.ivo.ganev.awords.SpannableStringCaretaker.*

/**
 * A clickable text view which assists in transformation(mutation) of separate words within
 * the provided text. Use [setClickableText] to assign a brand new text and then
 * attach to [onWordClickedListener] to listen when a word gets clicked.
 *
 * You can also replace the selected word with [replaceSelectedWord].
 * */
class TextViewWordMutator :
    AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    lateinit var caretaker: SpannableStringCaretaker

    /**
     * This listener will be notified when any part of the TextView's
     * text gets clicked.
     * */
    var onWordClickedListener: OnWordClickedListener? = null

    private val spannableFactory = object : Spannable.Factory() {
        override fun newSpannable(source: CharSequence?): Spannable {
            return source as Spannable
        }
    }

    init {
        movementMethod = LinkMovementMethod.getInstance()
        setText(text, BufferType.SPANNABLE)
        setSpannableFactory(spannableFactory)
        setClickableText(text)
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

    /**
     * Sets the text and ads a clickable span excluding "space" characters, meaning
     * that only words will be clickable.
     * */
    fun setClickableText(text: CharSequence) {
        caretaker = SpannableStringCaretaker(text) { clickableSpan() }
        this.text = caretaker.toSpannableStringBuilder()
    }


    fun redo() {
        text = caretaker.redo()
    }

    fun undo() {
        text = caretaker.undo()
    }

    private fun replaceWord(start: Int, end: Int, replacement: String) {
        text = caretaker.replace(start, end, replacement)
    }

    /**
     * Replaces the selected word from the text view and adds a clickable span to it.
     * "Space" characters won't be included as in [setClickableText]
     * */
    fun replaceSelectedWord(replacement: String) {
        if (isLegalSelection()) {
            replaceWord(selectionStart, selectionEnd, replacement)
        }
    }

    /**
     * Sometimes selectionStart and selectionEnd return -1
     * for no apparent reason which throws OutOfBoundsException.
     * The function will make sure we don't end up with it.
     */
    private fun isLegalSelection(): Boolean = (selectionStart or selectionEnd) != -1

    /**
     * Implement this method to listen for word clicks by setting an
     * [onWordClickedListener]
     * */
    interface OnWordClickedListener {
        fun onWordClick(word: String)
    }
}
