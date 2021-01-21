package com.ivo.ganev.awords.ui.main_activity.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivo.ganev.datamuse_kotlin.client.DatamuseKotlinClient
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.WordsEndpointBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.wordsBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint
import com.ivo.ganev.datamuse_kotlin.endpoint.words.hardConstraintsOf
import com.ivo.ganev.datamuse_kotlin.response.RemoteFailure
import com.ivo.ganev.datamuse_kotlin.response.WordResponse
import kotlinx.coroutines.launch

class EditorViewModel : ViewModel() {
    private val datamuseClient = DatamuseKotlinClient()

    lateinit var type: EditorViewModel.Type

    enum class Type {
        Synonyms,
        Antonyms,
        Rhymes
    }

    fun makeQuery(word: String, type: Type) = wordsBuilder {
        hardConstraints = when (type) {
            Type.Synonyms -> hardConstraintsOf(HardConstraint.MeansLike(word))
            Type.Antonyms -> hardConstraintsOf(HardConstraint.SoundsLike(word))
            Type.Rhymes -> hardConstraintsOf(HardConstraint.SoundsLike(word))
        }
        hardConstraints = hardConstraintsOf(HardConstraint.MeansLike(word))
        maxResults = 10
    }

    private val failure = MutableLiveData<RemoteFailure>()
    val results = MutableLiveData<Set<WordResponse>>()

    fun fireUpQuery(query: WordsEndpointBuilder) {
        viewModelScope.launch {
            val words = datamuseClient.query(query.build())
            words.applyEither({ it -> failure.postValue(it) }, { results.postValue(it) })
        }
    }
}
