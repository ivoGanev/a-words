package com.ivo.ganev.awords.supplier

import android.content.Context
import com.ivo.ganev.awords.functional.Result
import com.ivo.ganev.datamuse_kotlin.client.DatamuseKotlinClient
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.WordsEndpointBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.wordsBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint
import com.ivo.ganev.datamuse_kotlin.endpoint.words.hardConstraintsOf
import com.ivo.ganev.datamuse_kotlin.response.WordResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DatamuseWordSupplier(val coroutineScope: CoroutineScope) :
    WordSupplier {

    class StandardPayload(
        private val text: String,
        private val types: List<Type>
    ) :
        Payload {
        override fun get(): Pair<String, List<Type>> {
            return Pair(text, types)
        }
    }

    private val datamuseClient = DatamuseKotlinClient()

    enum class Type {
        SYNONYMS {
            override fun toHardConstraint(word: String) =
                relatedWord(HardConstraint.RelatedWords.Code.SYNONYMS, word)
        },
        ANTONYMS {
            override fun toHardConstraint(word: String) =
                relatedWord(HardConstraint.RelatedWords.Code.ANTONYMS, word)
        },
        RHYMES {
            override fun toHardConstraint(word: String) =
                relatedWord(HardConstraint.RelatedWords.Code.RHYMES, word)
        },
        HOMOPHONES {
            override fun toHardConstraint(word: String) =
                relatedWord(HardConstraint.RelatedWords.Code.HOMOPHONES, word)
        },
        POPULAR_ADJECTIVES {
            override fun toHardConstraint(word: String) =
                relatedWord(HardConstraint.RelatedWords.Code.POPULAR_ADJECTIVES, word)
        },
        POPULAR_NOUNS {
            override fun toHardConstraint(word: String) =
                relatedWord(HardConstraint.RelatedWords.Code.POPULAR_NOUNS, word)
        };

        abstract fun toHardConstraint(word: String): List<HardConstraint>

        fun relatedWord(code: HardConstraint.RelatedWords.Code, word: String) =
            hardConstraintsOf(HardConstraint.RelatedWords(code, word))
    }

    private fun Set<WordResponse>.toWordList() = this.flatMap { it.elements }
        .filterIsInstance<WordResponse.Element.Word>()
        .map { it.word }


    private fun queryAsync(query: WordsEndpointBuilder) =
        coroutineScope.async { datamuseClient.query(query.build()) }

    override fun process(
        context: Context,
        payload: Any,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val standardPayload = payload as StandardPayload
        val supportedTypes = standardPayload.get().second
        val typeConfiguredQueries = supportedTypes.map {
            wordsBuilder {
                hardConstraints = it.toHardConstraint(standardPayload.get().first)
                maxResults = 5
            }
        }

        val merge = mutableListOf<String>()

        coroutineScope.launch {
            val queries = typeConfiguredQueries.map { queryAsync(it).await() }
            for (i in queries.indices) {
                queries[i].applyEither({
                    result(Result.Failure(it))
                }, {
                    merge.addAll(it.toWordList())
                })
                if (i == queries.size - 1)
                    result(Result.Success(merge))
            }
        }
    }
}

