package com.ivo.ganev.awords.ui.main_activity.fragments

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.view.TextViewWordMutator
import com.ivo.ganev.datamuse_kotlin.response.WordResponse.Element.Word

class EditorFragment : Fragment(R.layout.fragment_editor), View.OnClickListener {
    private val viewModel: EditorViewModel by viewModels()
    private val args: EditorFragmentArgs by navArgs()

    private lateinit var binding: FragmentEditorBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditorBinding.bind(view)
        binding.contentTextview.setClickableText(args.content)

        binding.include.button1.tag = EditorViewModel.RadioGroupType.Synonyms
        binding.include.button2.tag = EditorViewModel.RadioGroupType.Antonyms
        binding.include.button3.tag = EditorViewModel.RadioGroupType.Rhymes
        binding.editorSwitch.setOnClickListener(this)
        binding.contentTextview.onWordClickedListener = onWordClickedListener()

        viewModel.results.observe(viewLifecycleOwner) { wordResponses ->
            val randomWord = wordResponses
                .flatMap { it.elements }
                .filterIsInstance<Word>()

            if (randomWord.isNotEmpty())
                binding.contentTextview.replaceSelectedWord(randomWord.random().word)
        }
    }

    override fun onClick(clickedView: View?) {
        if (clickedView isWithId R.id.editor_switch) {
            switchMode()
        }
    }

    private fun switchMode() {
        binding.editorViewSwitcher.showNext()
    }

    private fun onWordClickedListener() = object : TextViewWordMutator.OnWordClickedListener {
        override fun onWordClick(word: String) {
            val checkedId = binding.include.editorRadioGroup.checkedRadioButtonId
            val type = requireActivity().findViewById<RadioButton>(checkedId).tag
            viewModel.query(word, type as EditorViewModel.RadioGroupType)
        }
    }

}

