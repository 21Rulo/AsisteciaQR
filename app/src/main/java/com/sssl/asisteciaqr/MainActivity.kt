package com.sssl.asisteciaqr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.sssl.asisteciaqr.data.AppDatabase
import com.sssl.asisteciaqr.data.repository.AsistenciaRepository
import com.sssl.asisteciaqr.navigation.AppNavigation
import com.sssl.asisteciaqr.ui.theme.AsistenciaQRTheme
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AsistenciaViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = AsistenciaRepository(
                    database.grupoDao(),
                    database.alumnoDao(),
                    database.asistenciaDao()
                )
                @Suppress("UNCHECKED_CAST")
                return AsistenciaViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsistenciaQRTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}