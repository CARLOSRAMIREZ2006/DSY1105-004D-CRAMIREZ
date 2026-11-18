package com.example.insertcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.insertcode.data.AppDatabase
import com.example.insertcode.data.GuardianPreferences
import com.example.insertcode.data.GuardianRepository
import com.example.insertcode.data.PreferencesManager
import com.example.insertcode.data.UsuarioRepository
import com.example.insertcode.navigation.AppNavigation
import com.example.insertcode.ui.theme.InsertcodeTheme
import com.example.insertcode.viewmodels.GuardianViewModelFactory
import com.example.insertcode.viewmodels.UsuarioViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicialización manual de dependencias (Base de datos -> Repository -> Factory)
        val database = AppDatabase.getDatabase(applicationContext)
        val preferences = PreferencesManager(applicationContext)
        val repository = UsuarioRepository(database.usuarioDao(), applicationContext)
        val usuarioFactory = UsuarioViewModelFactory(repository, preferences)

        // Parte "Guardián": eventos, incidencias, sesión y ajustes
        val guardianPrefs = GuardianPreferences(applicationContext)
        val guardianRepo = GuardianRepository(
            database.eventoDao(),
            database.incidenciaDao(),
            applicationContext
        )
        val guardianFactory = GuardianViewModelFactory(guardianRepo, guardianPrefs)

        setContent {
            // null = seguir el tema del sistema; true/false = elegido en Perfil > Modo oscuro
            val modoOscuro by guardianPrefs.modoOscuroFlow.collectAsState(initial = null)

            InsertcodeTheme(darkTheme = modoOscuro ?: true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        usuarioFactory = usuarioFactory,
                        guardianFactory = guardianFactory
                    )
                }
            }
        }
    }
}
