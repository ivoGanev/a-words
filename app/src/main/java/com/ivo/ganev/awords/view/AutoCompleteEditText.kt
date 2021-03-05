package com.ivo.ganev.awords.view

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView


class AutoCompleteEditText : AppCompatMultiAutoCompleteTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        setTokenizer(SpaceTokenizer())
        threshold = 1
    }

    var onFilteredTextChangeListener: OnFilteredTextChangeListener? = null
    private val tokenizer = SpaceTokenizer()

    fun interface OnFilteredTextChangeListener {
        fun onFilteredTextChanged(word: String)
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        if (enoughToFilter()) {
            val tokenStart = tokenizer.findTokenStart(text.toString(), selectionEnd)
            val word = text.toString().substring(tokenStart, selectionEnd)
            onFilteredTextChangeListener?.onFilteredTextChanged(word)
        }
    }

    class SpaceTokenizer : Tokenizer {
        override fun findTokenStart(text: CharSequence, cursor: Int): Int {
            var i = cursor
            while (i > 0 && text[i - 1] != ' ')
                i--
            return i
        }

        override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
            var i = cursor
            val len = text.length
            while (i < len) {
                if (text[i] == ' ')
                    return i
                else i++
            }
            return len
        }

        override fun terminateToken(text: CharSequence): CharSequence {
            var i = text.length
            while (i > 0 && text[i - 1] == ' ')
                i--
            return if (i > 0 && text[i - 1] != ' ') {
                text
            } else {
                if (text is Spanned) {
                    val sp = SpannableString("$text ")
                    TextUtils.copySpansFrom(text, 0, text.length, Object::class.java, sp, 0)
                    sp
                } else {
                    "$text "
                }
            }
        }
    }

}