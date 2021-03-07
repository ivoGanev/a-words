package com.ivo.ganev.awords.supplier

import android.content.Context
import com.ivo.ganev.awords.functional.Result
import com.ivo.ganev.awords.extensions.openJsonAsset
import kotlinx.coroutines.CoroutineScope
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

class PartOfSpeechWordSupplier(val coroutineScope: CoroutineScope) :
    WordSupplier {

    class StandardPayload(
        val word: String? = null,
        val fileInfo: List<FileInfo>,
        val wordPickStrategy: WordPickStrategy
    ) : Payload {
        override fun get() = this
    }

    data class FileInfo(val fileName: String, val jsonArray: String)

//    enum class Type {
//        SpeechPart.NOUN {
//            override val fileName: String
//            get() = "nouns.json"
//            override val jsonArrayName: String
//            get() = "nouns"
//        },
//        ADJECTIVE {
//            override val fileName: String
//                get() = "adjs.json"
//            override val jsonArrayName: String
//                get() = "adjs"
//        },
//        ADVERB {
//            override val fileName: String
//                get() = "adverbs.json"
//            override val jsonArrayName: String
//                get() = "adverbs"
//        },
//        VERB {
//            override val fileName: String
//                get() = "verbs.json"
//            override val jsonArrayName: String
//                get() = "verbs"
//        };

//        abstract val fileName: String
//        abstract val jsonArrayName: String
//    }

    override fun process(
        context: Context,
        payload: Any,
        result: (Result<List<String>, Any>) -> Unit
    ) {
        val merge = mutableListOf<String>()
        val standardPayload = payload as StandardPayload

        // TODO: Make a picking process for the autocomplete. If user writes 'a'
        //  a word selector will pick a list with all the words starting with 'a'
        //  and a word filter should choose which words will remain. For now a
        //  filter with randomized fashion will suffice.
        for (wordType in standardPayload.fileInfo) {
            val json = context.openJsonAsset(wordType.fileName)
            val wordArray = JSONObject(json).getJSONArray(wordType.jsonArray)
            val words = standardPayload.wordPickStrategy.pick(wordArray, standardPayload.word)
            merge.addAll(words)
        }

        //TODO: figure out when the code would fail and emit a Failure()
        result(Result.Success(merge))
    }

    interface WordPickStrategy {
        fun pick(array: JSONArray, extraArgs: Any?): List<String>
    }

    class WordPickStrategyContainsName : WordPickStrategy {
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
                Timber.d(result.count().toString())
            }
            return result
        }
    }

    class WordPickStrategyRandom : WordPickStrategy {
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
}