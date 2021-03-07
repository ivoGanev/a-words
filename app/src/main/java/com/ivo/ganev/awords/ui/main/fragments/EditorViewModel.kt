package com.ivo.ganev.awords.ui.main.fragments

import android.content.Context
import androidx.lifecycle.*
import com.ivo.ganev.awords.data.UserSettingsRepository
import com.ivo.ganev.awords.functional.Result
import com.ivo.ganev.awords.supplier.DatamuseWordSupplier
import com.ivo.ganev.awords.supplier.PartOfSpeechWordSupplier
import com.ivo.ganev.awords.supplier.PartOfSpeechWordSupplier.FileInfo
import com.ivo.ganev.awords.supplier.Payload
import com.ivo.ganev.awords.supplier.WordSupplier
import com.ivo.ganev.datamuse_kotlin.endpoint.words.HardConstraint.RelatedWords.Code
import com.ivo.ganev.datamuse_kotlin.response.RemoteFailure
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class EditorViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val datamuseWordSupplier = DatamuseWordSupplier(viewModelScope)
    private val partOfSpeechSupplier = PartOfSpeechWordSupplier(viewModelScope)

    private val _wordResult = MutableLiveData<List<String>>()
    val wordResult: LiveData<List<String>>
        get() = _wordResult

    private val _remoteFailure = MutableLiveData<RemoteFailure>()

    @Suppress("unused")
    val remoteFailure: LiveData<RemoteFailure>
        get() = _remoteFailure


    private fun mapWordCodes(): List<Code> {
        val settings = runBlocking { userSettingsRepository.settingsFlow.first() }
        val wordCodes = mutableListOf<Code>()

        with(settings) {
            if (chkboxSynonyms)
                wordCodes.add(Code.SYNONYMS)
            if (chkboxAntonyms)
                wordCodes.add(Code.ANTONYMS)
            if (chkboxRhymes)
                wordCodes.add(Code.RHYMES)
            if (chkboxHomophones)
                wordCodes.add(Code.HOMOPHONES)
            if (chkboxPopAdj)
                wordCodes.add(Code.POPULAR_ADJECTIVES)
            if (chkboxPopNouns)
                wordCodes.add(Code.POPULAR_NOUNS)
        }
        return wordCodes
    }

    private fun mapFileInfos(): List<FileInfo> {
        val settings = runBlocking { userSettingsRepository.settingsFlow.first() }
        val fileInfos = mutableListOf<FileInfo>()

        with(settings) {
            with(fileInfos) {
                if (chkboxVerb) add(FileInfo("verbs.json", "verbs"))
                if (chkboxAdverb) add(FileInfo("adverbs.json", "adverbs"))
                if (chkboxNoun) add(FileInfo("nouns.json", "nouns"))
                if (chkboxAdj) add(FileInfo("adjs.json", "adjs"))
            }
        }
        return fileInfos
    }

    /**
     * argument keys: "tags" - List<*> where it has to be Supplier type,
     * "word" - String, "word_picker_strategy" - WordPickerJSONStrategy
     * */
    fun query(context: Context, arguments: Map<String, Any>) {
        val wordCodes = mapWordCodes()
        val fileInfos = mapFileInfos()

        val supplier: WordSupplier
        val payload: Payload
        val word = arguments["word"] as String
        val strategy =
            arguments["word_picker_strategy"] as PartOfSpeechWordSupplier.WordPickStrategy

        if (wordCodes.isNotEmpty()) {
            supplier = datamuseWordSupplier
            payload = DatamuseWordSupplier.StandardPayload(word, wordCodes)
        } else {
            supplier = partOfSpeechSupplier
            payload = PartOfSpeechWordSupplier.StandardPayload(word, fileInfos, strategy)
        }

        supplier.process(context, payload) { result ->
            when (result) {
                is Result.Failure -> _remoteFailure.postValue(result.failure as RemoteFailure)
                is Result.Success -> result.result.let { _wordResult.postValue(it) }
            }
        }
    }
}

class EditorViewModelFactory(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditorViewModel(userSettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

