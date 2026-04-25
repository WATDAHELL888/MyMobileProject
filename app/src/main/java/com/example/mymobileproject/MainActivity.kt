package com.example.mymobileproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mymobileproject.navigation.SmartFinanceNavGraph
import com.example.mymobileproject.presentation.auth.LoginViewModel
import com.example.mymobileproject.ui.theme.SmartFinanceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartFinanceTheme(darkTheme = true) {
                val loginViewModel: LoginViewModel = hiltViewModel()
                val isLoggedIn = loginViewModel.isLoggedIn()
                SmartFinanceNavGraph(isLoggedIn = isLoggedIn)
            }
        }
    }
}