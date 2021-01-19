package com.ivo.ganev.awords

import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivo.ganev.awords.databinding.MainFragmentBinding
import com.ivo.ganev.awords.extensions.isWithId
import java.io.*


class MainFragment : Fragment(R.layout.main_fragment) {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MainFragmentBinding.inflate(inflater)
        return binding.root
    }
}
