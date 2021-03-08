package com.ivo.ganev.awords.ui.main.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ivo.ganev.awords.*
import com.ivo.ganev.awords.data.UserSettingsRepository
import com.ivo.ganev.awords.data.settingsDataStore
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.extensions.filterTickedCheckboxWithTag
import com.ivo.ganev.awords.extensions.isWithId
import com.ivo.ganev.awords.extensions.selectWord
import com.ivo.ganev.awords.platform.concatLists
import com.ivo.ganev.awords.ui.WordSupplierOptionsSheetFragment
import com.ivo.ganev.awords.view.AutoCompleteEditText
import com.ivo.ganev.awords.view.TextViewWordMutator
import com.ivo.ganev.awords.io.FileHandler
import com.ivo.ganev.awords.supplier.PartOfSpeechWordSupplier
import com.ivo.ganev.awords.ui.main.EditorAutoCompleteScreen
import com.ivo.ganev.awords.ui.main.EditorRandomWordScreen
import com.ivo.ganev.awords.ui.main.fragments.EditorViewModel.QueryArgKeys.EDITOR_MODE
import kotlinx.coroutines.flow.map

private const val WORD_SETTINGS_FRAGMENT_TAG = "word_supplier_options"

class EditorFragment : Fragment(R.layout.fragment_editor),
    View.OnClickListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private val args: EditorFragmentArgs by navArgs()

    private lateinit var viewModel: EditorViewModel
    private lateinit var binding: FragmentEditorBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var editorRandomWordScreen: EditorRandomWordScreen
    private lateinit var editorAutoCompleteScreen: EditorAutoCompleteScreen
    private lateinit var bottomSheetFragment: BottomSheetDialogFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            EditorViewModelFactory(UserSettingsRepository.getInstance(requireContext()))
        ).get(EditorViewModel::class.java)

        binding = FragmentEditorBinding.bind(view)
        bottomSheetFragment = WordSupplierOptionsSheetFragment.newInstance()

        binding.apply {
            fileHandler = FileHandler(requireContext(), args.editorFragmentArgs, editorViewSwitcher)
            bottomNavigation.setOnNavigationItemSelectedListener(this@EditorFragment)
            editorExpandWordFetchers.setOnClickListener(this@EditorFragment)
        }

        editorRandomWordScreen =
            EditorRandomWordScreen(requireContext(), binding, viewModel, viewLifecycleOwner)
        editorAutoCompleteScreen =
            EditorAutoCompleteScreen(requireContext(), binding, viewModel, viewLifecycleOwner)

        lifecycle.addObserver(fileHandler)
        lifecycle.addObserver(editorRandomWordScreen)
        lifecycle.addObserver(editorAutoCompleteScreen)
    }

    override fun onClick(clickedView: View?) {
        when {
            clickedView isWithId R.id.editor_expand_word_fetchers -> {
                bottomSheetFragment.show(parentFragmentManager, WORD_SETTINGS_FRAGMENT_TAG)
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
}

