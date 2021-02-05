package com.ivo.ganev.awords

import android.text.SpannableStringBuilder
import com.ivo.ganev.dp_state.DiscretePartitionedState
import timber.log.Timber
import java.lang.IndexOutOfBoundsException

/**
 * Creates a new [SpannableStringBuilder] with undo redo
 * functionality and allows word replacement with attaching
 * clickable spans. Ideally this could be cached and only
 * words will be replaced.
 * */
class SpannableStringCaretaker(text: CharSequence) {
    private val buffer = SpannableStringBuilder(text)
    val partitionedState = DiscretePartitionedState<Word>()
    var lastEditPosition = -1

    /**
     * Returns the replaced word as a selection
     * */
    fun replace(start: Int, end: Int, replacement: String) {
        var nextRow = false
        try {
            partitionedState.current()
        } catch (ex: IndexOutOfBoundsException) {
            nextRow = true
        } finally {
            val toReplace = buffer.substring(start, end)

            nextRow = lastEditPosition != start
            if (nextRow) {
                partitionedState.push(Word(toReplace, start))
            }
        }
        partitionedState.add(Word(replacement, start))
        lastEditPosition = start

        buffer.apply {
            replace(start, end, replacement)
            clearSpans()
        }
    }

    fun undo(): SpannableStringBuilder {
        try {
            val current = partitionedState.current()
            val restored = partitionedState.left()
            val peek = partitionedState.peekRight()

            val end = pickEnd(restored, current, peek)

            buffer.apply {
                replace(restored.start, end, restored.word)
                clearSpans()
            }
        } catch (ex: IndexOutOfBoundsException) {
            Timber.e(ex)
        }

        return buffer
    }

    fun redo(): SpannableStringBuilder {
        try {
            val current = partitionedState.current()
            val restored = partitionedState.right()
            val peek = partitionedState.peekLeft()

            val end = pickEnd(restored, current, peek)

            buffer.apply {
                replace(restored.start, end, restored.word)
                clearSpans()
            }
        } catch (ex: IndexOutOfBoundsException) {
            Timber.e(ex)
        }

        return buffer
    }

    private fun pickEnd(
        restored: Word,
        current: Word,
        peek: Word
    ) = if (restored.start != current.start) {
        peek.start + peek.word.length
    } else {
        current.start + current.word.length
    }

    override fun toString(): String {
        return buffer.toString()
    }

    data class Word(
        var word: String,
        var start: Int,
    ) {
        companion object {
            fun empty() = Word(
                start = -1, word = ""
            )
        }

        override fun toString(): String {
            return "{ word: $word, start: $start }"
        }
    }
}