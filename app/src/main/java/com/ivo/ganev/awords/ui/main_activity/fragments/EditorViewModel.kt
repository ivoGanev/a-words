@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivo.ganev.awords.*
import com.ivo.ganev.datamuse_kotlin.response.RemoteFailure
import timber.log.Timber.d as debug

class EditorViewModel : ViewModel() {
    private val datamuseWordSupplier = DatamuseWordSupplier(viewModelScope)
    private val randomWordSupplier = POSWordSupplier(viewModelScope)

    private val _wordResult = MutableLiveData<List<String>>()
    val wordResult: LiveData<List<String>>
        get() = _wordResult

    private val _remoteFailure = MutableLiveData<RemoteFailure>()
    val remoteFailure: LiveData<RemoteFailure>
        get() = _remoteFailure

    fun query(context: Context, word: String, wordType: List<Any>) {
        // TODO: partition the list by types but allow only 1 type of selection
        //  otherwise produce a Failure
        val datamuseWordType = wordType.filterIsInstance<DatamuseWordSupplier.Type>()
        if(datamuseWordType.isNotEmpty()) {
            val payload = DatamuseWordSupplier.StandardPayload(word, datamuseWordType)
            processWordSupplier(context, datamuseWordSupplier, payload)
        }
        val POSWordType = wordType.filterIsInstance<POSWordSupplier.Type>()
        debug(POSWordType.size.toString())
        if(POSWordType.isNotEmpty()) {
            val payload = POSWordSupplier.StandardPayload(word, POSWordType)
            processWordSupplier(context, randomWordSupplier, payload)
        }
    }

    private fun <T : Payload> processWordSupplier(
        context: Context,
        supplier: PayloadsWordSupplier<T>,
        payload: T
    ) {
        supplier.process(context, payload) { result ->
            when (result) {
                is Result.Failure -> _remoteFailure.value = result.failure as RemoteFailure
                is Result.Success -> result.result.let { _wordResult.value = it }
            }
        }
    }
}
