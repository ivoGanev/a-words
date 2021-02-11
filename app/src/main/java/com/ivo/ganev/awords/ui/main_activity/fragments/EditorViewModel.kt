@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivo.ganev.awords.*
import com.ivo.ganev.datamuse_kotlin.response.RemoteFailure

class EditorViewModel : ViewModel() {
    private val datamuseWordSupplier = DatamuseWordSupplier(viewModelScope)
    private val randomWordSupplier = RandomWordSupplier(viewModelScope)

    private val _wordResult = MutableLiveData<List<String>>()
    val wordResult: LiveData<List<String>>
        get() = _wordResult

    private val _failure = MutableLiveData<RemoteFailure>()
    val failure: LiveData<RemoteFailure>
        get() = _failure


    fun queryRandom(context: Context, queryType: List<RandomWordSupplier.Type>) {
        val payload = RandomWordSupplierPayload(queryType)
        processWordSupplier(context, randomWordSupplier, payload)
    }

    fun query(context: Context, word: String, queryType: List<DatamuseWordSupplier.Type>) {
        val payload = DatamuseWordSupplierPayload(word, queryType)
        processWordSupplier(context, datamuseWordSupplier, payload)
    }

    private fun <T : Payload> processWordSupplier(
        context: Context,
        supplier: PayloadsWordSupplier<T>,
        payload: T
    ) {
        supplier.process(context, payload) { result ->
            when (result) {
                is Result.Failure -> _failure.value = result.failure as RemoteFailure
                is Result.Success -> result.result.let { _wordResult.value = it }
            }
        }
    }
}
