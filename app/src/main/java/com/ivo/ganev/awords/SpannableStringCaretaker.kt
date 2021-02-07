package com.ivo.ganev.awords

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import com.ivo.ganev.awords.extensions.setClickableSpanToAllWords
import com.ivo.ganev.dp_state.DiscretePartitionedState
import timber.log.Timber
import java.lang.IndexOutOfBoundsException

/**
 * Creates a new [SpannableStringBuilder] with undo redo
 * functionality and allows word replacement with attaching
 * clickable spans. Ideally this could be cached and only
 * words will be replaced.
 * */
class SpannableStringCaretaker(text: CharSequence, val clickableSpan: () -> ClickableSpan) {
    private val buffer = SpannableStringBuilder(text)
    private val partitionedState = DiscretePartitionedState<WordState>()
    private var lastEditPosition = -1

    private var isTraversing = false

    init {
        buffer.setClickableSpanToAllWords { clickableSpan() }
        partitionedState.clear()
    }

    fun toSpannableStringBuilder() = buffer

    /**
     * Returns the replaced word as a selection
     * */
    fun replace(start: Int, end: Int, replacement: String): SpannableStringBuilder {
        if(isTraversing) {
            partitionedState.clear()
            isTraversing = false
            lastEditPosition = -1
        }

        val toReplace = buffer.substring(start, end)
        if (lastEditPosition != start) {
            partitionedState.push(WordState(toReplace, start))
        }
        partitionedState.add(WordState(replacement, start))
        lastEditPosition = start

        return buffer.replaceImpl(start, end, replacement)
    }

    private fun SpannableStringBuilder.replaceImpl(
        start: Int,
        end: Int,
        replacement: String,
    ): SpannableStringBuilder {
        return this.apply {
            replace(start, end, replacement)
            //setSpan(clickableSpan(), start, start + replacement.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            // TODO: Just set the current span instead of rebuilding the entire string span
            clearSpans()
            setClickableSpanToAllWords { clickableSpan() }
        }
    }


    fun undo(): SpannableStringBuilder {
        try {
            val current = partitionedState.current()
            val restored = partitionedState.left()
            val peek = partitionedState.peekRight()
            val end = pickEnd(restored, current, peek)

            buffer.replaceImpl(restored.start, end, restored.word)
        } catch (ex: IndexOutOfBoundsException) {
            Timber.e(ex)
        }
        isTraversing = true
        return buffer
    }

    fun redo(): SpannableStringBuilder {
        try {
            val current = partitionedState.current()
            val restored = partitionedState.right()
            val peek = partitionedState.peekLeft()
            val end = pickEnd(restored, current, peek)

            buffer.replaceImpl(restored.start, end, restored.word)
        } catch (ex: IndexOutOfBoundsException) {
            Timber.e(ex)
        }

        return buffer
    }

    private fun pickEnd(
        restored: WordState,
        current: WordState,
        peek: WordState
    ) = if (restored.start != current.start) {
        peek.start + peek.word.length
    } else {
        current.start + current.word.length
    }

    override fun toString(): String {
        return buffer.toString()
    }

    data class WordState(
        var word: String,
        var start: Int,
    ) {
        companion object {
            fun empty() = WordState(
                start = -1, word = ""
            )
        }

        override fun toString(): String {
            return "{ word: $word, start: $start }"
        }
    }


}