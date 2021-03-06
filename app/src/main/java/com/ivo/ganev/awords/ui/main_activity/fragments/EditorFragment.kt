package com.ivo.ganev.awords.ui.main_activity.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ivo.ganev.awords.*
import com.ivo.ganev.awords.DatamuseWordSupplier.Type.*
import com.ivo.ganev.awords.POSWordSupplier.Type.*
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.extensions.filterTickedCheckboxWithTag
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.extensions.selectWord
import com.ivo.ganev.awords.platform.concatLists
import com.ivo.ganev.awords.view.AutoCompleteEditText
import com.ivo.ganev.awords.view.TextViewWordMutator
import kotlinx.coroutines.flow.map

class EditorFragment : Fragment(R.layout.fragment_editor),
    View.OnClickListener,
    BottomNavigationView.OnNavigationItemSelectedListener,
    AutoCompleteEditText.OnFilteredTextChangeListener {

    private val WORD_SETTINGS_FRAGMENT_TAG = "word_supplier_options"

    private val viewModel: EditorViewModel by viewModels()

    private val args: EditorFragmentArgs by navArgs()
    private lateinit var binding: FragmentEditorBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var arrayAdapter: ArrayAdapter<String>

    private lateinit var bottomSheetFragment: BottomSheetDialogFragment

    private inline fun <reified T> filterCheckboxTags(viewGroup: ViewGroup): List<T> =
        viewGroup.children.filterTickedCheckboxWithTag()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditorBinding.bind(view)
        bottomSheetFragment = WordSupplierOptionsSheet.newInstance()
        bottomSheetFragment
        binding.apply {
            fileHandler = FileHandler(requireContext(), args.editorFragmentArgs, editorViewSwitcher)

            bottomNavigation.setOnNavigationItemSelectedListener(this@EditorFragment)
            editorExpandWordFetchers.setOnClickListener(this@EditorFragment)
            contentTextview.onWordClickedListener = onWordClickedListener()
            editorEditText.onFilteredTextChangeListener = this@EditorFragment

//            include.apply {
//                editorPopupDatamuseAnt.tag = ANTONYMS
//                editorPopupDatamuseSyn.tag = SYNONYMS
//                editorPopupDatamuseRhy.tag = RHYMES
//                editorPopupDatamuseHom.tag = HOMOPHONES
//                editorPopupDatamusePopAdj.tag = POPULAR_ADJECTIVES
//                editorPopupDatamusePopNoun.tag = POPULAR_NOUNS
//                editorPopupRandomAdj.tag = ADJECTIVE
//                editorPopupRandomNoun.tag = NOUN
//                editorPopupRandomAdverb.tag = ADVERB
//                editorPopupRandomVerb.tag = VERB
//            }
        }

        viewModel.wordResult.observe(viewLifecycleOwner) {
            if (it.isEmpty())
                Toast.makeText(
                    requireContext(),
                    "Couldn't find any related words",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                arrayAdapter =
                    ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, it)
                binding.editorEditText.setAdapter(arrayAdapter)
                binding.editorEditText.showDropDown()
            }
        }

        lifecycle.addObserver(fileHandler)
    }


    private fun onWordClickedListener() = object : TextViewWordMutator.OnWordClickedListener {
        override fun onWordClick(word: String) {
//            val checkedId = binding.include.editorRadioGroup.checkedRadioButtonId
//            val type = requireActivity().findViewById<RadioButton>(checkedId).tag
//            viewModel.query(word, type as DatamuseType)
        }
    }

    override fun onClick(clickedView: View?) {
        when {
            clickedView isWithId R.id.editor_expand_word_fetchers -> {
                bottomSheetFragment.show(parentFragmentManager, WORD_SETTINGS_FRAGMENT_TAG)
            }

            clickedView isWithId R.id.editor_edit_text -> {
                onClickAutoCompleteEditText()
            }
            clickedView isWithId R.id.editor_redo -> {
                parentFragmentManager.findFragmentByTag(WORD_SETTINGS_FRAGMENT_TAG)
                binding.contentTextview.redo()
            }
            clickedView isWithId R.id.editor_undo -> {
                binding.contentTextview.undo()
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
                    "tags" to collectTags(),
                    "word" to word,
                    "word_picker_strategy" to WordPickerJSONStrategyRandom()
                )
                viewModel.query(requireContext(), arguments)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_nav_first -> onFirstBottomNavButtonClicked()
            R.id.bottom_nav_second -> onSecondBottomNavButtonClicked()
        }
        return true
    }

    private fun onSecondBottomNavButtonClicked() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.apply {
            editorViewSwitcher.displayedChild = 1
            includeUndoRedo.includeUndoRedoLayout.apply {
                visibility = View.VISIBLE
                startAnimation(animation)
            }
        }
    }

    private fun onFirstBottomNavButtonClicked() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
        binding.apply {
            editorViewSwitcher.displayedChild = 0
            includeUndoRedo.includeUndoRedoLayout.apply {
                visibility = View.GONE
                startAnimation(animation)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().settingsDataStore.data.map { item ->
            with(item)
            {
                println(item.chkboxAdj)
            }
        }
    }

    override fun onFilteredTextChanged(word: String) {
        val arguments = mapOf(
            "tags" to collectTags(),
            "word" to word,
            "word_picker_strategy" to WordPickerJSONStrategyContainsName()
        )
        viewModel.query(requireContext(), arguments)
    }

    private fun collectTags(): List<*> {
        return concatLists(
//            filterCheckboxTags<DatamuseWordSupplier.Type>(binding.include.editorDatamuseGrid),
//            filterCheckboxTags<POSWordSupplier.Type>(binding.include.editorPosWordGrid)
        )
    }
}

