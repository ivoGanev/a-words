package com.ivo.ganev.awords.ui.main_activity.fragments

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.view.TextViewWordMutator

class EditorFragment : Fragment(R.layout.fragment_editor), View.OnClickListener {
    private val viewModel: EditorViewModel by viewModels()
    private val args: EditorFragmentArgs by navArgs()

    private lateinit var binding: FragmentEditorBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditorBinding.bind(view)
        binding.apply {
            contentTextview.setClickableText(args.content)
            contentTextview.onWordClickedListener = onWordClickedListener()
            include.button1.tag = EditorViewModel.RadioGroupType.Synonyms
            include.button2.tag = EditorViewModel.RadioGroupType.Antonyms
            include.button3.tag = EditorViewModel.RadioGroupType.Rhymes
            editorSwitch.setOnClickListener(this@EditorFragment)
            editorRedo.setOnClickListener(this@EditorFragment)
            editorUndo.setOnClickListener(this@EditorFragment)
        }



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
              // binding.contentTextview.redoReplacedWord()
            }
            clickedView isWithId R.id.editor_undo -> {
                binding.contentTextview.undoReplacedWord()
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
}

