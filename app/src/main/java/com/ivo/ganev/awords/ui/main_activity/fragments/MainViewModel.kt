package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

const val SELECT_FILE_CODE = 7

class MainViewModel : ViewModel() {

    private val _userPickedFile = MutableLiveData<String>()

    val userPickedFile: LiveData<String>
        get() = _userPickedFile


    fun handleUri(uri: Uri, contentResolver: ContentResolver) {

        val reader = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri)))
        var line: String? = reader.readLine()

        val stringBuilder = StringBuilder()
        while (line != null) {
            stringBuilder.append("$line\n")
            line = reader.readLine()
        }

        _userPickedFile.value = stringBuilder.toString()
    }

    fun doneNavigating() {
        _userPickedFile.value = null
    }

}
