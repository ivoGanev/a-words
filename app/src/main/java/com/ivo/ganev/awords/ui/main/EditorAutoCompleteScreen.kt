package com.ivo.ganev.awords.ui.main

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.extensions.selectWord
import com.ivo.ganev.awords.supplier.PartOfSpeechWordSupplier
import com.ivo.ganev.awords.ui.main.fragments.EditorViewModel
import com.ivo.ganev.awords.view.AutoCompleteEditText

class EditorAutoCompleteScreen(
    context: Context,
    binding: FragmentEditorBinding,
    viewModel: EditorViewModel,
    lifeCycleOwner: LifecycleOwner
) : EditorScreenBase(context, binding, viewModel, lifeCycleOwner),
    AutoCompleteEditText.OnFilteredTextChangeListener {

    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate() {
        with(binding) {
            editorEditText.onFilteredTextChangeListener = this@EditorAutoCompleteScreen
            editorEditText.setOnClickListener(this@EditorAutoCompleteScreen)
        }
        viewModel.wordResults.observe(lifeCycleOwner) { wordList ->
            wordList.forEach { println(it) }

            if (wordList.isEmpty())
                Toast.makeText(
                    context,
                    "Couldn't find any related words",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                arrayAdapter =
                    ArrayAdapter(context, R.layout.dropdown_autocomplete, wordList)
                binding.editorEditText.setAdapter(arrayAdapter)
                binding.editorEditText.showDropDown()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.editor_edit_text -> {
                onClickAutoCompleteEditText()
            }
        }
    }

    private fun onClickAutoCompleteEditText() {
        with(binding.editorEditText)
        {
            val text = text.toString()
            if (selectionStart < text.length) {
                val word = text.selectWord(selectionStart)
                val arguments = mapOf(
                    EditorViewModel.WORD to word,
                    EditorViewModel.PART_OF_SPEECH_WORD_PICK_STRATEGY to PartOfSpeechWordSupplier.WordPickStrategyRandom(),
                    EditorViewModel.EDITOR_MODE to 0
                )
                viewModel.query(context, arguments)
            }
        }
    }

    override fun onFilteredTextChanged(word: String) {
        val arguments = mapOf(
            EditorViewModel.WORD to word,
            EditorViewModel.PART_OF_SPEECH_WORD_PICK_STRATEGY to PartOfSpeechWordSupplier.WordPickStrategyContainsName(),
            EditorViewModel.EDITOR_MODE to 0
        )
        viewModel.query(context, arguments)
    }
}