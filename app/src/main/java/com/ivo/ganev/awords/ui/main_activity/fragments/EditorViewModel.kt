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

    fun query(context: Context, payload: Payload) {
        when(payload) {
            is POSWordSupplier.StandardPayload -> processWordSupplier(context, randomWordSupplier, payload)
            is DatamuseWordSupplier.StandardPayload -> processWordSupplier(context, datamuseWordSupplier, payload)
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
