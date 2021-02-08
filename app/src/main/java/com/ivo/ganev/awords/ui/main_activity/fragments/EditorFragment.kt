package com.ivo.ganev.awords.ui.main_activity.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.ivo.ganev.awords.FileHandler
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.SpaceTokenizer
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.extensions.selectWord
import com.ivo.ganev.awords.ui.main_activity.fragments.EditorViewModel.DatamuseType
import com.ivo.ganev.awords.ui.main_activity.fragments.EditorViewModel.DatamuseType.*
import com.ivo.ganev.awords.view.TextViewWordMutator
import timber.log.Timber.d as debug

class EditorFragment : Fragment(R.layout.fragment_editor), View.OnClickListener, TextWatcher {
    private val viewModel: EditorViewModel by viewModels()
    private val args: EditorFragmentArgs by navArgs()

    private lateinit var binding: FragmentEditorBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditorBinding.bind(view)
        binding.apply {
            fileHandler = FileHandler(requireContext(), args.editorFragmentArgs, editorViewSwitcher)
            lifecycle.addObserver(fileHandler)

            arrayAdapter =
                ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, arrayListOf())

            editorEditText.apply {
                setAdapter(arrayAdapter)
                setTokenizer(SpaceTokenizer())
                threshold = 0
                addTextChangedListener(this@EditorFragment)
                setOnClickListener(this@EditorFragment)
            }

            include.apply {
                editorPopupDatamuseAnt.tag = Antonyms
                editorPopupDatamuseSyn.tag = Synonyms
                editorPopupDatamuseRhy.tag = Rhymes
                editorPopupDatamuseHom.tag = Homophones
                editorPopupDatamusePopAdj.tag = PopularAdjectives
                editorPopupDatamusePopNoun.tag = PopularNoun
            }

            contentTextview.onWordClickedListener = onWordClickedListener()
            editorSwitch.setOnClickListener(this@EditorFragment)
            editorRedo.setOnClickListener(this@EditorFragment)
            editorUndo.setOnClickListener(this@EditorFragment)
        }

        debug(args.editorFragmentArgs.toString())


        viewModel.wordResult.observe(viewLifecycleOwner) {
            if (it.isEmpty())
                Toast.makeText(
                    requireContext(),
                    "Couldn't find any related words",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                it.forEach { t -> debug(t) }
                arrayAdapter =
                    ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, it)
                binding.editorEditText.setAdapter(arrayAdapter)

                //if (selection == Selection.CLICK) {
                arrayAdapter.notifyDataSetChanged()
                binding.editorEditText.showDropDown()
                //}
            }
        }
    }

    private fun onWordClickedListener() = object : TextViewWordMutator.OnWordClickedListener {
        override fun onWordClick(word: String) {
//            val checkedId = binding.include.editorRadioGroup.checkedRadioButtonId
//            val type = requireActivity().findViewById<RadioButton>(checkedId).tag
//            viewModel.query(word, type as DatamuseType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        with(binding.editorEditText)
        {
            if (enoughToFilter()) {
                val tokenizer = SpaceTokenizer()
                val tokenStart = tokenizer.findTokenStart(text, selectionEnd)
                val word = text.toString().substring(tokenStart, selectionEnd)

                viewModel.query(word, getDatamuseCheckboxFunc())
                println("caught: $word")
            }
        }
    }

    private fun getDatamuseCheckboxFunc(): List<DatamuseType> {
        val cb = binding.include.editorDatamuseGrid.children.filterIsInstance<CheckBox>()
        val list = mutableListOf<DatamuseType>()
        cb.forEach {
            if (it.isChecked)
                list.add(it.tag as DatamuseType)
        }
        return list
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun onClick(clickedView: View?) {
        when {
            clickedView isWithId R.id.editor_edit_text -> {
                replWord()
            }
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

    private fun replWord() {
        with(binding.editorEditText)
        {
            val text = text.toString()
            if (selectionStart < text.length) {
                val selectedWord = text.selectWord(selectionStart)
                val checkedId = binding.include.editorRadioGroup.checkedRadioButtonId
                val type = requireActivity().findViewById<RadioButton>(checkedId).tag
                viewModel.query(selectedWord, getDatamuseCheckboxFunc())
                debug(selectedWord)
            }
        }
    }
}

