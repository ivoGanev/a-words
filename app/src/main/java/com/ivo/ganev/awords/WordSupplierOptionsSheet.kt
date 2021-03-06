package com.ivo.ganev.awords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ivo.ganev.awords.extensions.filterTickedCheckboxWithTag

@Suppress("unused")
class WordSupplierOptionsSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.include_editor_bottom, container, false)
    }

    override fun onPause() {
        super.onPause()
    }

    companion object {
        fun newInstance(): WordSupplierOptionsSheet {
            return WordSupplierOptionsSheet()
        }
    }


}