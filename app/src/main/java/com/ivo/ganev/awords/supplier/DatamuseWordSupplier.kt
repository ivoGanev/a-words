package com.ivo.ganev.awords.supplier

import android.content.Context
import com.ivo.ganev.awords.functional.Result
import com.ivo.ganev.datamuse_kotlin.client.DatamuseKotlinClient
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.WordsEndpointBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.wordsBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.RelatedWords
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.RelatedWords.Code
import com.ivo.ganev.datamuse_kotlin.endpoint.words.hardConstraintsOf
import com.ivo.ganev.datamuse_kotlin.response.WordResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DatamuseWordSupplier(private val coroutineScope: CoroutineScope) :
    WordSupplier {

    private val datamuseClient = DatamuseKotlinClient()

    class StandardPayload(
        private val word: String,
        private val types: List<Code>
    ) :
        Payload {
        override fun get(): Pair<String, List<Code>> {
            return Pair(word, types)
        }
    }

    private fun queryAsync(query: WordsEndpointBuilder) =
        coroutineScope.async { datamuseClient.query(query.build()) }

    override fun process(
        context: Context,
        payload: Any,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val standardPayload = payload as StandardPayload
        val wordCodes = standardPayload.get().second
        val listOfWords = mutableListOf<String>()

        val endpointQueries = wordCodes.map { relatedWordsCode ->
            wordsBuilder {
                val word = standardPayload.get().first
                hardConstraints = hardConstraintsOf(RelatedWords(relatedWordsCode, word))
                maxResults = 5
            }
        }

        coroutineScope.launch {
            val queries = endpointQueries.map { queryAsync(it).await() }
            for (i in queries.indices) {
                queries[i].applyEither({
                    result(Result.Failure(it))
                }, { wordResponse ->
                    listOfWords.addAll(wordResponse.toWordList())
                })
                if (i == queries.size - 1)
                    result(Result.Success(listOfWords))
            }
        }
    }

    private fun Set<WordResponse>.toWordList() = this.flatMap { it.elements }
        .filterIsInstance<WordResponse.Element.Word>()
        .map { it.word }
}

