package com.jetbrains.kmpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jetbrains.kmpapp.ui.AnimeKmpApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AnimeKmpApp() }
    }
}
