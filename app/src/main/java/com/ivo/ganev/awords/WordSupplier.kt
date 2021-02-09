@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords

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


interface WordSupplier {
    interface CreationConfig {
        fun get(): Any
    }

    fun getWords(
        creationConfig: CreationConfig,
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
        creationConfig: WordSupplier.CreationConfig,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        creationConfig as CreationConfig

        val wordsQuery = wordsBuilder {
            hardConstraints = creationConfig.get()
            maxResults = 10
        }

        coroutineScope.launch {
            val query = queryAsync(wordsQuery).await()

            query.applyEither({
                result(Failure(it))
            }, {
                result(Success(it.toWordList()))
            })
        }
    }

    private fun Set<WordResponse>.toWordList() = this.flatMap { it.elements }
        .filterIsInstance<WordResponse.Element.Word>()
        .map { it.word }


    private fun queryAsync(query: WordsEndpointBuilder) =
        coroutineScope.async { datamuseClient.query(query.build()) }
}
