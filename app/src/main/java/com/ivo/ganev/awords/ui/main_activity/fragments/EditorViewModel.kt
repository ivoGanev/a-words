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

    @Suppress("unused")
    val remoteFailure: LiveData<RemoteFailure>
        get() = _remoteFailure

    /**
     * argument keys: "tags" - List<*> where it has to be Supplier type,
     * "word" - String, "word_picker_strategy" - WordPickerJSONStrategy
     * */
    fun query(context: Context, arguments: Map<String, Any>) {
        val (first, second) = arguments["tags"] as List<*>
        // TODO: In this current version datamuse and POS queries are separated and
        //       cannot be mixed. Fix that in the next version
        val datamuseTags = (first as List<*>).filterIsInstance<DatamuseWordSupplier.Type>()
        val posTags = (second as List<*>).filterIsInstance<POSWordSupplier.Type>()
        val supplier: WordSupplier
        val payload: Payload
        val word = arguments["word"] as String
        val strategy = arguments["word_picker_strategy"] as WordPickerJSONStrategy

        debug(posTags.size.toString())
        debug(datamuseTags.size.toString())
        debug(word)
        debug(strategy::class.java.toString())

        if (datamuseTags.isNotEmpty()) {
            supplier = datamuseWordSupplier
            payload = DatamuseWordSupplier.StandardPayload(word, datamuseTags)
        } else {
            supplier = randomWordSupplier
            payload = POSWordSupplier.StandardPayload(word, posTags, strategy)
        }

        supplier.process(context, payload) { result ->
            when (result) {
                is Result.Failure -> _remoteFailure.value = result.failure as RemoteFailure
                is Result.Success -> result.result.let { _wordResult.value = it }
            }
        }
    }
}
