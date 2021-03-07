package com.ivo.ganev.awords.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.data.UserSettingsRepository
import com.ivo.ganev.awords.data.settingsDataStore
import com.ivo.ganev.awords.databinding.IncludeEditorBottomBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Suppress("unused")
class WordSupplierOptionsSheetFragment : BottomSheetDialogFragment() {
    private var _binding: IncludeEditorBottomBinding? = null
    private val binding get() = _binding!!

    private var _userSettings: UserSettingsRepository? = null
    private val userSettings get() = _userSettings!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.include_editor_bottom, container, false)
        _binding = IncludeEditorBottomBinding.bind(view)
        _userSettings = UserSettingsRepository(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            userSettings.settingsFlow.collect { settings ->
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
                storeSettings()
            }
        }
        super.onPause()
    }

    companion object {
        fun newInstance(): WordSupplierOptionsSheetFragment {
            return WordSupplierOptionsSheetFragment()
        }
    }

    private suspend fun storeSettings() {
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

    // TODO:// Figure out a way to release the resources, but be careful because they are used in
    //  a background thread.
    private fun releaseResources() {
        _binding = null
        _userSettings = null
    }
}