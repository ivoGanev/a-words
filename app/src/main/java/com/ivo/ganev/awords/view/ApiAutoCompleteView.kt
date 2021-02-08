package com.ivo.ganev.awords.view

import android.content.Context
import android.text.TextUtils
import android.text.method.QwertyKeyListener
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import com.ivo.ganev.awords.FileSaver
import com.ivo.ganev.awords.SpaceTokenizer

class ApiAutoCompleteView : AppCompatMultiAutoCompleteTextView{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var showDropdown: Boolean = false
    private val tokenizer = SpaceTokenizer()

    var fileSave: FileSaver? = null

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun replaceText(text: CharSequence) {
        clearComposingText()

        val end: Int = tokenizer.findTokenEnd(getText(), selectionEnd)
        val start: Int = tokenizer.findTokenStart(getText(), end)

        val editable = getText()
        val original = TextUtils.substring(editable, start, end)

        QwertyKeyListener.markAsReplaced(editable, start, end, original)
        editable.replace(start, end, tokenizer.terminateToken(text))
    }


    override fun performFiltering(text: CharSequence, keyCode: Int) {
        if (enoughToFilter()) {
            val end: Int = tokenizer.findTokenEnd(getText(), selectionEnd)
            val start: Int = tokenizer.findTokenStart(text, end)
            // this displays the popup again
            performFiltering(text, start, end, keyCode)
        } else {
            dismissDropDown()
            val f = filter
            f?.filter(null)
        }
    }

    fun disableDropDown() {
        showDropdown = false
    }
}
