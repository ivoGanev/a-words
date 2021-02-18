@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords

import android.content.Context
import com.ivo.ganev.awords.POSWordSupplier.StandardPayload
import com.ivo.ganev.awords.Result.Failure
import com.ivo.ganev.awords.Result.Success
import com.ivo.ganev.awords.extensions.openJsonAsset
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
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber.d as debug


interface Payload {
    fun get(): Any
}

interface PayloadsWordSupplier<T : Payload> {
    fun process(
        context: Context,
        payload: T,
        result: (Result<List<String>, Any>) -> Unit
    )
}

interface WordPickerJSONStrategy {
    fun pick(array: JSONArray, extraArgs: Any?): List<String>
}

class WordPickerJSONStrategyContainsName : WordPickerJSONStrategy {
    override fun pick(array: JSONArray, extraArgs: Any?): List<String> {
        val result = mutableListOf<String>()

        if (extraArgs is String) {
            array.let {
                // TODO: Find an efficient way to fetch the words
                for (i in 0 until it.length()) {
                    if (it.getString(i).contains(extraArgs))
                        result.add(it.getString(i))
                }
            }
            debug(result.count().toString())
        }
        return result
    }
}

class WordPickerJSONStrategyRandom : WordPickerJSONStrategy {
    override fun pick(array: JSONArray, extraArgs: Any?): List<String> {
        val result = mutableListOf<String>()
        val randomWordCount = (0..10)

        array.let {
            // TODO: This may include the same word twice
            for (i in randomWordCount)
                result.add(it.getString((0 until it.length()).random()))
        }
        return result
    }
}

class POSWordSupplier(val coroutineScope: CoroutineScope) :
    PayloadsWordSupplier<StandardPayload> {

    class StandardPayload(
        val word: String? = null,
        val type: List<Type>,
        val wordPickerJSONStrategy: WordPickerJSONStrategy
    ) : Payload {
        override fun get() = this
    }

    enum class Type {
        NOUN {
            override val fileName: String
                get() = "nouns.json"
            override val jsonArrayName: String
                get() = "nouns"
        },
        ADJECTIVE {
            override val fileName: String
                get() = "adjs.json"
            override val jsonArrayName: String
                get() = "adjs"
        },
        ADVERB {
            override val fileName: String
                get() = "adverbs.json"
            override val jsonArrayName: String
                get() = "adverbs"
        },
        VERB {
            override val fileName: String
                get() = "verbs.json"
            override val jsonArrayName: String
                get() = "verbs"
        };

        abstract val fileName: String
        abstract val jsonArrayName: String
    }

    override fun process(
        context: Context,
        payload: StandardPayload,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val merge = mutableListOf<String>()

        // TODO: Make a picking process for the autocomplete. If user writes 'a'
        //  a word selector will pick a list with all the words starting with 'a'
        //  and a word filter should choose which words will remain. For now a
        //  filter with randomized fashion will suffice.
        for (wordType in payload.type) {
            val json = context.openJsonAsset(wordType.fileName)
            val wordArray = JSONObject(json).getJSONArray(wordType.jsonArrayName)
            val words = payload.wordPickerJSONStrategy.pick(wordArray, payload.word)
            merge.addAll(words)
        }

        //TODO: figure out when the code would fail and emit a Failure()
        result(Success(merge))
    }
}

class DatamuseWordSupplier(val coroutineScope: CoroutineScope) :
    PayloadsWordSupplier<DatamuseWordSupplier.StandardPayload> {

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
        payload: StandardPayload,
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

