package com.ivo.ganev.awords.ui.main_activity.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.ui.main_activity.MainActivity

class EditorFragment : Fragment(R.layout.fragment_editor) {
    private val viewModel: EditorViewModel by viewModels()
    private lateinit var binding: FragmentEditorBinding
    private val args: EditorFragmentArgs by navArgs()

    //the currently running instance of the activity
    private val mainActivityContext: MainActivity by lazy {
        activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditorBinding.bind(view)
        binding.contentTextview.text = args.content
    }

}
