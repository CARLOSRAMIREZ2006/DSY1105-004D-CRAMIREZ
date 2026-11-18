package com.example.insertcode.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.data.EventoEntity
import com.example.insertcode.model.CategoriaEvento
import com.example.insertcode.model.EstadoEvento
import com.example.insertcode.model.SeveridadEvento
import com.example.insertcode.model.textoEstado
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.theme.ColorAdvertencia
import com.example.insertcode.ui.theme.ColorCritico
import com.example.insertcode.ui.theme.ColorExito
import com.example.insertcode.ui.theme.ColorInfo
import com.example.insertcode.util.formatoHora

// ---------------------------------------------------------------------
//  Barra de navegación inferior (Inicio · Cámaras · Eventos · Alertas · Perfil)
// ---------------------------------------------------------------------
private data class ItemBarra(val ruta: String, val titulo: String, val icono: ImageVector)

@Composable
fun BarraInferior(
    seleccionada: String,
    navController: NavController,
    alertasPendientes: Int
) {
    val items = listOf(
        ItemBarra(Rutas.INICIO, "Dashboard", Icons.Default.Dashboard),
        ItemBarra(Rutas.PEDIDOS, "Pedidos", Icons.Default.ShoppingCart),
        ItemBarra(Rutas.CATALOGO, "Catálogo", Icons.Default.Storefront),
        ItemBarra(Rutas.RUTA_GPS, "Ruta GPS", Icons.Default.LocationOn),
        ItemBarra(Rutas.ROOM_SYNC, "Room / Sync", Icons.Default.Storage)
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = seleccionada == item.ruta,
                onClick = {
                    navController.navigate(item.ruta) {
                        popUpTo(Rutas.INICIO) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (item.ruta == Rutas.ALERTAS && alertasPendientes > 0) {
                        BadgedBox(badge = { Badge { Text("$alertasPendientes") } }) {
                            Icon(item.icono, contentDescription = item.titulo)
                        }
                    } else {
                        Icon(item.icono, contentDescription = item.titulo)
                    }
                },
                label = { Text(item.titulo) }
            )
        }
    }
}

// ---------------------------------------------------------------------
//  Etiqueta (chip de solo lectura) con color semántico
// ---------------------------------------------------------------------
@Composable
fun ChipEtiqueta(texto: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

// Bloque pequeño "valor + etiqueta" usado en resúmenes
@Composable
fun BloqueEstadistica(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------------------------------------------------------------------
//  Colores / iconos según el evento
// ---------------------------------------------------------------------
fun colorSeveridad(severidad: String): Color = when (severidad) {
    SeveridadEvento.CRITICA -> ColorCritico
    SeveridadEvento.MEDIA -> ColorAdvertencia
    else -> ColorExito
}

fun colorEstado(estado: String): Color = when (estado) {
    EstadoEvento.PENDIENTE -> ColorAdvertencia
    EstadoEvento.CONFIRMADA, EstadoEvento.AUTORIZADO -> ColorExito
    EstadoEvento.RESUELTO -> ColorInfo
    EstadoEvento.ESCALADA -> ColorCritico
    else -> Color.Gray
}

fun iconoEvento(e: EventoEntity): ImageVector = when {
    e.categoria == CategoriaEvento.ACCESO -> Icons.Default.MeetingRoom
    e.titulo.contains("Cámara", ignoreCase = true) -> Icons.Default.VideocamOff
    e.titulo.contains("Ronda", ignoreCase = true) -> Icons.Default.DirectionsWalk
    e.titulo.contains("Batería", ignoreCase = true) -> Icons.Default.BatteryAlert
    e.categoria == CategoriaEvento.ALERTA -> Icons.Default.Warning
    else -> Icons.Default.Info
}

// ---------------------------------------------------------------------
//  Tarjeta de evento (historial y alertas)
// ---------------------------------------------------------------------
@Composable
fun TarjetaEvento(
    evento: EventoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = colorSeveridad(evento.severidad)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Franja lateral con el color de severidad
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconoEvento(evento), contentDescription = null, tint = color)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(evento.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(evento.ubicacion, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${evento.fecha.formatoHora()} · ${evento.detalle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipEtiqueta(evento.estado.textoEstado(), colorEstado(evento.estado))
                    Text(
                        text = evento.camara,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
