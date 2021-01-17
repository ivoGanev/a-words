package com.ivo.ganev.awords.extensions

import android.text.Spannable
import android.text.style.ClickableSpan

/**
 * Creates clickable span on the entire spannable excluding
 * "space" characters
 * */
fun Spannable.toNoSpaceClickableSpan(clickableSpan: ()-> ClickableSpan) {
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