package com.ivo.ganev.awords.ui.main_activity.fragments

import android.R.attr.label
import android.content.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ivo.ganev.awords.platform.SingleEvent
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader


class MainViewModel : ViewModel() {
    private val _userPickedFile = MutableLiveData<SingleEvent<String>>()

    val userPickedFile: LiveData<SingleEvent<String>>
        get() = _userPickedFile

    fun loadFromClipBoard(context: Context) {
        val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clippedText = clipboard?.primaryClip
        _userPickedFile.value = SingleEvent(clippedText?.getItemAt(0)?.text.toString())
    }

    fun loadFile(context: Context, providerIntent: Intent): Boolean {
        if (providerIntent.data == null) return false
        val reader = BufferedReader(InputStreamReader(providerIntent.data?.let {
            context.contentResolver.openInputStream(it)
        }))
        _userPickedFile.value = SingleEvent(reader.readText())
        return true
    }

    fun createFile(providerIntent: Intent): Boolean {
        if (providerIntent.data == null) return false
        providerIntent.data?.let { Timber.d("File with provider Uri: $it successfully created.") }
        return true
    }
}



