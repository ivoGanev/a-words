package com.ivo.ganev.awords

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.widget.MultiAutoCompleteTextView

class SpaceTokenizer : MultiAutoCompleteTextView.Tokenizer {
   override fun findTokenStart(text: CharSequence, cursor: Int): Int {
        var i = cursor
        while (i > 0 && text[i - 1] != ' ')
            i--
        return i
    }

    override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
        var i = cursor
        var len = text.length
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
        if (i > 0 && text[i - 1] != ' ') {
            return text
        } else {
            return if(text is Spanned) {
                val sp = SpannableString("$text ")
                TextUtils.copySpansFrom(text, 0, text.length, Object::class.java, sp, 0)
                sp
            } else {
                "$text "
            }
        }
    }
}