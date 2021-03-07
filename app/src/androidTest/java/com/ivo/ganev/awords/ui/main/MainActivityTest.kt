package com.ivo.ganev.awords.ui.main

import android.database.MatrixCursor
import org.junit.Test

class MainActivityTest {
    @Test
    fun something() {
        val cursor = MatrixCursor(arrayOf("One", "Two", "Three"))
        cursor.addRow(arrayOf("1", "2", "3"))
        println(cursor)
    }
}