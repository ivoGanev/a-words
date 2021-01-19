package com.ivo.ganev.awords

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ivo.ganev.awords.databinding.ActivityTestIoBinding
import com.ivo.ganev.awords.extensions.isWithId
import java.io.*

class ActivityTestIo : AppCompatActivity(), View.OnClickListener {

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
            println(reader.readText())
        }
    }

    private fun saveFile(resultData: Intent) {
        try {
            // TODO this needs to be inside a coroutine
            val parcelFileDescriptor = contentResolver.openFileDescriptor(resultData.data!!, "w")
            val outputStream = FileOutputStream(parcelFileDescriptor?.fileDescriptor)
            val string = binding.tvSimpleText.text.toString()
            outputStream.write(string.encodeToByteArray())
            outputStream.close()
            parcelFileDescriptor?.close()
            Toast.makeText(
                this,
                "File with URI: ${resultData.data!!} successfully saved.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (ex: FileNotFoundException) {

        } catch (ex: IOException) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultData != null)
            when (requestCode) {
                RequestCode.OPEN.ordinal -> loadFile(resultData)
                RequestCode.SAVE.ordinal -> saveFile(resultData)
            }
    }
}

