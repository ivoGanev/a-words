package com.ivo.ganev.awords.ui.main_activity.fragments

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ivo.ganev.awords.ActivityTestIo
import com.ivo.ganev.awords.platform.SingleEvent
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

class MainViewModel : ViewModel() {
    private val _userPickedFile = MutableLiveData<SingleEvent<String>>()

    val userPickedFile: LiveData<SingleEvent<String>>
        get() = _userPickedFile

    fun loadFile(contentResolver: ContentResolver, providerIntent: Intent): Boolean {
        if (providerIntent.data == null) return false
        val reader = BufferedReader(InputStreamReader(providerIntent.data?.let {
            contentResolver.openInputStream(it) }))
        _userPickedFile.value = SingleEvent(reader.readText())
        return true
    }

    fun createFile(providerIntent: Intent): Boolean {
        if (providerIntent.data == null) return false
        providerIntent.data?.let { Timber.d("File with provider Uri: $it successfully created.") }
        return true
    }
}



