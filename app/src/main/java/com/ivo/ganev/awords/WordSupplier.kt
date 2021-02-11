@file:Suppress("SpellCheckingInspection")

package com.ivo.ganev.awords

import android.content.Context
import com.ivo.ganev.awords.POSWordSupplier.ClassPayload
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
import org.json.JSONObject


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

interface WordSupplier {
    fun process(result: (Result<List<String>, Any>) -> Unit)
}

class POSWordSupplier(val coroutineScope: CoroutineScope) :
    PayloadsWordSupplier<ClassPayload> {

    class ClassPayload(private val type: List<Type>) : Payload {
        override fun get(): List<Type> = type
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

    private fun getRandomWordsByType(context: Context, type: Type): List<String> {
        val json = context.openJsonAsset(type.fileName)
        val wordArray = JSONObject(json).getJSONArray(type.jsonArrayName)
        val result = mutableListOf<String>()
        val randomWordCount = (0..10)

        wordArray.let {
            // TODO: This may include the same word twice
            for (i in randomWordCount)
                result.add(it.getString((0 until it.length()).random()))
        }
        return result
    }

    override fun process(
        context: Context,
        payload: ClassPayload,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val merge = mutableListOf<String>()

        //TODO very inefficient due to constantly opening files. Make it more efficient.
        for (wordType in payload.get())
            merge.addAll(getRandomWordsByType(context, wordType))

        //TODO: figure out when the code would fail and emit a Failure()
        result(Success(merge))
    }
}

class DatamuseWordSupplier(val coroutineScope: CoroutineScope) :
    PayloadsWordSupplier<DatamuseWordSupplier.ClassPayload> {

    class ClassPayload(
        private val text: String,
        private val types: List<DatamuseWordSupplier.Type>
    ) :
        Payload {
        override fun get(): Pair<String, List<DatamuseWordSupplier.Type>> {
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
        payload: ClassPayload,
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

