package com.example.insertcode.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.R
import com.example.insertcode.auth.GoogleSignInHelper
import com.example.insertcode.auth.ResultadoGoogle
import com.example.insertcode.ui.components.BotonGoogle
import com.example.insertcode.ui.components.PremiumCard
import com.example.insertcode.viewmodels.UsuarioViewModel
import kotlinx.coroutines.launch

@Composable
fun RegistroIntegralScreen(
    navController: NavController,
    viewModel: UsuarioViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.default_web_client_id)
    var googleCargando by remember { mutableStateOf(false) }
    var mensajeGoogle by rememberSaveable { mutableStateOf<String?>(null) }

    // Estado local del botón premium (sobrevive a la rotación)
    var premiumActivo by rememberSaveable { mutableStateOf(false) }
    // Se sincroniza con el valor real cargado desde DataStore
    LaunchedEffect(uiState.modoEspecial) { premiumActivo = uiState.modoEspecial }

    val textoDerivado by remember {
        derivedStateOf { if (premiumActivo) "Modo Premium: ACTIVADO" else "Modo Premium: DESACTIVADO" }
    }

    // Animación de color: gris -> verde #2E7D32
    val colorBoton by animateColorAsState(
        targetValue = if (premiumActivo) Color(0xFF2E7D32) else Color.Gray,
        animationSpec = tween(durationMillis = 600),
        label = "colorBotonPremium"
    )

    Scaffold { innerPadding ->
        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo de InsertCode
                Image(
                    painter = painterResource(id = R.drawable.logo_insertcode),
                    contentDescription = "Logo InsertCode",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Registro",
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = uiState.correo,
                    onValueChange = viewModel::onCorreoChange,
                    label = { Text("Correo electrónico") },
                    isError = errores.correo != null,
                    supportingText = { errores.correo?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.clave,
                    onValueChange = viewModel::onClaveChange,
                    label = { Text("Clave") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errores.clave != null,
                    supportingText = { errores.clave?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.confirmarClave,
                    onValueChange = viewModel::onConfirmarClaveChange,
                    label = { Text("Confirmar clave") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errores.confirmarClave != null,
                    supportingText = { errores.confirmarClave?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Términos y condiciones
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = uiState.aceptaTerminos,
                            onCheckedChange = viewModel::onTerminosChange
                        )
                        Text("Acepto los términos y condiciones")
                    }
                    errores.aceptaTerminos?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                // Botón modo premium: color animado + texto derivado
                Button(
                    onClick = {
                        premiumActivo = !premiumActivo
                        viewModel.toggleModoEspecial()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorBoton),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(textoDerivado)
                }

                AnimatedVisibility(visible = mensajeExito) {
                    Text(
                        text = if (premiumActivo) "✔ Modo Premium guardado en DataStore"
                        else "✔ Modo Básico guardado en DataStore",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                PremiumCard(modoEspecial = uiState.modoEspecial)

                // Validar -> guardar en ROOM -> navegar a resumen
                Button(
                    onClick = {
                        if (viewModel.validarFormulario()) {
                            viewModel.guardarEnRoom {
                                navController.navigate("resumen")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar")
                }

                // Iniciar sesión con Google: abre el selector de cuentas y guarda al usuario
                BotonGoogle(
                    texto = "Iniciar sesión con Google",
                    enabled = !googleCargando,
                    onClick = {
                        googleCargando = true
                        mensajeGoogle = null
                        scope.launch {
                            val resultado = GoogleSignInHelper.elegirCuenta(context, webClientId)
                            googleCargando = false
                            when (resultado) {
                                is ResultadoGoogle.Exito -> {
                                    viewModel.onNombreChange(resultado.cuenta.nombre)
                                    viewModel.onCorreoChange(resultado.cuenta.correo)
                                    viewModel.guardarEnRoom { navController.navigate("resumen") }
                                }
                                is ResultadoGoogle.Cancelado -> Unit
                                is ResultadoGoogle.Error -> mensajeGoogle = resultado.mensaje
                            }
                        }
                    }
                )
                mensajeGoogle?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(onClick = { navController.navigate("listado") }) {
                    Text("Ver listado offline")
                }
            }
        }
    }
}
