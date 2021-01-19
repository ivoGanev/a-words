package com.ivo.ganev.awords.ui.main_activity.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentMainBinding
import com.ivo.ganev.awords.ui.main_activity.MainActivity


class MainFragment : Fragment(R.layout.fragment_main) {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    //the currently running instance of the activity
    private val mainActivityContext: MainActivity by lazy {
        activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        binding.selectFile.setOnClickListener {
            launchTxtFilePicker()
        }
        viewModel.doneNavigating()

        viewModel.userPickedFile.observe(viewLifecycleOwner) {
            if (it != null) mainActivityContext.navController.navigate(MainFragmentDirections.actionMainFragmentToEditorFragment(it))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null)
            when (requestCode) {
                SELECT_FILE_CODE -> viewModel.handleUri(
                    data.data!!,
                    mainActivityContext.contentResolver
                )
            }
    }

    private fun launchTxtFilePicker() {
        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "text/plain"
        startActivityForResult(photoPickerIntent, SELECT_FILE_CODE)
    }

}
