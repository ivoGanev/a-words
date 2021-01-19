package com.ivo.ganev.awords

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ivo.ganev.awords.databinding.ActivityTestIoBinding
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.view.TextViewWordMutator
import java.io.*

class ActivityTestIo : AppCompatActivity(), View.OnClickListener,
    TextViewWordMutator.OnWordClickedListener {

    enum class RequestCode {
        OPEN,
        SAVE,
        CREATE
    }

    private lateinit var binding: ActivityTestIoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestIoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonOpenFile.setOnClickListener(this)
        binding.buttonSaveFile.setOnClickListener(this)
        binding.buttonCreateFile.setOnClickListener(this)
        binding.tvSimpleText.onWordClickedListener = this
    }

    override fun onClick(clickedView: View?) {
        when {
            clickedView isWithId R.id.button_open_file -> openDocumentProvider(RequestCode.OPEN)
            clickedView isWithId R.id.button_save_file -> openDocumentProvider(RequestCode.SAVE)
            clickedView isWithId R.id.button_create_file -> openDocumentProvider(RequestCode.CREATE)
        }
    }

    private fun openDocumentProvider(requestCode: RequestCode) {
        val action = when (requestCode.ordinal) {
            RequestCode.OPEN.ordinal -> Intent.ACTION_OPEN_DOCUMENT
            RequestCode.SAVE.ordinal -> Intent.ACTION_OPEN_DOCUMENT
            RequestCode.CREATE.ordinal -> Intent.ACTION_CREATE_DOCUMENT
            else -> throw IllegalArgumentException("$requestCode is not supported.")
        }

        val intent = Intent(action)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        startActivityForResult(intent, requestCode.ordinal)
    }

    private fun loadFile(resultData: Intent) {
        resultData.apply {
            println(data)
            val stream = contentResolver.openInputStream(data!!)
            val reader = BufferedReader(InputStreamReader(stream))
            // TODO display probably inside the view model
            binding.tvSimpleText.setClickableText(reader.readText())
            Toast.makeText(
                this@ActivityTestIo,
                "Loaded from: $data",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveFile(resultData: Intent) {
        try {
            // TODO this needs to be inside a coroutine
            val uri = resultData.data ?: return

            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "w")
            val outputStream = FileOutputStream(parcelFileDescriptor?.fileDescriptor)
            val string = binding.tvSimpleText.text.toString()
            outputStream.write(string.encodeToByteArray())
            outputStream.close()
            parcelFileDescriptor?.close()
            Toast.makeText(this, "File with URI: $uri successfully saved.", Toast.LENGTH_LONG)
                .show()
        } catch (ex: FileNotFoundException) {
            TODO()
        } catch (ex: IOException) {
            TODO()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultData != null)
            when (requestCode) {
                RequestCode.OPEN.ordinal -> loadFile(resultData)
                RequestCode.SAVE.ordinal -> saveFile(resultData)
                RequestCode.CREATE.ordinal -> createFile(resultData)
            }
    }

    private fun createFile(resultData: Intent) {
        val uri = resultData.data ?: return
        Toast.makeText(this, "File with URI: $uri successfully created.", Toast.LENGTH_LONG)
            .show()
    }

    override fun onWordClick(word: String) {
        binding.tvSimpleText.replaceSelectedWord((0..100).random().toString())
    }
}

