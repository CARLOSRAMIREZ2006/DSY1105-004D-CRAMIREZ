package com.example.insertcode.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BarraInferior
import com.example.insertcode.viewmodels.GuardianViewModel

// ---------------------------------------------------------------------
//  Datos de demostración (mismos que las capturas de referencia)
// ---------------------------------------------------------------------
private data class Pedido(
    val id: String,
    val cliente: String,
    val direccion: String,
    val estado: String,
    val total: String
)

private data class Producto(
    val codigo: String,
    val categoria: String,
    val nombre: String,
    val descripcion: String,
    val precio: String
)

private data class Parada(
    val nombre: String,
    val direccion: String,
    val estado: String
)

private val pedidosDemo = listOf(
    Pedido("PED-001", "Duoc UC Sede Alameda", "Av. España 8, Santiago Centro", "PENDIENTE", "\$450.000"),
    Pedido("PED-002", "Empresa Tech Chile Ltda", "Av. Providencia 1234, Of. 502", "EN_RUTA", "\$120.000"),
    Pedido("PED-003", "Comercial Central InsertCode", "Av. Andrés Bello 2680, Las Condes", "ENTREGADO", "\$180.000")
)

private val productosDemo = listOf(
    Producto("PROD-01", "SOFTWARE", "Licencia Dashboard Web Sales", "Sistema centralizado de ventas, inventario e indicadores.", "\$450.000"),
    Producto("PROD-02", "HARDWARE", "Módulo IoT Rastreador GPS", "Hardware de rastreo vehicular en tiempo real para flotas.", "\$120.000"),
    Producto("PROD-03", "HARDWARE", "Terminal POS Móvil Android", "Dispositivo táctil portátil con lector de comprobantes.", "\$180.000")
)

private val paradasDemo = listOf(
    Parada("Duoc UC Sede Alameda", "Av. España 8, Santiago Centro", "PENDIENTE"),
    Parada("Empresa Tech Chile Ltda", "Av. Providencia 1234, Of. 502", "EN_RUTA"),
    Parada("Comercial Central InsertCode", "Av. Andrés Bello 2680, Las Condes", "ENTREGADO")
)

// Posiciones relativas (0 a 1) de la base y las paradas en el mapa
private val puntosMapa = listOf(
    Pair(0.12f, 0.72f),
    Pair(0.36f, 0.46f),
    Pair(0.62f, 0.20f),
    Pair(0.88f, 0.60f)
)

private val ColorPendiente = Color(0xFFF59E0B)
private val ColorEnRuta = Color(0xFF3B82F6)
private val ColorEntregado = Color(0xFF22C55E)

private fun colorDeEstado(estado: String): Color = when (estado) {
    "PENDIENTE" -> ColorPendiente
    "EN_RUTA" -> ColorEnRuta
    else -> ColorEntregado
}

// ---------------------------------------------------------------------
//  Componentes compartidos
// ---------------------------------------------------------------------
@Composable
private fun Etiqueta(texto: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Encabezado(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TarjetaOscura(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF1F2937)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            contenido()
        }
    }
}

@Composable
private fun Kpi(modifier: Modifier, titulo: String, valor: String, detalle: String, color: Color) {
    TarjetaOscura(modifier = modifier) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(
            detalle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------
//  INICIO (Dashboard comercial)
// ---------------------------------------------------------------------
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(topBar = { CabeceraApp(navController) }, bottomBar = { BarraInferior(Rutas.INICIO, navController, 0) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Encabezado(
                    "Panel Comercial & Operacional",
                    "Indicadores clave de ventas y cumplimiento de entregas en terreno"
                )
            }
            item {
                Button(
                    onClick = { navController.navigate(Rutas.PEDIDOS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nuevo Pedido")
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kpi(Modifier.weight(1f), "VENTAS TOTALES", "\$1.780.000", "↑ Metas actualizadas", ColorEntregado)
                    Kpi(Modifier.weight(1f), "PENDIENTES", "1", "Por despachar", ColorPendiente)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kpi(Modifier.weight(1f), "EN RUTA", "1", "Repartidores activos", ColorEnRuta)
                    Kpi(Modifier.weight(1f), "ENTREGADOS", "1", "Con firma digital", ColorEntregado)
                }
            }
            item {
                TarjetaOscura {
                    Text(
                        "Estado de Pedidos en Terreno",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        Box(Modifier.weight(1f).height(12.dp).background(ColorPendiente))
                        Box(Modifier.weight(1f).height(12.dp).background(ColorEnRuta))
                        Box(Modifier.weight(1f).height(12.dp).background(ColorEntregado))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Pendientes (1)  ·  En ruta (1)  ·  Entregados (1)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                TarjetaOscura {
                    Text(
                        "Acciones Rápidas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Rutas.PEDIDOS) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Gestionar Pedidos", modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Rutas.CATALOGO) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Ver Catálogo", modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
//  PEDIDOS
// ---------------------------------------------------------------------
@Composable
fun PedidosScreen(navController: NavController) {
    Scaffold(topBar = { CabeceraApp(navController) }, bottomBar = { BarraInferior(Rutas.PEDIDOS, navController, 0) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Encabezado("Pedidos", "Seguimiento de pedidos en terreno")
            }
            items(pedidosDemo) { pedido ->
                TarjetaOscura {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            pedido.id,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Etiqueta(pedido.estado, colorDeEstado(pedido.estado))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(pedido.cliente, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        pedido.direccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        pedido.total,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorEntregado
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
//  CATÁLOGO
// ---------------------------------------------------------------------
@Composable
fun CatalogoScreen(navController: NavController) {
    var cotizando by remember { mutableStateOf<Producto?>(null) }

    Scaffold(topBar = { CabeceraApp(navController) }, bottomBar = { BarraInferior(Rutas.CATALOGO, navController, 0) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Encabezado("Catálogo de Soluciones", "Productos y licencias InsertCode disponibles para cotización")
            }
            items(productosDemo) { producto ->
                TarjetaOscura {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Etiqueta(producto.categoria, ColorEnRuta)
                        Spacer(Modifier.weight(1f))
                        Text(
                            producto.codigo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        producto.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            producto.precio,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ColorEntregado,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { cotizando = producto }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Cotizar")
                        }
                    }
                }
            }
        }
    }

    cotizando?.let { producto ->
        AlertDialog(
            onDismissRequest = { cotizando = null },
            title = { Text("Cotización") },
            text = { Text("Se preparará una cotización para ${producto.nombre} (${producto.codigo}) por ${producto.precio}.") },
            confirmButton = {
                TextButton(onClick = { cotizando = null }) { Text("Aceptar") }
            }
        )
    }
}

// ---------------------------------------------------------------------
//  RUTA GPS
// ---------------------------------------------------------------------
private val etiquetasMapa = listOf("Base InsertCode", "Duoc UC Alameda", "Tech Chile", "Comercial Las Condes")
private val coloresMapa = listOf(ColorEntregado, ColorPendiente, ColorEnRuta, ColorEnRuta)

@Composable
fun RutaGpsScreen(navController: NavController) {
    Scaffold(topBar = { CabeceraApp(navController) }, bottomBar = { BarraInferior(Rutas.RUTA_GPS, navController, 0) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Encabezado("Ruta GPS de Despacho", "Seguimiento de entregas asignadas en terreno (coordenadas simuladas)")
            }
            item {
                TarjetaOscura {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(Modifier.width(6.dp))
                        Text("Mapa de Ruta Interactiva", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Etiqueta("GPS Activo", ColorEntregado)
                    }
                    Spacer(Modifier.height(12.dp))
                    MapaRuta()
                }
            }
            item {
                Text(
                    "Secuencia de Entregas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            itemsIndexed(paradasDemo) { indice, parada ->
                TarjetaOscura {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2937)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${indice + 1}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(parada.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                parada.direccion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Etiqueta(parada.estado, colorDeEstado(parada.estado))
                    }
                }
            }
        }
    }
}

@Composable
private fun MapaRuta() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0B1120))
    ) {
        // Cuadrícula de fondo, como en el mapa de referencia
        for (i in 1 until 6) {
            val x = size.width * i / 6f
            drawLine(color = Color(0xFF1F2937), start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
        }
        for (j in 1 until 5) {
            val y = size.height * j / 5f
            drawLine(color = Color(0xFF1F2937), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
        }

        val puntos = puntosMapa.map { Offset(it.first * size.width, it.second * size.height) }

        // Ruta punteada entre paradas
        for (i in 0 until puntos.size - 1) {
            drawLine(
                color = ColorEnRuta,
                start = puntos[i],
                end = puntos[i + 1],
                strokeWidth = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f))
            )
        }

        // Puntos con su nombre debajo
        val texto = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 12.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        puntos.forEachIndexed { i, punto ->
            drawCircle(color = coloresMapa[i], radius = 10.dp.toPx(), center = punto)
            drawContext.canvas.nativeCanvas.drawText(
                etiquetasMapa[i],
                punto.x,
                punto.y + 26.dp.toPx(),
                texto
            )
        }
    }
}

// ---------------------------------------------------------------------
//  PERFIL (vendedor)
// ---------------------------------------------------------------------
@Composable
fun PerfilVendedorScreen(navController: NavController, viewModel: GuardianViewModel) {
    val sesion by viewModel.sesion.collectAsState()
    val rol by viewModel.rol.collectAsState()
    val permisos = listOf(
        "Consulta de catálogo de soluciones y clientes",
        "Registro y cotización de nuevos pedidos",
        "Consulta de estados de despacho"
    )

    Scaffold(topBar = { CabeceraApp(navController) }, bottomBar = { BarraInferior(Rutas.PERFIL, navController, 0) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Encabezado("Mi Perfil Corporativo", "Información de cuenta Firebase, permisos asignados y preferencias")
            }
            item {
                TarjetaOscura {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Text(sesion.nombre.ifBlank { "Usuario" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Etiqueta(rol?.etiqueta?.uppercase() ?: "SIN ROL", ColorEnRuta)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(sesion.correo.ifBlank { "Sin correo" }, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sede Alameda - Santiago", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("+56 9 8765 4321", style = MaterialTheme.typography.bodySmall)
                        }
                        Etiqueta("Firebase Auth Verificado", ColorEntregado)
                    }
                }
            }
            item {
                TarjetaOscura {
                    Text(
                        "Permisos del Perfil (VENDEDOR)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    permisos.forEach { permiso ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(permiso, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Check, contentDescription = null, tint = ColorEntregado, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Activo", style = MaterialTheme.typography.labelSmall, color = ColorEntregado)
                        }
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        viewModel.cerrarSesion {
                            navController.navigate(Rutas.LOGIN) {
                                popUpTo(Rutas.INICIO) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
//  Encabezado de la app (logo, nombre, badge, botones de la captura)
// ---------------------------------------------------------------------
@Composable
private fun CabeceraApp(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2563EB)),
            contentAlignment = Alignment.Center
        ) {
            Text("</>", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("InsertCode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Etiqueta("DSY1105", ColorEnRuta)
            }
            Text(
                "Ventas & Distribución Móvil",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF0F2A22))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ColorEntregado)
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { navController.navigate(Rutas.PERFIL) },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF111827))
        ) {
            Icon(Icons.Default.Work, contentDescription = "Perfil")
        }
        IconButton(
            onClick = {
                navController.navigate(Rutas.LOGIN) {
                    popUpTo(Rutas.INICIO) { inclusive = true }
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF111827))
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
        }
    }
}

// ---------------------------------------------------------------------
//  ROOM / SYNC (quinta pestaña de la barra)
// ---------------------------------------------------------------------
@Composable
fun RoomSyncScreen(navController: NavController) {
    Scaffold(topBar = { CabeceraApp(navController) }, bottomBar = { BarraInferior(Rutas.ROOM_SYNC, navController, 0) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Encabezado("Room / Sync", "Base de datos local y sincronización de pedidos")
            }
            item {
                TarjetaOscura {
                    Text("Estado de sincronización", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Etiqueta("SINCRONIZADO", ColorEntregado)
                }
            }
            item {
                TarjetaOscura {
                    Text("Registros locales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pedidos: 3  ·  Productos: 3  ·  Paradas: 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
