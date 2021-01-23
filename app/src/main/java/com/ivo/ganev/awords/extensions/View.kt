package com.ivo.ganev.awords.extensions

import android.view.View

/**
 * Checks if a given id is matched with the view's id
 * */
infix fun View?.isWithId(id: Int): Boolean {
    return this?.id == id
}
