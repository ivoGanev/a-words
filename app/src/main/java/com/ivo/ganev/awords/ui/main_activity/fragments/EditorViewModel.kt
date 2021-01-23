package com.ivo.ganev.awords.ui.main_activity.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivo.ganev.datamuse_kotlin.client.DatamuseKotlinClient
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.wordsBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.*
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.RelatedWords.Code.*
import com.ivo.ganev.datamuse_kotlin.endpoint.words.hardConstraintsOf
import com.ivo.ganev.datamuse_kotlin.response.RemoteFailure
import com.ivo.ganev.datamuse_kotlin.response.WordResponse
import kotlinx.coroutines.launch

class EditorViewModel : ViewModel() {
    private val datamuseClient = DatamuseKotlinClient()

    private val _results = MutableLiveData<Set<WordResponse>>()
    val results: LiveData<Set<WordResponse>>
        get() = _results

    private val _failure = MutableLiveData<RemoteFailure>()
    val failure: LiveData<RemoteFailure>
        get() = _failure

    enum class RadioGroupType {
        Synonyms {
            override fun toConstraint(word: String): List<HardConstraint> {
                return hardConstraintsOf(RelatedWords(SYNONYMS, word))
            }
        },
        Antonyms {
            override fun toConstraint(word: String): List<HardConstraint> {
                return hardConstraintsOf(RelatedWords(ANTONYMS, word))
            }
        },
        Rhymes {
            override fun toConstraint(word: String): List<HardConstraint> {
                return hardConstraintsOf(RelatedWords(RHYMES, word))
            }
        };

        abstract fun toConstraint(word: String): List<HardConstraint>
    }

    /**
     * Will query Datamuse and update [results] or [failure] accordingly.
     * */
    fun query(word: String, radioGroupType: RadioGroupType) {
        val query = wordsBuilder {
            hardConstraints = radioGroupType.toConstraint(word)
        }
        println(query.build().toUrl())
        viewModelScope.launch {
            val words = datamuseClient.query(query.build())
            words.applyEither({ _failure.postValue(it) }, { _results.postValue(it) })
        }
    }
}
