package com.ivo.ganev.awords.ui.main_activity

import android.database.MatrixCursor
import org.junit.Assert.*
import org.junit.Test

class MainActivityTest {
    @Test
    fun something() {
        val cursor = MatrixCursor(arrayOf("One", "Two", "Three"))
        cursor.addRow(arrayOf("1", "2", "3"))
        println(cursor)
    }
}