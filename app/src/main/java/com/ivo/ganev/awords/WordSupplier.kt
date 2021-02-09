@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords

import com.ivo.ganev.awords.Result.Failure
import com.ivo.ganev.awords.Result.Success
import com.ivo.ganev.awords.WordSupplier.CreationConfig
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


interface WordSupplier {
    interface CreationConfig {
        fun get(): Any
    }

    fun getWords(
        creationConfig: List<CreationConfig>,
        result: (Result<List<String>, Any>) -> Unit
    )
}

class DatamuseWordSupplier(val coroutineScope: CoroutineScope) : WordSupplier {
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

    sealed class CreationConfig(val type: Type) : WordSupplier.CreationConfig {
        lateinit var word: String

        override fun get(): List<HardConstraint> = type.toHardConstraint(word)

        object Synonym : CreationConfig(Type.SYNONYMS)
        object Antonym : CreationConfig(Type.ANTONYMS)
        object Rhyme : CreationConfig(Type.RHYMES)
        object Homophones : CreationConfig(Type.HOMOPHONES)
        object PopularNouns : CreationConfig(Type.POPULAR_ADJECTIVES)
        object PopularAdjectives : CreationConfig(Type.POPULAR_NOUNS)
    }

    override fun getWords(
        creationConfig: List<WordSupplier.CreationConfig>,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val configuredQueries = creationConfig.filterIsInstance<CreationConfig>()
            .map {
                wordsBuilder {
                    hardConstraints = it.get()
                    maxResults = 5
                }
            }

        val merge = mutableListOf<String>()

        coroutineScope.launch {
            val queries = configuredQueries.map { queryAsync(it).await() }
            for (i in queries.indices) {
                queries[i].applyEither({
                    result(Failure(it))
                }, {
                    merge.addAll(it.toWordList())
                })
                if(i==queries.size-1)
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
