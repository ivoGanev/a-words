package com.ivo.ganev.awords.extensions

import java.util.*

fun String.capitalizeFirstLetter() =
    this.substring(0, 1).capitalize(Locale.ROOT) + this.substring(1)

fun String.isFirstCharacterUpperCase() =
    this[0].isUpperCase()

fun String.selectWord(cursor: Int): String {
    var c = cursor
    if (this[c] == ' ')
        return ""

    var start = 0
    var end = 0
    while (c > 0 && this[c - 1] != ' ') {
        c--
    }
    start = c

    while (c < this.length && this[c] != ' ') {
        c++
    }
    end = c
    return this.substring(start, end)
}