package com.ivo.ganev.awords

import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import com.ivo.ganev.awords.extensions.setClickableSpanToAllWords

/**
 * Creates a new [SpannableStringBuilder] and allows word replacement
 * with attaching clickable spans. Ideally this could be cached and only
 * words will be replaced
 * */
class SpannableStringBuilderUndoable(text: CharSequence) {
    private val stack: SnapshotStack<WordSelection> =
        SnapshotStack()
    private val builder = SpannableStringBuilder(text)

    /**
     * Replaces the word selection and records it for undo
     * */
    fun replaceAndRecordUndo(wordSelection: WordSelection) =
        replace(wordSelection) {
            it.takeSnapshot()
        }

    private fun replace(wordSelection: WordSelection, action: (WordSelection) -> Unit) =
        builder.apply {
            with(wordSelection) {
                action(this)
                replace(selectionStart, selectionEnd, word)
                clearSpans()
                setClickableSpanToAllWords { clickableSpan }
            }
        }

    private fun WordSelection.takeSnapshot() {
        val oldWord = builder.substring(selectionStart, selectionEnd)
        val oldSnapshot =
            WordSelection(clickableSpan, oldWord, selectionStart, selectionStart + word.length)
        stack.store(oldSnapshot)
    }

    fun undo() = stack.undo {
        replace(it.storedState()) {}
    }

    fun printStack() = stack.toString()


    override fun toString(): String {
        return builder.toString()
    }

    data class WordSelection(
        val clickableSpan: ClickableSpan,
        val word: String,
        val selectionStart: Int,
        val selectionEnd: Int
        // no need for coordinates because we replace at the clicked position
        // but when undone we need the coordinates of the previous
        // push the replaced word { Hello , 1, 5 }, pop
    ) : Snapshot<WordSelection> {

        override fun storedState(): WordSelection {
            return this
        }

        override fun toString(): String {
            return "WordSelection:: word: $word, start: $selectionStart, end: $selectionEnd"
        }
    }

}