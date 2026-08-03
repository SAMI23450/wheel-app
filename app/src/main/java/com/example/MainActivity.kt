package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WheelViewModel
import com.example.ui.viewmodel.WheelViewModelFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize Database, DAO, Repository & ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val appDao = database.appDao()
        val repository = AppRepository(appDao, applicationContext)
        val viewModelFactory = WheelViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[WheelViewModel::class.java]

        enableEdgeToEdge()
        
        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val isDark = when (appTheme) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                // Main Switcher: Splash -> Main App Content
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
