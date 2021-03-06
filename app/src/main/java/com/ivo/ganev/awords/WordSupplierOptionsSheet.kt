package com.ivo.ganev.awords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ivo.ganev.awords.databinding.IncludeEditorBottomBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


@Suppress("unused")
class WordSupplierOptionsSheet : BottomSheetDialogFragment() {
    lateinit var binding: IncludeEditorBottomBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.include_editor_bottom, container, false)
        binding = IncludeEditorBottomBinding.bind(view)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            requireContext().settingsDataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(Settings.getDefaultInstance())
                    } else {
                        throw exception
                    }
                }.collect { settings ->
                    with(settings)
                    {
                        with(binding) {
                            editorPopupDatamuseSyn.isChecked = chkboxSynonyms
                            editorPopupDatamuseAnt.isChecked = chkboxAntonyms
                            editorPopupDatamuseRhy.isChecked = chkboxRhymes
                            editorPopupDatamuseHom.isChecked = chkboxHomophones
                            editorPopupDatamusePopAdj.isChecked = chkboxPopAdj
                            editorPopupDatamusePopNoun.isChecked = chkboxPopNouns
                            editorPopupRandomNoun.isChecked = chkboxNoun
                            editorPopupRandomAdj.isChecked = chkboxAdj
                            editorPopupRandomVerb.isChecked = chkboxVerb
                            editorPopupRandomAdverb.isChecked = chkboxAdverb
                        }
                    }
                }
        }
    }

    override fun onPause() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                requireContext().settingsDataStore.updateData { currentSettings ->
                    with(binding) {
                        currentSettings.toBuilder()
                            .setChkboxSynonyms(editorPopupDatamuseSyn.isChecked)
                            .setChkboxAntonyms(editorPopupDatamuseAnt.isChecked)
                            .setChkboxRhymes(editorPopupDatamuseRhy.isChecked)
                            .setChkboxHomophones(editorPopupDatamuseHom.isChecked)
                            .setChkboxPopAdj(editorPopupDatamusePopAdj.isChecked)
                            .setChkboxPopNouns(editorPopupDatamusePopNoun.isChecked)
                            .setChkboxNoun(editorPopupRandomNoun.isChecked)
                            .setChkboxAdj(editorPopupRandomAdj.isChecked)
                            .setChkboxVerb(editorPopupRandomVerb.isChecked)
                            .setChkboxAdverb(editorPopupRandomAdverb.isChecked)
                            .build()
                    }
                }
            }
        }
        super.onPause()
    }

    companion object {
        fun newInstance(): WordSupplierOptionsSheet {
            return WordSupplierOptionsSheet()
        }
    }


}