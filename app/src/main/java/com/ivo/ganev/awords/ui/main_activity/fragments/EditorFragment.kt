package com.ivo.ganev.awords.ui.main_activity.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.ui.main_activity.MainActivity
import com.ivo.ganev.awords.view.TextViewWordMutator
import com.ivo.ganev.datamuse_kotlin.response.WordResponse
import com.ivo.ganev.datamuse_kotlin.response.WordResponse.Element.Word

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
        binding.contentTextview.setClickableText(args.content)

        when (binding.include.Type.checkedRadioButtonId) {
            binding.include.button1.id -> viewModel.type = EditorViewModel.Type.Synonyms
            binding.include.button2.id -> viewModel.type = EditorViewModel.Type.Antonyms
            binding.include.button3.id -> viewModel.type = EditorViewModel.Type.Rhymes
        }

        binding.include.Type.setOnCheckedChangeListener { _, index ->
            when (index) {
                0 -> viewModel.type = EditorViewModel.Type.Synonyms
                1 -> viewModel.type = EditorViewModel.Type.Antonyms
                2 -> viewModel.type = EditorViewModel.Type.Rhymes
            }
        }

        binding.contentTextview.onWordClickedListener =
            object : TextViewWordMutator.OnWordClickedListener {
                override fun onWordClick(word: String) {
                    val query = viewModel.makeQuery(word, viewModel.type)
                    println(query.build().toUrl())
                    viewModel.fireUpQuery(query)
                }
            }

        viewModel.results.observe(viewLifecycleOwner) { wordResponses ->
            val randomWord = wordResponses
                .flatMap { it.elements }
                .filterIsInstance<Word>()

            if (randomWord.isNotEmpty())
                binding.contentTextview.replaceSelectedWord(randomWord.random().word)
        }
    }
}

