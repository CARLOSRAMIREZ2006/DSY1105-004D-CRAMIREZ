package com.example.hola_mundo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // (1) Declarar variables requeridas por la guía
    val nombreUsuario: String = "Ana"
    var edadUsuario: Int = 15
    var promedioNotas: Double = 6.5
    val esMayorDeEdad: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // (3) Invocar funciones y mostrar el resultado en pantalla
        val saludo = crearSaludo(nombreUsuario, edadUsuario)
        val esMayor = calcularMayoriaEdad(edadUsuario)
        val mensajeFinal = "$saludo ¿Es mayor de edad? $esMayor"

        mostrarResultado(mensajeFinal)
    }

    // (2) Crear funciones solicitadas
    fun crearSaludo(nombre: String, edad: Int): String {
        return "Hola $nombre, tienes $edad años."
    }

    fun calcularMayoriaEdad(edad: Int): Boolean {
        return edad >= 18
    }

    fun mostrarResultado(mensaje: String) {
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = mensaje
    }
}