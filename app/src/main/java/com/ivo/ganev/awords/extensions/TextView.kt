package com.ivo.ganev.awords.extensions

import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

/**
 * It's a mutating function that sets clickable span on each word it finds inside
 * the receiver [Spannable] excluding "space" characters.
 * */
fun Spannable.setClickableSpanToAllWords(clickableSpan: ()->ClickableSpan) {
    val noSpaceMatch = matchNoSpace()
    while (noSpaceMatch.find()) {
        setSpan(
            clickableSpan(),
            noSpaceMatch.start(),
            noSpaceMatch.end(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}
