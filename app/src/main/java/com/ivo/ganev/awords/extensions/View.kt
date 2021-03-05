package com.ivo.ganev.awords.extensions

import android.view.View
import android.widget.CheckBox

/**
 * Checks if a given id is matched with the view's id
 * */
infix fun View?.isWithId(id: Int): Boolean {
    return this?.id == id
}

/**
 * Filters out only checked boxes, and maps the sequence as tag <T>
 * */
inline fun <reified T> Sequence<View>.filterTickedCheckboxWithTag(): List<T> =
    this.filterIsInstance<CheckBox>()
        .filter { it.isChecked }
        .map { it.tag as T }
        .toList()

