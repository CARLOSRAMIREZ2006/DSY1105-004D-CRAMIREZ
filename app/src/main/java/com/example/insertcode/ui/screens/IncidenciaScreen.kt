package com.example.insertcode.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.insertcode.ui.components.ImagenInteligente
import com.example.insertcode.ui.utils.crearUriTemporal
import com.example.insertcode.util.aUri
import com.example.insertcode.util.formatoFechaHora
import com.example.insertcode.viewmodels.GuardianViewModel

private val TIPOS_INCIDENCIA = listOf("Intrusión", "Falla técnica", "Acceso indebido", "Otro")

// Formulario para reportar una incidencia con foto (cámara o galería) -> se guarda en ROOM
@Composable
fun IncidenciaScreen(
    navController: NavController,
    viewModel: GuardianViewModel,
    eventoId: Long?
) {
    val context = LocalContext.current
    val incidencias by viewModel.incidencias.collectAsState()

    var tipo by rememberSaveable { mutableStateOf(TIPOS_INCIDENCIA[0]) }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var uriTemporal by rememberSaveable { mutableStateOf<Uri?>(null) }
    var errorDescripcion by rememberSaveable { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var exito by rememberSaveable { mutableStateOf(false) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) foto = uri
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok: Boolean ->
        if (ok && uriTemporal != null) foto = uriTemporal
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Column {
                    Text("Reportar incidencia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (eventoId != null) {
                        Text(
                            "Asociada a la alerta #$eventoId",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text("Tipo de incidencia", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TIPOS_INCIDENCIA.take(2).forEach { t ->
                    FilterChip(selected = tipo == t, onClick = { tipo = t }, label = { Text(t) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TIPOS_INCIDENCIA.drop(2).forEach { t ->
                    FilterChip(selected = tipo == t, onClick = { tipo = t }, label = { Text(t) })
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    errorDescripcion = null
                },
                label = { Text("Descripción") },
                isError = errorDescripcion != null,
                supportingText = { errorDescripcion?.let { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Foto
            if (foto != null) {
                AsyncImage(
                    model = foto,
                    contentDescription = "Foto de la incidencia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val uri = crearUriTemporal(context)
                        uriTemporal = uri
                        camara.launch(uri)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text("  Cámara")
                }
                OutlinedButton(
                    onClick = { galeria.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Text("  Galería")
                }
            }

            Button(
                onClick = {
                    if (descripcion.trim().length < 10) {
                        errorDescripcion = "Describe lo ocurrido (mínimo 10 caracteres)"
                    } else {
                        guardando = true
                        viewModel.guardarIncidencia(tipo, descripcion, foto, eventoId) {
                            guardando = false
                            exito = true
                        }
                    }
                },
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Text("  Enviar reporte")
                }
            }

            // Historial local de incidencias (ROOM, funciona offline)
            if (incidencias.isNotEmpty()) {
                Text("Incidencias reportadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                incidencias.take(5).forEach { inc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ImagenInteligente(imagenUri = inc.fotoPath.aUri(), tamano = 48.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inc.tipo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(inc.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                Text(
                                    inc.fecha.formatoFechaHora(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (exito) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            title = { Text("Incidencia registrada") },
            text = { Text("Quedó guardada en el historial del turno y disponible sin conexión.") },
            confirmButton = {
                TextButton(onClick = {
                    exito = false
                    navController.popBackStack()
                }) { Text("Aceptar") }
            }
        )
    }
}
