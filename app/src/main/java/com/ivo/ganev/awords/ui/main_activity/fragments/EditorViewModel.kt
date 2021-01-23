package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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


    private val _results = MutableLiveData<Set<WordResponse>>()
    val results: LiveData<Set<WordResponse>>
        get() = _results

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
     * Will query Datamuse and update [results] or [failure] accordingly.
     * */
    fun query(word: String, radioGroupType: RadioGroupType) {
        val query = wordsBuilder {
            hardConstraints = radioGroupType.toConstraint(word)
        }
        println(query.build().toUrl())
        viewModelScope.launch {
            val words = datamuseClient.query(query.build())
            words.applyEither({ _failure.postValue(it) }, { _results.postValue(it) })
        }
    }

    fun save(context: Context,uri: Uri?, text: String) {
        try {
            if(uri!=null) {
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
