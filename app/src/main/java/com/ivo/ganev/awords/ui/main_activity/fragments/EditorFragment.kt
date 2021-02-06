package com.ivo.ganev.awords.ui.main_activity.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.ivo.ganev.awords.FileHandler
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.TextChangeBroadcast
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.view.TextViewWordMutator
import timber.log.Timber
import timber.log.Timber.d as debug

class EditorFragment : Fragment(R.layout.fragment_editor), View.OnClickListener {
    private val viewModel: EditorViewModel by viewModels()
    private val args: EditorFragmentArgs by navArgs()

    private lateinit var binding: FragmentEditorBinding
    private lateinit var fileHandler: FileHandler
    private var textChangeBroadcast: TextChangeBroadcast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditorBinding.bind(view)
        binding.apply {
            //contentTextview.setClickableText(args.fileName)

            fileHandler = FileHandler(requireContext(), args.editorFragmentArgs, editorViewSwitcher)
            lifecycle.addObserver(fileHandler)
//
//            textChangeBroadcast = TextChangeBroadcast(listOf(contentTextview, editorEditText))
//            textChangeBroadcast?.textChangeListener = fileHandler


            contentTextview.onWordClickedListener = onWordClickedListener()
            include.button1.tag = EditorViewModel.RadioGroupType.Synonyms
            include.button2.tag = EditorViewModel.RadioGroupType.Antonyms
            include.button3.tag = EditorViewModel.RadioGroupType.Rhymes
            editorSwitch.setOnClickListener(this@EditorFragment)
            editorRedo.setOnClickListener(this@EditorFragment)
            editorUndo.setOnClickListener(this@EditorFragment)
        }

        debug(args.editorFragmentArgs.toString())

        viewModel.wordResult.observe(viewLifecycleOwner) {
            println(it)
            if (it.isEmpty())
                Toast.makeText(requireContext(), "Couldn't find any related words", Toast.LENGTH_SHORT).show()
            else
                binding.contentTextview.replaceSelectedWord(it)
        }
    }

    override fun onClick(clickedView: View?) {
        when {
            clickedView isWithId R.id.editor_switch -> {
                binding.editorViewSwitcher.showNext()
            }
            clickedView isWithId R.id.editor_redo -> {
                binding.contentTextview.redo()
            }
            clickedView isWithId R.id.editor_undo -> {
                binding.contentTextview.undo()
            }
        }
    }


    private fun onWordClickedListener() = object : TextViewWordMutator.OnWordClickedListener {
        override fun onWordClick(word: String) {
            val checkedId = binding.include.editorRadioGroup.checkedRadioButtonId
            val type = requireActivity().findViewById<RadioButton>(checkedId).tag
            viewModel.query(word, type as EditorViewModel.RadioGroupType)
        }
    }

    override fun onDestroyView() {
        textChangeBroadcast = null
        super.onDestroyView()
    }
}

