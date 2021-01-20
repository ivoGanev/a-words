package com.ivo.ganev.awords.ui.main_activity.fragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentMainBinding
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.provider.StorageAccessFramework
import com.ivo.ganev.awords.ui.main_activity.MainActivity
import com.ivo.ganev.awords.ui.main_activity.fragments.MainFragment.RequestCode.*



class MainFragment : Fragment(R.layout.fragment_main), View.OnClickListener {
    enum class RequestCode {
        OPEN,
        SAVE,
        CREATE
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    // This is a potential memory leak!
    private val mainActivityContext: MainActivity by lazy {
        activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        binding.mainButtonCreateNewFile.setOnClickListener(this)
        binding.mainButtonOpenFile.setOnClickListener(this)
        binding.mainButtonOpenFile.setOnClickListener(this)

        viewModel.doneNavigating()

        viewModel.userPickedFile.observe(viewLifecycleOwner) {
            if (it != null) mainActivityContext.navController.navigate(MainFragmentDirections.actionMainFragmentToEditorFragment(it))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                OPEN.ordinal -> viewModel.loadFile(requireContext().contentResolver, data)
                CREATE.ordinal -> viewModel.createFile(data)
            }
        }
        else if(resultCode == Activity.RESULT_CANCELED) {
            TODO()
        }
    }

    override fun onClick(clickedView: View?) {
        when {
            clickedView isWithId R.id.main_button_open_file ->
                startActivityForResult(StorageAccessFramework.getOpenIntent(), OPEN.ordinal)
            clickedView isWithId R.id.main_button_create_new_file ->
                startActivityForResult(StorageAccessFramework.getCreateIntent(), CREATE.ordinal)
            clickedView isWithId R.id.main_button_load_clipboard -> TODO()
        }
    }
}
