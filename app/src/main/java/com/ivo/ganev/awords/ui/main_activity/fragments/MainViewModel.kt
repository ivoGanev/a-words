package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ivo.ganev.awords.EditorFragmentArguments
import com.ivo.ganev.awords.FileHandler
import com.ivo.ganev.awords.FileHandler.Action.CREATE
import com.ivo.ganev.awords.platform.SingleEvent
import timber.log.Timber


class MainViewModel : ViewModel() {
    private val _action = MutableLiveData<SingleEvent<EditorFragmentArguments>>()

    val action: LiveData<SingleEvent<EditorFragmentArguments>>
        get() = _action


    fun provideEditorFragmentArguments(providerIntent: Intent, fileHandlerAction: Int): Boolean {
        if (providerIntent.data == null) return false
        providerIntent.data?.let {
            Timber.d("File with provider Uri: $it successfully created.")
            _action.value = SingleEvent(EditorFragmentArguments(fileHandlerAction, it))
        }
        return true
    }
}



