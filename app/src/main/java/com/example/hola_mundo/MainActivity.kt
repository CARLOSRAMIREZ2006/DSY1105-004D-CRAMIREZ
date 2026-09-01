import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

open class Entrada(
    val id: Int,
    val precio: Double
) {
    open fun mostrarDetalle() {
        println("ID Entrada: $id | Precio: $$precio")
    }
}

class EntradaGeneral(
    id: Int,
    precio: Double
) : Entrada(id, precio) {
    override fun mostrarDetalle() {
        println("[ENTRADA GENERAL] ID: $id | Precio: $$precio | Acceso: Zona General")
    }
}

class EntradaVIP(
    id: Int,
    precio: Double,
    val beneficiosExtra: String
) : Entrada(id, precio) {
    override fun mostrarDetalle() {
        println("[ENTRADA VIP]     ID: $id | Precio: $$precio | Beneficios: $beneficiosExtra")
    }
}

sealed class EstadoValidacion {
    object Validando : EstadoValidacion()
    data class Valida(val entrada: Entrada) : EstadoValidacion()
    data class NoValida(val mensajeError: String) : EstadoValidacion()
}

suspend fun validarEntrada(id: Int, lista: List<Entrada>): EstadoValidacion {
    println("Consultando sistema para validar la entrada ID $id...")

    delay(2000)

    val entradaEncontrada = lista.find { it.id == id }

    return if (entradaEncontrada != null) {
        EstadoValidacion.Valida(entradaEncontrada)
    } else {
        EstadoValidacion.NoValida("No existe ninguna entrada registrada con el ID $id.")
    }
}


fun main() = runBlocking {
    println("=== SISTEMA DE GESTIÓN Y VALIDACIÓN DE ENTRADAS ===\n")

    val listaEntradas: List<Entrada> = listOf(
        EntradaGeneral(id = 1, precio = 15000.0),
        EntradaGeneral(id = 2, precio = 15000.0),
        EntradaVIP(id = 3, precio = 45000.0, beneficiosExtra = "Bar abierto + Estacionamiento"),
        EntradaGeneral(id = 4, precio = 15000.0),
        EntradaVIP(id = 5, precio = 60000.0, beneficiosExtra = "Meet & Greet + Polera oficial")
    )

    println("--- Lista de Entradas Vendidas ---")
    listaEntradas.forEach { entrada ->
        entrada.mostrarDetalle()
    }

    val ingresoTotal = listaEntradas.sumOf { it.precio }
    val cantidadVIP = listaEntradas.count { it is EntradaVIP }

    println("\n--- Métricas y Análisis de Ventas ---")
    println("Ingreso total generado: $$ingresoTotal")
    println("Total de entradas VIP vendidas: $cantidadVIP")

    println("\n--- Proceso de Validación de Entradas (Asíncrono) ---")

    val idsAProbar = listOf(3, 99)

    for (id in idsAProbar) {
        println("\nEnviando solicitud de validación...")

        val estadoActual = validarEntrada(id, listaEntradas)

        when (estadoActual) {
            is EstadoValidacion.Validando -> {
                println("Estado: Proceso de validación en curso...")
            }
            is EstadoValidacion.Valida -> {
                println("Estado: ¡Entrada VÁLIDA!")
                print("Detalle de la entrada confirmada -> ")
                estadoActual.entrada.mostrarDetalle()
            }
            is EstadoValidacion.NoValida -> {
                println("Estado: Entrada NO VÁLIDA")
                println("Razón: ${estadoActual.mensajeError}")
            }
        }
    }
}