package com.ivo.ganev.awords

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.ivo.ganev.awords.view.ViewSwitcherEditorText
import timber.log.Timber
import java.io.*

interface FileSaver {
    fun save()
}

/**
 * Handle loading and saving the files
 * */
class FileHandler(
    private val context: Context,
    private val args: EditorFragmentArguments,
    private val textViews: ViewSwitcherEditorText
) : LifecycleObserver, FileSaver {

    enum class Action {
        CREATE,
        OPEN
    }

    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var outputStream: FileOutputStream

    private fun loadFile(uri: Uri): Boolean {
        val contentResolver = context.contentResolver
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(
                InputStreamReader(inputStream)
            )
            textViews.setText(reader.readText())
        } catch (ex: FileNotFoundException) {
            Timber.e(ex)
            return false
        }
        return true
    }

    override fun save() {
        if (::outputStream.isInitialized) {
            try {
                val text = textViews.getText()
                println("Saving..$text")
                outputStream.write(text.encodeToByteArray())
            } catch (ex: FileNotFoundException) {
                Timber.e(ex)
            } catch (ex: IOException) {
                Timber.e(ex)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        save()
        closeStreams()
    }

    private fun closeStreams() {
        // TODO(Fix Bug): when back button is pressed there is a crash with
        //  "lateinit property outputStream has not been initialized"
        outputStream.close()
        parcelFileDescriptor.close()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (args.fileUri != null && args.fileUri.toString() != "") {
            loadFile(args.fileUri)
            parcelFileDescriptor = context
                .contentResolver
                .openFileDescriptor(args.fileUri, "w")!!
            outputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
        } else {
            Timber.e("No file arguments found.")
        }
    }
}