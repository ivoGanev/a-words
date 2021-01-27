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
import com.ivo.ganev.awords.Snapshot
import com.ivo.ganev.awords.SnapshotStack
import com.ivo.ganev.awords.extensions.setClickableSpanToAllWords

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

    val snapshotStack: SnapshotStack<Word> = SnapshotStack()

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
        val spannableString = SpannableString(text)
        spannableString.setClickableSpanToAllWords { clickableSpan() }

        // when you set the text = "something" you create a new copy of it in memory with the factory.
        // But what also happens is the Factory creates a new SpannableString() every time we call setText()
        // instead of creating it we cast the text(CharSequence) as Spannable and set the text to it.
        super.setText(spannableString)
    }


    fun undoReplaceWord() {

    }

    fun redoReplaceWord() {

    }

    /**
     * Replaces the selected word from the text view and adds a clickable span to it.
     * "Space" characters won't be included as in [setClickableText]
     * */
    fun replaceSelectedWord(replacement: String) {
        val spannableStringBuilder = SpannableStringBuilder(text)

        if (isLegalSelection()) {
            text = spannableStringBuilder.apply {
                replace(selectionStart, selectionEnd, replacement) // replace only the selected word string
                snapshotStack.push(Word(selectionStart, selectionEnd, replacement))
                clearSpans()
                // TODO: Possibly create a more efficient algorithm to replace the spans.
                //       Also I haven't checked the performance but for each span there is a clickableSpan object
                //       meaning that there could be 1000's of spans for a long text. For now it's not that
                //       important though.
                setClickableSpanToAllWords { clickableSpan() } // recreate all spans
            }
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

    data class Word(val selectionStart: Int,
                    val selectionEnd: Int,
                    val word: String) : Snapshot<Word> {
        override fun restore(): Word {
            return this
        }
    }


}
