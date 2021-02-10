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
 * Filters out the tag <T> of the selected views if it is present.
 * */
inline fun <reified T> Sequence<View>.filterTickedCheckboxWithTag(): List<T> =
    this.filterIsInstance<CheckBox>()
        .filter { it.isChecked }
        .map { it.tag as T }
        .toList()

