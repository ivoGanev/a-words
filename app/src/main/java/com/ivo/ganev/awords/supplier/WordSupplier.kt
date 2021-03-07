package com.ivo.ganev.awords.supplier

import android.content.Context
import com.ivo.ganev.awords.functional.Result

interface Payload {
    fun get(): Any
}

interface WordSupplier {
    fun process(
        context: Context,
        payload: Any,
        result: (Result<List<String>, Any>) -> Unit
    )
}