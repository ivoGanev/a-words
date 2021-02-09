package com.ivo.ganev.awords.extensions

import android.content.Context

fun Context.openJsonAsset(fileName: String) : String {
    val inStream = this.assets.open(fileName)
    val json: String
    inStream.bufferedReader().use {
        json = it.readText()
    }
    return json
}