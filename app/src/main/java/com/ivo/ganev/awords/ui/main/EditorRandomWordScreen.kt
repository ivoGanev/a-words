package com.ivo.ganev.awords.ui.main

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.supplier.PartOfSpeechWordSupplier
import com.ivo.ganev.awords.ui.main.fragments.EditorViewModel
import com.ivo.ganev.awords.view.TextViewWordMutator

class EditorRandomWordScreen(
    private val context: Context,
    private val binding: FragmentEditorBinding,
    private val viewModel: EditorViewModel,
    private val lifeCycleOwner: LifecycleOwner
) : LifecycleObserver, View.OnClickListener {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        binding.contentTextview.onWordClickedListener = onWordClickedListener()
        binding.includeUndoRedo.editorUndo.setOnClickListener(this)
        binding.includeUndoRedo.editorRedo.setOnClickListener(this)
        viewModel.randomWord.observe(lifeCycleOwner, { word ->
            println(word)
            binding.contentTextview.replaceSelectedWord(word)
        })
    }

    private fun onWordClickedListener() = object : TextViewWordMutator.OnWordClickedListener {
        override fun onWordClick(word: String) {
            val arguments = mapOf(
                EditorViewModel.WORD to word,
                EditorViewModel.PART_OF_SPEECH_WORD_PICK_STRATEGY to PartOfSpeechWordSupplier.WordPickStrategyRandom(),
                EditorViewModel.EDITOR_MODE to 1
            )
            viewModel.query(context, arguments)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.editor_redo -> {
                binding.contentTextview.redo()
            }
            R.id.editor_undo -> {
                binding.contentTextview.undo()
            }
        }
    }
}