package com.ivo.ganev.awords.extensions

import org.junit.Test

import org.junit.Assert.*
import java.lang.IndexOutOfBoundsException
import kotlin.test.assertFailsWith

class StringKtTest {

    @Test
    fun selectWord() {
        val w1 = "Big"
        val w2 = "beautiful"
        val w3 = "sentence."
        val s1 = " $w1 $w2 $w3 "

        assertEquals(w1, s1.selectWord(1))
        assertEquals(w2, s1.selectWord(5))
        assertEquals(w3, s1.selectWord(15))
        assertEquals("", s1.selectWord(0))
    }

    @Test
    fun test2() {
        val s2 = "b"

        assertEquals("b", s2.selectWord(0))
        assertFailsWith<IndexOutOfBoundsException> {
            s2.selectWord(1)
        }
    }
}