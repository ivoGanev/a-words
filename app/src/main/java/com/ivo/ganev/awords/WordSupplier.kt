@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords

import android.content.Context
import com.ivo.ganev.awords.RandomWordSupplier.Type.*
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

class DatamuseWordSupplierPayload(
    private val text: String,
    private val types: List<DatamuseWordSupplier.Type>
) :
    Payload {
    override fun get(): Pair<String, List<DatamuseWordSupplier.Type>> {
        return Pair(text, types)
    }
}

class RandomWordSupplierPayload(private val type: List<RandomWordSupplier.Type>) : Payload {
    override fun get(): List<RandomWordSupplier.Type> {
        return type
    }
}

interface PayloadsWordSupplier<T : Payload> {
    fun process(
        context: Context,
        payload: T,
        result: (Result<List<String>, Any>) -> Unit
    )
}

interface WordSupplier {
    fun process(result: (Result<List<String>, Any>) -> Unit)
}

class RandomWordSupplier(val coroutineScope: CoroutineScope) :
    PayloadsWordSupplier<RandomWordSupplierPayload> {
    enum class Type {
        NOUN,
        ADJECTIVE,
        ADVERB,
        VERB
    }

    private fun getAdjectives(context: Context): List<String> {
        val assetJsonMapper = AssetJsonLoader(context)
        val result = mutableListOf<String>()

        assetJsonMapper.adjectives().let {
            for (i in 0..10) {
                // TODO: This may include the same word twice
                val rnd = (0 until it.length()).random()
                result.add(it.getString(rnd))
            }
        }
        return result
    }

    override fun process(
        context: Context,
        payload: RandomWordSupplierPayload,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val merge = mutableListOf<String>()

        for (supplierType in payload.get()) {
            when (supplierType) {
                ADJECTIVE -> merge.addAll(getAdjectives(context))
                NOUN -> TODO()
                ADVERB -> TODO()
                VERB -> TODO()
            }
        }
        //TODO: figure out the failure
        result(Success(merge))
    }
}

class DatamuseWordSupplier(val coroutineScope: CoroutineScope) :
    PayloadsWordSupplier<DatamuseWordSupplierPayload> {
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

    override fun process(
        context: Context,
        payload: DatamuseWordSupplierPayload,
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

