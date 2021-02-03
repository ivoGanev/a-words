package com.ivo.ganev.awords

import DiscretePartitionedState
import android.graphics.DiscretePathEffect
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.util.Log
import com.ivo.ganev.awords.extensions.setClickableSpanToAllWords

/**
 * Creates a new [SpannableStringBuilder] with undo redo
 * functionality and allows word replacement with attaching
 * clickable spans. Ideally this could be cached and only
 * words will be replaced.
 * */
class SpannableStringCaretaker(text: CharSequence) {
    private val builder = SpannableStringBuilder(text)
    val partitionedState = DiscretePartitionedState<String>()

    /**
     * Returns the replaced word as a selection
     * */
    fun replace(start: Int, end: Int, word: String, span: ClickableSpan?) {
        builder.apply {
            replace(start, end, word)
            clearSpans()
            if (span != null)
                setClickableSpanToAllWords { span }
        }
    }

    fun undo(): SpannableStringBuilder {
        return builder
    }

    fun redo(): SpannableStringBuilder {
        return builder
    }

    override fun toString(): String {
        return builder.toString()
    }

    data class Word(
        var clickableSpan: ClickableSpan?,
        var start: Int,
        var word: String
    ) {
        companion object {
            fun empty() = Word(
                clickableSpan = null,
                start = -1, word = ""
            )
        }
    }
}