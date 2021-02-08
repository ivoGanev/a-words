package com.ivo.ganev.awords

import android.content.Context
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
    val context: Context,
    val args: EditorFragmentArguments,
    val textViews: ViewSwitcherEditorText
) : LifecycleObserver, FileSaver {

    enum class Action {
        CREATE,
        OPEN
    }

    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var outputStream: FileOutputStream
    private lateinit var inputStream: FileInputStream

    private fun loadFile(): Boolean {
        try {
            if (args.fileUri != null) {
                val reader = BufferedReader(
                    InputStreamReader(
                        context.contentResolver.openInputStream(args.fileUri)
                    )
                )
                textViews.setText(reader.readText())
            } else {
                Timber.e("No file arguments found.")
            }
        } catch (ex: FileNotFoundException) {
            Timber.e(ex)
            return false
        }
        return true
    }

    override fun save() {
        if(::outputStream.isInitialized) {
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
        outputStream.close()
        parcelFileDescriptor.close()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (args.fileUri != null) {
            loadFile()

            parcelFileDescriptor = context
                .contentResolver
                .openFileDescriptor(args.fileUri, "w")!!
            outputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
        } else {
            Timber.e("No file arguments found.")
        }
    }
}