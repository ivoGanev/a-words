package com.ivo.ganev.awords.extensions

import android.text.Spannable
import android.text.style.ClickableSpan

/**
 * It's a mutating function that sets clickable span on each word it finds inside
 * the receiver [Spannable] excluding "space" characters.
 * */
fun Spannable.setClickableSpanToAllWords(clickableSpan: ()->ClickableSpan) {
    val noSpaceMatch = onlyCharacters()
    while (noSpaceMatch.find()) {
        setSpan(
            clickableSpan(),
            noSpaceMatch.start(),
            noSpaceMatch.end(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}