package com.example.insertcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.insertcode.ui.HomeScreen
import com.example.insertcode.ui.theme.InsertcodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InsertcodeTheme {
                // Llamamos a la pantalla base creada en la Guía 8
                HomeScreen()
            }
        }
    }
}

// Vista previa de la pantalla principal dentro de MainActivity
@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    InsertcodeTheme {
        HomeScreen()
    }
}