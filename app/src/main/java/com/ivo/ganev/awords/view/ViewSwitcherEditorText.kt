package com.ivo.ganev.awords.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewSwitcher


class ViewSwitcherEditorText : ViewSwitcher {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    val copyText: View.(View) -> Unit =  {
        when {
            // copying text like this..yeah I know. Unfortunately setting PrecomputedText
            // could be done only to the TextView but not the EditText as far as it seems.
            (this is TextView && it is TextViewWordMutator) -> it.setClickableText(text)
            (this is TextView) && (it is TextView) -> it.text = text
        }
    }

    override fun setDisplayedChild(whichChild: Int) {
        if (whichChild != displayedChild) {
            currentView.copyText(nextView)
            super.setDisplayedChild(whichChild)
        }
    }

    fun setText(text: String) {
        val v = currentView
        if (v is TextViewWordMutator) v.setClickableText(text)
        else if (v is EditText) v.setText(text)
    }

    fun getText() : String {
        return (currentView as TextView).text.toString()
    }
}
