package com.ivo.ganev.awords

import android.content.Context
import android.widget.ArrayAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.ui.main_activity.fragments.EditorViewModel
import com.ivo.ganev.awords.view.AutoCompleteEditText
import timber.log.Timber

class EditorModeAutoComplete(
    private val binding: FragmentEditorBinding,
    private val context: Context,
    private val viewModel: EditorViewModel
) :
    LifecycleObserver,
    AutoCompleteEditText.OnFilteredTextChangeListener {

    private lateinit var arrayAdapter: ArrayAdapter<String>

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onViewCreated() {
        arrayAdapter =
            ArrayAdapter(context, R.layout.dropdown_autocomplete, arrayListOf())

        binding.editorEditText.apply {
            setAdapter(arrayAdapter)
            onFilteredTextChangeListener = this@EditorModeAutoComplete
        }


    }

    override fun onFilteredTextChanged(word: String) {
        Timber.d("Yey%s", word)
    }

    fun close() {

    }
}