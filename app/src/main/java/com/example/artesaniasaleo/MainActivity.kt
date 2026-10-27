package com.example.artesaniasaleo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.artesaniasaleo.navigation.AppNavegacion
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Punto de entrada: aplica el tema y entrega el control a la navegación
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtesaniasAleoTheme {
                AppNavegacion()
            }
        }
    }
}
