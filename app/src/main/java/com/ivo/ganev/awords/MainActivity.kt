package com.ivo.ganev.awords

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ivo.ganev.awords.databinding.ActivityMainBinding
import com.ivo.ganev.awords.view.TextViewClickable

class MainActivity : AppCompatActivity(R.layout.activity_main), TextViewClickable.OnWordClickedListener {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clickableText.onWordClickedListener = this

        binding.clickableText.makeTextClickable("Meeh Weee  AA")
    }

    override fun onWordClick(word: String) {
        println(word)
        binding.clickableText.replaceSelectedWord((0..100).random().toString())
    }
}
