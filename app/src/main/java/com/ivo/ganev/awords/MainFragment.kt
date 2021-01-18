package com.ivo.ganev.awords

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivo.ganev.awords.databinding.MainFragmentBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class MainFragment : Fragment(R.layout.main_fragment), View.OnClickListener {

    private val openRequestCode: Int = 1

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonOpenFile.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.type = "text/plain";
        startActivityForResult(intent, openRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        var currentUri: Uri? = null
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == openRequestCode) {
                if (resultData != null) {
                    println(resultData.data)
                    val stream = context?.contentResolver?.openInputStream(resultData.data!!)
                    val reader = BufferedReader(InputStreamReader(stream))
                    println(reader.readText())
                }
            }
        }
    }
}
