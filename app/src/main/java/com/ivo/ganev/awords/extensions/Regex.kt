package com.ivo.ganev.awords.extensions

import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Matches all the characters excluding the "space" character.
 * Example: "The  big monkey!, " will return the matcher groups:
 * "The", "big" "monkey!,"
 * */
fun CharSequence.matchNoSpace() : Matcher {
    // exclude all space characters
    val regex: Pattern = Pattern.compile("[\\S]+")
    return regex.matcher(this)
}


/**
 * Matches only alphabet characters.
 * */
fun CharSequence.onlyCharacters() : Matcher {
    // exclude all space characters
    val regex: Pattern = Pattern.compile("[A-Za-z]+")
    return regex.matcher(this)
}