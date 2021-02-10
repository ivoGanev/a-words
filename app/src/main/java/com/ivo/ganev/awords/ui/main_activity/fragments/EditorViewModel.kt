@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivo.ganev.awords.AssetJsonLoader
import com.ivo.ganev.awords.DatamuseWordSupplier
import com.ivo.ganev.awords.Result
import com.ivo.ganev.datamuse_kotlin.response.RemoteFailure

class EditorViewModel : ViewModel() {
    private val datamuseWordSupplier = DatamuseWordSupplier(viewModelScope)

    private val _wordResult = MutableLiveData<List<String>>()
    val wordResult: LiveData<List<String>>
        get() = _wordResult

    private val _failure = MutableLiveData<RemoteFailure>()
    val failure: LiveData<RemoteFailure>
        get() = _failure

    enum class RandomType {
        Noun,
        Adjective,
        Adverb,
        Verb
    }

    fun queryRandom(context: Context, randomWordTypes: List<RandomType>) {
        val assetJsonMapper = AssetJsonLoader(context)
        val words = mutableListOf<String>()

        for (t in randomWordTypes) {
            when (t) {
                RandomType.Adjective -> assetJsonMapper.adjectives().let {
                    for (i in 0..10) {
                        // TODO: This may include the same word twice
                        val rnd = (0..it.length()).random()
                        words.add(it.getString(rnd))
                    }
                }
                RandomType.Noun -> TODO()
                RandomType.Adverb -> TODO()
                RandomType.Verb -> TODO()
            }
        }

        _wordResult.set(words)
    }

    fun query(word: String, config: List<DatamuseWordSupplier.Type>) {
        datamuseWordSupplier.process(DatamuseWordSupplier.DatamusePayload(word, config)) { result ->
            when (result) {
                is Result.Failure -> _failure.value = result.failure as RemoteFailure
                is Result.Success -> _wordResult.value = result.result
            }
        }
    }

    private fun MutableLiveData<List<String>>.set(list: List<String>) {
        value = list
    }
}
