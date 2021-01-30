package com.ivo.ganev.awords

import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import androidx.core.text.getSpans
import com.ivo.ganev.awords.extensions.setClickableSpanToAllWords

/**
 * Creates a new [SpannableStringBuilder] with undo redo
 * functionality and allows word replacement with attaching
 * clickable spans. Ideally this could be cached and only
 * words will be replaced.
 * */
class SpannableStringCaretaker(text: CharSequence) {
    private val stack: SnapshotStack<WordSelection> =
        SnapshotStack()
    private val builder = SpannableStringBuilder(text)

    fun printStack() = stack.toString()

    /**
     * Replaces the word selection and records it for undo
     * */
    fun replaceAndStore(wordSelection: WordSelection) {
        with(wordSelection) {
           storeWord(start, start + word.length, start, end).takeSnapshot()
           replace(WordSelection(clickableSpan, word, start, end))
        }
    }

    /**
     * Returns the replaced word as a selection
     * */
    fun replace(wordSelection: WordSelection) {
        builder.apply {
            with(wordSelection) {
                replace(start, end, word)
                clearSpans()
                if (clickableSpan != null)
                    setClickableSpanToAllWords { clickableSpan }
            }
        }
    }

    private fun storeWord(start: Int, end: Int, selectionStart: Int, selectionEnd: Int): WordSelection {
        val word = builder.substring(selectionStart, selectionEnd)
        val span = builder.getSpans<ClickableSpan>(start, end).firstOrNull()
        return WordSelection(span, word, start, end)
    }

    private fun WordSelection.takeSnapshot() {
        stack.store(this)
    }

    fun undo(): SpannableStringBuilder {
        stack.undo {
            replace(it.storedState())
        }
        return builder
    }

    fun redo(): SpannableStringBuilder {
        stack.redo {
            replace(it.storedState())
        }
        return builder
    }

    override fun toString(): String {
        return builder.toString()
    }

    data class WordSelection(
        val clickableSpan: ClickableSpan?,
        val word: String,
        val start: Int,
        val end: Int
        // no need for coordinates because we replace at the clicked position
        // but when undone we need the coordinates of the previous
        // push the replaced word { Hello , 1, 5 }, pop
    ) : Snapshot<WordSelection> {

        override fun storedState(): WordSelection {
            return this
        }

        override fun toString(): String {
            return "WordSelection:: word: $word, start: $start, end: $end"
        }
    }

}