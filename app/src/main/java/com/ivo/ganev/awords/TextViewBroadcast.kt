package com.ivo.ganev.awords

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

class TextChangeBroadcast(val textViews: List<TextView>) : TextWatcher {

    var textChangeListener: OnTextChangeListener? = null

    init {
        for (e in textViews)
            e.addTextChangedListener(this)
    }

    fun interface OnTextChangeListener {
        fun onTextChange(text: CharSequence)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        text?.let { textChangeListener?.onTextChange(text) }
    }

    override fun afterTextChanged(s: Editable?) {
    }
}
