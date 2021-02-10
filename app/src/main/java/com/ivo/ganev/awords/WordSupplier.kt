@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords

import android.view.View
import android.widget.CheckBox
import androidx.core.view.children
import com.ivo.ganev.awords.Result.Failure
import com.ivo.ganev.awords.Result.Success
import com.ivo.ganev.datamuse_kotlin.client.DatamuseKotlinClient
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.WordsEndpointBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.wordsBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.RelatedWords
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.RelatedWords.*
import com.ivo.ganev.datamuse_kotlin.endpoint.words.hardConstraintsOf
import com.ivo.ganev.datamuse_kotlin.response.WordResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


interface Payload {
    fun get(): Any
}

interface PayloadsWordSupplier<T : Payload> {
    fun process(payload: T, result: (Result<List<String>, Any>) -> Unit)
}

interface WordSupplier {
    fun process(result: (Result<List<String>, Any>) -> Unit)
}

class DatamuseWordSupplier(val coroutineScope: CoroutineScope) :
    PayloadsWordSupplier<DatamuseWordSupplier.DatamusePayload> {
    private val datamuseClient = DatamuseKotlinClient()

    enum class Type {
        SYNONYMS {
            override fun toHardConstraint(word: String) =
                relatedWord(Code.SYNONYMS, word)
        },
        ANTONYMS {
            override fun toHardConstraint(word: String) =
                relatedWord(Code.ANTONYMS, word)
        },
        RHYMES {
            override fun toHardConstraint(word: String) =
                relatedWord(Code.RHYMES, word)
        },
        HOMOPHONES {
            override fun toHardConstraint(word: String) =
                relatedWord(Code.HOMOPHONES, word)
        },
        POPULAR_ADJECTIVES {
            override fun toHardConstraint(word: String) =
                relatedWord(Code.POPULAR_ADJECTIVES, word)
        },
        POPULAR_NOUNS {
            override fun toHardConstraint(word: String) =
                relatedWord(Code.POPULAR_NOUNS, word)
        };

        abstract fun toHardConstraint(word: String): List<HardConstraint>

        fun relatedWord(code: Code, word: String) =
            hardConstraintsOf(RelatedWords(code, word))
    }

    class DatamusePayload(private val text: String, private val types: List<Type>) : Payload {
        override fun get(): Pair<String, List<Type>> {
            return Pair(text, types)
        }
    }

    override fun process(
        payload: DatamusePayload,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val supportedTypes = payload.get().second
        val typeConfiguredQueries = supportedTypes.map {
            wordsBuilder {
                hardConstraints = it.toHardConstraint(payload.get().first)
                maxResults = 5
            }
        }

        val merge = mutableListOf<String>()

        coroutineScope.launch {
            val queries = typeConfiguredQueries.map { queryAsync(it).await() }
            for (i in queries.indices) {
                queries[i].applyEither({
                    result(Failure(it))
                }, {
                    merge.addAll(it.toWordList())
                })
                if (i == queries.size - 1)
                    result(Success(merge))
            }
        }
    }

    private fun Set<WordResponse>.toWordList() = this.flatMap { it.elements }
        .filterIsInstance<WordResponse.Element.Word>()
        .map { it.word }


    private fun queryAsync(query: WordsEndpointBuilder) =
        coroutineScope.async { datamuseClient.query(query.build()) }
}
