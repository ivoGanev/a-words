package com.ivo.ganev.awords.ui.main.fragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivo.ganev.awords.io.FileHandler.Action.CREATE
import com.ivo.ganev.awords.io.FileHandler.Action.OPEN
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentMainBinding
import com.ivo.ganev.awords.provider.StorageAccessFramework
import com.ivo.ganev.awords.ui.main.MainActivity


class MainFragment : Fragment(R.layout.fragment_main), View.OnClickListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    private val mainActivityContext: MainActivity by lazy {
        activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)

        binding.apply {
            mainButtonOpenFile.setOnClickListener(this@MainFragment)
            mainButtonLoadClipboard.setOnClickListener(this@MainFragment)
            mainButtonCreateNewFile.setOnClickListener(this@MainFragment)
        }

        viewModel.action.observe(viewLifecycleOwner) { event ->
            event.getUnhandled()
                ?.let {
                    mainActivityContext.navController.navigate(
                        MainFragmentDirections.actionMainFragmentToEditorFragment(it)
                    )
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            viewModel.provideEditorFragmentArguments(data, requestCode)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(mainActivityContext, "No File Selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(clickedView: View) {
        when (clickedView) {
            binding.mainButtonOpenFile ->
                startActivityForResult(StorageAccessFramework.getOpenIntent(), OPEN.ordinal)
            binding.mainButtonCreateNewFile ->
                startActivityForResult(StorageAccessFramework.getCreateIntent(), CREATE.ordinal)
        }
    }
}
