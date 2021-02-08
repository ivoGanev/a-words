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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditorViewModel : ViewModel() {
    private val datamuseClient = DatamuseKotlinClient()

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

    enum class DatamuseType {
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
        },
        Homophones {
            override fun toConstraint(word: String): List<HardConstraint> {
                return hardConstraintsOf(RelatedWords(HOMOPHONES, word))
            }
        },
        PopularAdjectives {
            override fun toConstraint(word: String): List<HardConstraint> {
                return hardConstraintsOf(RelatedWords(POPULAR_ADJECTIVES, word))
            }
        },
        PopularNoun {
            override fun toConstraint(word: String): List<HardConstraint> {
                return hardConstraintsOf(RelatedWords(POPULAR_NOUNS, word))
            }
        };

        abstract fun toConstraint(word: String): List<HardConstraint>
    }

    /**
     * Will query Datamuse and update [word] or [failure] accordingly.
     * */
    fun query(word: String, datamuseType: List<DatamuseType>) {
        val typeList = datamuseType.map {
            wordsBuilder {
                hardConstraints = it.toConstraint(word)
                maxResults = 10
            }
        }

        datamuseType.forEach {
            println(datamuseType)
        }

        viewModelScope.launch {
            val result = typeList.map {
                val result = async {
                    datamuseClient.query(it.build())
                }
                result.await()
            }

            val allResults = mutableListOf<String>()

            for (r in result) {
                val wordResponseToList: (Set<WordResponse>) -> List<String> = { wordResponse ->
                    wordResponse
                        .flatMap { it.elements }
                        .filterIsInstance<WordResponse.Element.Word>()
                        .map { it.word }
                }

                r.applyEither(
                    { _failure.postValue(it) },
                    { allResults.addAll(wordResponseToList(it)) })
            }

            println(":::::$allResults")
            _wordResult.set(allResults)
        }

            //val words = datamuseClient.query(query.build())
//            val wordResponseToList: (Set<WordResponse>) -> List<String> = { wordResponse ->
//                wordResponse
//                    .flatMap { it.elements }
//                    .filterIsInstance<WordResponse.Element.Word>()
//                    .map { it.word }
//            }
//            words.applyEither(
//                { _failure.postValue(it) },
//                { _wordResult.set(wordResponseToList(it)) })
        }
    }

    private fun MutableLiveData<List<String>>.set(list: List<String>) {
        value = list
    }

