package com.ivo.ganev.awords

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ivo.ganev.awords.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
    }
}
