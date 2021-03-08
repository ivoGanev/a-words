package com.ivo.ganev.awords.ui.main

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.ivo.ganev.awords.databinding.FragmentEditorBinding
import com.ivo.ganev.awords.ui.main.fragments.EditorViewModel

abstract class EditorScreenBase(
    protected val context: Context,
    protected val binding: FragmentEditorBinding,
    protected val viewModel: EditorViewModel,
    protected val lifeCycleOwner: LifecycleOwner
) : LifecycleObserver, View.OnClickListener {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    abstract fun onCreate()
}