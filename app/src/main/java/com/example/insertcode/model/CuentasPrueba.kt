package com.example.insertcode.model

// Cuentas FICTICIAS de prueba indicadas en el enunciado (sintéticas, nunca credenciales reales).
enum class RolUsuario(val etiqueta: String) {
    ADMIN("Admin"),
    SUPERVISOR("Supervisor"),
    OPERADOR("Operador")
}

data class CuentaPrueba(
    val correo: String,
    val clave: String,
    val rol: RolUsuario
)

object CuentasPrueba {
    // Lista fija de cuentas válidas para el login
    val lista = listOf(
        CuentaPrueba("admin@guardian.test", "123456", RolUsuario.ADMIN),
        CuentaPrueba("supervisor@guardian.test", "123456", RolUsuario.SUPERVISOR),
        CuentaPrueba("operador@guardian.test", "123456", RolUsuario.OPERADOR)
    )

    // Devuelve la cuenta si el correo y la clave coinciden (sin distinguir mayúsculas en el correo)
    fun buscar(correo: String, clave: String): CuentaPrueba? =
        lista.firstOrNull { it.correo.equals(correo.trim(), ignoreCase = true) && it.clave == clave }

    // Devuelve el rol asociado a un correo, o null si no es una cuenta de prueba
    fun rolDe(correo: String): RolUsuario? =
        lista.firstOrNull { it.correo.equals(correo.trim(), ignoreCase = true) }?.rol
}
