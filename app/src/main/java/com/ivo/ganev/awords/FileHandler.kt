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
import timber.log.Timber.d as d1
import timber.log.Timber.d as debug

/**
 * Handle loading and saving the files
 * */
class FileHandler(
    val context: Context,
    val args: EditorFragmentArguments,
    val textViews: ViewSwitcherEditorText
) : LifecycleObserver, TextChangeBroadcast.OnTextChangeListener {

    enum class Action {
        CREATE,
        OPEN
    }

    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var outputStream: FileOutputStream

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

    private fun save() {
        try {
            val text = textViews.getText()
            outputStream.write(text.encodeToByteArray())
        } catch (ex: FileNotFoundException) {
            Timber.e(ex)
        } catch (ex: IOException) {
            Timber.e(ex)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        save()
        outputStream.close()
        parcelFileDescriptor.close()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        debug("resume")
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

    override fun onTextChange(text: CharSequence) {
        save()
    }
}