package com.example.insertcode.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.R
import com.example.insertcode.auth.GoogleSignInHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.insertcode.auth.ResultadoGoogle
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BotonGoogle
import com.example.insertcode.viewmodels.GuardianViewModel
import kotlinx.coroutines.launch

// Pantalla de acceso: correo + contraseña, "Recordarme", recuperar clave, Google y crear cuenta
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: GuardianViewModel
) {
    val errores by viewModel.erroresLogin.collectAsState()
    val cargando by viewModel.cargandoLogin.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.default_web_client_id)
    val snackbar = remember { SnackbarHostState() }

    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var verClave by rememberSaveable { mutableStateOf(false) }
    var recordar by rememberSaveable { mutableStateOf(true) }
    var dialogoOlvide by rememberSaveable { mutableStateOf(false) }
    var correoRecuperacion by rememberSaveable { mutableStateOf("") }
    var googleCargando by remember { mutableStateOf(false) }
    var mensajeGoogle by rememberSaveable { mutableStateOf<String?>(null) }

    val irAInicio: () -> Unit = {
        navController.navigate(Rutas.INICIO) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
        }
    }

    val lanzadorGoogle = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { respuesta ->
        scope.launch {
                        val resultado = GoogleSignInHelper.procesarResultado(respuesta.data)
                        googleCargando = false
                        when (resultado) {
                            is ResultadoGoogle.Exito ->
                                viewModel.iniciarSesionGoogle(
                                    resultado.cuenta.nombre,
                                    resultado.cuenta.correo,
                                    irAInicio
                                )
                            is ResultadoGoogle.Cancelado -> Unit
                            is ResultadoGoogle.Error -> mensajeGoogle = resultado.mensaje
                        }
                    }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_insertcode),
                contentDescription = "Logo InsertCode",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentScale = ContentScale.Fit
            )

            Text(text = "Bienvenido de vuelta", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Ingresa a tu turno de monitoreo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    viewModel.limpiarErroresLogin()
                },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = errores.correo != null,
                supportingText = { errores.correo?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clave,
                onValueChange = {
                    clave = it
                    viewModel.limpiarErroresLogin()
                },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { verClave = !verClave }) {
                        Icon(
                            imageVector = if (verClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (verClave) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                isError = errores.clave != null || errores.general != null,
                supportingText = { (errores.clave ?: errores.general)?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = recordar, onCheckedChange = { recordar = it })
                    Text("Recordarme")
                }
                TextButton(onClick = {
                    correoRecuperacion = correo
                    dialogoOlvide = true
                }) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }

            Button(
                onClick = { viewModel.iniciarSesion(correo, clave, recordar, irAInicio) },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ingresar al sistema")
                }
            }

            Text(
                text = "Tus permisos se cargarán automáticamente según tu perfil",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "O continuar con",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mismo botón y mismo helper de Google que usa el registro
            BotonGoogle(
                texto = "Continuar con Google",
                enabled = !googleCargando,
                onClick = {
                    googleCargando = true
                    mensajeGoogle = null
                    GoogleSignInHelper.lanzarSelectorDeCuentas(context, webClientId) { lanzadorGoogle.launch(it) }
                }
            )
            mensajeGoogle?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta?")
                TextButton(onClick = { navController.navigate(Rutas.REGISTRO) }) {
                    Text("Crear cuenta")
                }
            }

            Text(
                text = "Protegido con encriptación de extremo a extremo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (dialogoOlvide) {
        AlertDialog(
            onDismissRequest = { dialogoOlvide = false },
            title = { Text("Recuperar contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Te enviaremos las instrucciones al correo registrado.")
                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = correoRecuperacion.contains("@"),
                    onClick = {
                        dialogoOlvide = false
                        scope.launch {
                            snackbar.showSnackbar("Si el correo existe, recibirás las instrucciones")
                        }
                    }
                ) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoOlvide = false }) { Text("Cancelar") }
            }
        )
    }
}
