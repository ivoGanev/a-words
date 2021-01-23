package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivo.ganev.awords.extensions.capitalizeFirstLetter
import com.ivo.ganev.awords.extensions.isFirstCharacterUpperCase
import com.ivo.ganev.datamuse_kotlin.client.DatamuseKotlinClient
import com.ivo.ganev.datamuse_kotlin.endpoint.builders.wordsBuilder
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.*
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.RelatedWords.Code.*
import com.ivo.ganev.datamuse_kotlin.endpoint.words.hardConstraintsOf
import com.ivo.ganev.datamuse_kotlin.response.RemoteFailure
import com.ivo.ganev.datamuse_kotlin.response.WordResponse
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class EditorViewModel : ViewModel() {
    private val datamuseClient = DatamuseKotlinClient()


    private val _wordResult = MutableLiveData<String>()
    val wordResult: LiveData<String>
        get() = _wordResult

    private val _failure = MutableLiveData<RemoteFailure>()
    val failure: LiveData<RemoteFailure>
        get() = _failure

    enum class RadioGroupType {
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
        };

        abstract fun toConstraint(word: String): List<HardConstraint>
    }

    /**
     * Will query Datamuse and update [word] or [failure] accordingly.
     * */
    fun query(word: String, radioGroupType: RadioGroupType) {
        val query = wordsBuilder {
            hardConstraints = radioGroupType.toConstraint(word)
        }


        viewModelScope.launch {
            val words = datamuseClient.query(query.build())
            words.applyEither({ _failure.postValue(it) }, { wordResponses ->
                val randomWord = wordResponses
                    .flatMap { it.elements }
                    .filterIsInstance<WordResponse.Element.Word>()
                    .takeIf { it.isNotEmpty() }
                    ?.random()
                    ?.word
                println(randomWord)
                if (randomWord != null) {
                    if (word.isFirstCharacterUpperCase())
                        _wordResult.postValue(randomWord.capitalizeFirstLetter())
                    else
                        _wordResult.postValue(randomWord)
                } else
                    _wordResult.postValue("")
            })
        }
    }

    fun save(context: Context, uri: Uri?, text: String) {
        try {
            if (uri != null) {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")
                val outputStream = FileOutputStream(parcelFileDescriptor?.fileDescriptor)
                outputStream.write(text.encodeToByteArray())
                outputStream.close()
                parcelFileDescriptor?.close()
            }
        } catch (ex: FileNotFoundException) {
            TODO()
        } catch (ex: IOException) {
            TODO()
        }
    }
}
