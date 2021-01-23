package com.ivo.ganev.awords.extensions

import java.util.*

fun String.capitalizeFirstLetter() =
    this.substring(0, 1).capitalize(Locale.ROOT) + this.substring(1)

fun String.isFirstCharacterUpperCase() =
    this[0].isUpperCase()