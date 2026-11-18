package com.example.insertcode.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.model.EstadoCamara
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BarraInferior
import com.example.insertcode.ui.components.BloqueEstadistica
import com.example.insertcode.ui.components.ChipEtiqueta
import com.example.insertcode.ui.components.ImagenInteligente
import com.example.insertcode.ui.theme.ColorExito
import com.example.insertcode.ui.theme.ColorInfo
import com.example.insertcode.ui.utils.crearUriTemporal
import com.example.insertcode.util.aUri
import com.example.insertcode.viewmodels.GuardianViewModel
import kotlinx.coroutines.launch

private const val CORREO_SOPORTE = "soporte@insercode.cl"

// Perfil del guardián: cuenta, seguridad, ajustes de la app y cierre de sesión
@Composable
fun PerfilGuardianScreen(
    navController: NavController,
    viewModel: GuardianViewModel
) {
    val sesion by viewModel.sesion.collectAsState()
    val ajustes by viewModel.ajustes.collectAsState()
    val camaras by viewModel.camaras.collectAsState()
    val resumen by viewModel.resumenTurno.collectAsState()
    val pendientes by viewModel.alertasPendientes.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var dialogo by rememberSaveable { mutableStateOf<String?>(null) }
    var uriTemporal by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.actualizarFotoPerfil(it) }
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok: Boolean ->
        val u = uriTemporal
        if (ok && u != null) viewModel.actualizarFotoPerfil(u)
    }

    val modoOscuroActivo = ajustes.modoOscuro ?: isSystemInDarkTheme()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { BarraInferior(Rutas.PERFIL, navController, pendientes) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Mi Perfil", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Cuenta y preferencias del turno",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ---------- Tarjeta de identidad ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ImagenInteligente(imagenUri = sesion.fotoPath.aUri(), tamano = 80.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${sesion.nombre} — Guardián", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(sesion.correo.ifBlank { "guardian@insercode.cl" }, style = MaterialTheme.typography.bodySmall)
                            ChipEtiqueta("✔ Correo verificado", ColorExito, Modifier.padding(top = 6.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipEtiqueta("Guardián • Zona Sur", ColorInfo)
                        ChipEtiqueta("Turno Activo", ColorExito)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                val uri = crearUriTemporal(context)
                                uriTemporal = uri
                                camara.launch(uri)
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Tomar foto") }
                        OutlinedButton(
                            onClick = { galeria.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Galería") }
                    }
                }
            }

            // ---------- Resumen del turno ----------
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                BloqueEstadistica("14:00 – 22:00", "Turno", Modifier.weight(1.3f))
                BloqueEstadistica("${camaras.size}", "Cámaras asignadas", Modifier.weight(1f))
                BloqueEstadistica("${resumen.alertasResueltasHoy}", "Alertas resueltas hoy", Modifier.weight(1f))
            }

            // ---------- Mi cuenta ----------
            SeccionAjustes("MI CUENTA") {
                FilaAjuste(
                    icono = Icons.Default.Videocam,
                    titulo = "Mis cámaras asignadas",
                    detalle = "${camaras.size} cámaras · ${camaras.count { it.estado == EstadoCamara.EN_LINEA }} en línea",
                    onClick = { navController.navigate(Rutas.CAMARAS) }
                )
                HorizontalDivider()
                FilaAjuste(
                    icono = Icons.Default.History,
                    titulo = "Historial de turnos",
                    detalle = "Turnos anteriores y registros",
                    onClick = { navController.navigate(Rutas.EVENTOS) }
                )
                HorizontalDivider()
                FilaAjuste(
                    icono = Icons.Default.Badge,
                    titulo = "Certificado de guardia / Credencial",
                    detalle = "Documento vigente",
                    onClick = { dialogo = "credencial" }
                )
            }

            // ---------- Seguridad ----------
            SeccionAjustes("SEGURIDAD") {
                FilaAjuste(
                    icono = Icons.Default.Key,
                    titulo = "Cambiar contraseña",
                    onClick = { dialogo = "clave" }
                )
                HorizontalDivider()
                FilaAjuste(
                    icono = Icons.Default.Devices,
                    titulo = "Sesiones activas",
                    detalle = "Dispositivo actual",
                    onClick = { dialogo = "sesiones" },
                    destacado = "1 dispositivo"
                )
                HorizontalDivider()
                FilaSwitch(
                    icono = Icons.Default.VerifiedUser,
                    titulo = "Verificación en 2 pasos",
                    detalle = if (ajustes.dosPasos) "Protección adicional activada" else "Protección adicional desactivada",
                    activo = ajustes.dosPasos,
                    onCambio = viewModel::setDosPasos
                )
                HorizontalDivider()
                FilaSwitch(
                    icono = Icons.Default.Notifications,
                    titulo = "Notificaciones Push",
                    detalle = if (ajustes.push) "Avisos activados" else "Avisos desactivados",
                    activo = ajustes.push,
                    onCambio = viewModel::setPush
                )
                HorizontalDivider()
                FilaSwitch(
                    icono = Icons.Default.NotificationsActive,
                    titulo = "Alertas críticas",
                    detalle = "Siempre visibles y sonoras",
                    activo = ajustes.alertasCriticas,
                    onCambio = viewModel::setAlertasCriticas
                )
            }

            // ---------- App ----------
            SeccionAjustes("APP") {
                FilaSwitch(
                    icono = Icons.Default.DarkMode,
                    titulo = "Modo oscuro",
                    detalle = if (modoOscuroActivo) "Tema oscuro aplicado" else "Tema claro aplicado",
                    activo = modoOscuroActivo,
                    onCambio = viewModel::setModoOscuro
                )
                HorizontalDivider()
                FilaAjuste(
                    icono = Icons.Default.Email,
                    titulo = "Ayuda y soporte Insercode",
                    detalle = "Guías y contacto",
                    onClick = { dialogo = "ayuda" }
                )
                HorizontalDivider()
                FilaAjuste(
                    icono = Icons.Default.Description,
                    titulo = "Términos y políticas de seguridad",
                    onClick = { dialogo = "terminos" }
                )
            }

            // ---------- Datos locales (pantallas originales de la guía) ----------
            SeccionAjustes("DATOS LOCALES") {
                FilaAjuste(
                    icono = Icons.Default.People,
                    titulo = "Usuarios registrados (offline)",
                    detalle = "Listado guardado en ROOM",
                    onClick = { navController.navigate(Rutas.LISTADO) }
                )
                HorizontalDivider()
                FilaAjuste(
                    icono = Icons.Default.Badge,
                    titulo = "Registrar nuevo usuario",
                    detalle = "Formulario con foto y modo Premium",
                    onClick = { navController.navigate(Rutas.REGISTRO) }
                )
            }

            OutlinedButton(
                onClick = { dialogo = "cerrar" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("  Cerrar Sesión", color = MaterialTheme.colorScheme.error)
            }

            Text(
                "Insercode v1.0 • Protegido",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // ---------------------------------------------------------------------
    //  Diálogos
    // ---------------------------------------------------------------------
    when (dialogo) {
        "credencial" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            icon = { Icon(Icons.Default.Badge, contentDescription = null) },
            title = { Text("Credencial de guardia") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nombre: ${sesion.nombre}")
                    Text("Cargo: Guardián de seguridad")
                    Text("Zona: Sur")
                    Text("N° credencial: #4892")
                    Text("Estado: Vigente", color = ColorExito)
                }
            },
            confirmButton = { TextButton(onClick = { dialogo = null }) { Text("Cerrar") } }
        )

        "clave" -> DialogoCambiarClave(
            onCerrar = { dialogo = null },
            onGuardado = {
                dialogo = null
                scope.launch { snackbar.showSnackbar("Contraseña actualizada") }
            }
        )

        "sesiones" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            icon = { Icon(Icons.Default.Devices, contentDescription = null) },
            title = { Text("Sesiones activas") },
            text = { Text("1 dispositivo: ${Build.MANUFACTURER} ${Build.MODEL} (este dispositivo)") },
            confirmButton = { TextButton(onClick = { dialogo = null }) { Text("Cerrar") } }
        )

        "ayuda" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text("Ayuda y soporte") },
            text = {
                Text(
                    "• Alertas: confirma o descarta en menos de 2 minutos.\n" +
                        "• Cámaras: toca una cámara para verla en vivo.\n" +
                        "• Reportes: Eventos → Exportar reporte de turno.\n\n" +
                        "Contacto: $CORREO_SOPORTE"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogo = null
                    try {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$CORREO_SOPORTE")))
                    } catch (e: ActivityNotFoundException) {
                        scope.launch { snackbar.showSnackbar("No hay una app de correo instalada") }
                    }
                }) { Text("Escribir a soporte") }
            },
            dismissButton = { TextButton(onClick = { dialogo = null }) { Text("Cerrar") } }
        )

        "terminos" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text("Términos y políticas") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "1. El acceso es personal e intransferible.\n\n" +
                            "2. Las imágenes y eventos son confidenciales y solo pueden usarse para labores de seguridad.\n\n" +
                            "3. Toda alerta crítica debe atenderse dentro del tiempo del protocolo (2 minutos).\n\n" +
                            "4. Los reportes e incidencias quedan registrados con fecha, hora y autor.\n\n" +
                            "5. Cierra sesión al terminar tu turno."
                    )
                }
            },
            confirmButton = { TextButton(onClick = { dialogo = null }) { Text("Entendido") } }
        )

        "cerrar" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres salir de tu turno de monitoreo?") },
            confirmButton = {
                TextButton(onClick = {
                    dialogo = null
                    viewModel.cerrarSesion {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { dialogo = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun DialogoCambiarClave(onCerrar: () -> Unit, onGuardado: () -> Unit) {
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = actual, onValueChange = { actual = it; error = null },
                    label = { Text("Contraseña actual") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = nueva, onValueChange = { nueva = it; error = null },
                    label = { Text("Nueva contraseña") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmar, onValueChange = { confirmar = it; error = null },
                    label = { Text("Confirmar nueva contraseña") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = when {
                    actual.isBlank() -> "Ingresa tu contraseña actual"
                    nueva.length < 6 -> "La nueva contraseña debe tener al menos 6 caracteres"
                    nueva != confirmar -> "Las contraseñas no coinciden"
                    nueva == actual -> "La nueva contraseña debe ser distinta de la actual"
                    else -> null
                }
                if (error == null) onGuardado()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun SeccionAjustes(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column { contenido() }
        }
    }
}

@Composable
private fun FilaAjuste(
    icono: ImageVector,
    titulo: String,
    onClick: () -> Unit,
    detalle: String? = null,
    destacado: String? = null
) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = detalle?.let { { Text(it) } },
        leadingContent = { Icon(icono, contentDescription = null) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (destacado != null) ChipEtiqueta(destacado, ColorInfo)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun FilaSwitch(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = { Text(detalle) },
        leadingContent = { Icon(icono, contentDescription = null) },
        trailingContent = { Switch(checked = activo, onCheckedChange = onCambio) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
