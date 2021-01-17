package com.ivo.ganev.awords

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val userPickedFileUri = MutableLiveData<Uri>()
}
