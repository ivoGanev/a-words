package com.ivo.ganev.awords

import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivo.ganev.awords.databinding.MainFragmentBinding
import com.ivo.ganev.awords.extensions.isWithId
import java.io.*


class MainFragment : Fragment(R.layout.main_fragment), View.OnClickListener {

    enum class RequestCode {
        OPEN,
        SAVE,
        CREATE
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding

    private val contentResolver: ContentResolver by lazy {
        requireContext().contentResolver
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            Toast.makeText(requireContext(), "File with URI: ${resultData.data!!} successfully saved.", Toast.LENGTH_SHORT).show()
        } catch (ex: FileNotFoundException) {

        } catch (ex: IOException) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultData != null)
            when (requestCode) {
                RequestCode.OPEN.ordinal -> loadFile(resultData)
                RequestCode.SAVE.ordinal -> saveFile(resultData)
            }
    }
}
