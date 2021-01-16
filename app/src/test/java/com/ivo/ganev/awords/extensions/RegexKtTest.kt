package com.ivo.ganev.awords.extensions

import junit.framework.TestCase
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class RegexKtTest : TestCase() {

    @Test
    fun testMatchNoSpace() {
        val text = "Hello it's me, the elephant   ..."
        val match = text.matchNoSpace()
        val result = mutableListOf<String>()

        while (match.find()) {
            result.add(match.group())
        }

        result[0] shouldBeEqualTo "Hello"
        result[1] shouldBeEqualTo "it's"
        result[2] shouldBeEqualTo "me,"
        result[3] shouldBeEqualTo "the"
        result[4] shouldBeEqualTo "elephant"
        result[5] shouldBeEqualTo "..."
    }
}