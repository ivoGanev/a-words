package com.ivo.ganev.awords.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ivo.ganev.awords.EditorFragmentArguments
import com.ivo.ganev.awords.R
import com.ivo.ganev.awords.databinding.ActivityMainBinding
import com.ivo.ganev.awords.ui.main.fragments.MainFragmentDirections
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment).navController
    }

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(navController.graph)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //TODO: Remove when finished debugging
        navController.navigate(
            MainFragmentDirections.actionMainFragmentToEditorFragment(
                EditorFragmentArguments(
                    0,
                    Uri.parse("")
                )
            )
        )

        setSupportActionBar(binding.mainToolbar)
        setupActionBarWithNavController(navController)
        binding.mainToolbar.setupWithNavController(navController, appBarConfiguration)
    }


    override fun onNavigateUp(): Boolean {
        NavigationUI.navigateUp(navController, appBarConfiguration)
        return true
    }
}
