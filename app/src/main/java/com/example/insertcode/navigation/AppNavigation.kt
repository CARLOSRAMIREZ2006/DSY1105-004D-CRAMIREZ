package com.example.insertcode.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.insertcode.ui.screens.AlertaDetalleScreen
import com.example.insertcode.ui.screens.AlertasScreen
import com.example.insertcode.ui.screens.CamaraDetalleScreen
import com.example.insertcode.ui.screens.CamarasScreen
import com.example.insertcode.ui.screens.EventosScreen
import com.example.insertcode.ui.screens.IncidenciaScreen
import com.example.insertcode.ui.screens.InicioScreen
import com.example.insertcode.ui.screens.ListadoOfflineScreen
import com.example.insertcode.ui.screens.LoginScreen
import com.example.insertcode.ui.screens.PerfilGuardianScreen
import com.example.insertcode.ui.screens.RegistroIntegralScreen
import com.example.insertcode.ui.screens.ResumenScreen
import com.example.insertcode.ui.screens.DashboardScreen
import com.example.insertcode.ui.screens.PedidosScreen
import com.example.insertcode.ui.screens.CatalogoScreen
import com.example.insertcode.ui.screens.RutaGpsScreen
import com.example.insertcode.ui.screens.PerfilVendedorScreen
import com.example.insertcode.ui.screens.RoomSyncScreen
import com.example.insertcode.viewmodels.GuardianViewModel
import com.example.insertcode.viewmodels.GuardianViewModelFactory
import com.example.insertcode.viewmodels.UsuarioViewModel
import com.example.insertcode.viewmodels.UsuarioViewModelFactory

@Composable
fun AppNavigation(
    usuarioFactory: UsuarioViewModelFactory,
    guardianFactory: GuardianViewModelFactory
) {
    val navController = rememberNavController()
    // ViewModels ligados a la Activity: se comparten entre todas las pantallas
    val usuarioViewModel: UsuarioViewModel = viewModel(factory = usuarioFactory)
    val guardianViewModel: GuardianViewModel = viewModel(factory = guardianFactory)

    // Espera a saber si hay una sesión recordada para elegir la pantalla inicial
    val sesionRecordada by guardianViewModel.sesionInicial.collectAsState()
    val hayRecordada = sesionRecordada
    if (hayRecordada == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (hayRecordada) Rutas.INICIO else Rutas.LOGIN
    ) {
        // ---------- Acceso ----------
        composable(route = Rutas.LOGIN) {
            LoginScreen(navController = navController, viewModel = guardianViewModel)
        }

        // ---------- Pantallas originales (Guía 14) ----------
        composable(route = Rutas.REGISTRO) {
            RegistroIntegralScreen(navController = navController, viewModel = usuarioViewModel)
        }
        composable(route = Rutas.RESUMEN) {
            ResumenScreen(navController = navController, viewModel = usuarioViewModel)
        }
        composable(route = Rutas.LISTADO) {
            ListadoOfflineScreen(navController = navController, viewModel = usuarioViewModel)
        }

        // ---------- Pestañas principales ----------
        composable(route = Rutas.INICIO) {
            DashboardScreen(navController = navController)
        }
        composable(route = Rutas.CAMARAS) {
            CamarasScreen(navController = navController, viewModel = guardianViewModel)
        }
        composable(route = Rutas.EVENTOS) {
            EventosScreen(navController = navController, viewModel = guardianViewModel)
        }
        composable(route = Rutas.ALERTAS) {
            AlertasScreen(navController = navController, viewModel = guardianViewModel)
        }
        composable(route = Rutas.PERFIL) {
            PerfilVendedorScreen(navController = navController, viewModel = guardianViewModel)
        }

        // ---------- Ventas ----------
        composable(route = Rutas.PEDIDOS) {
            PedidosScreen(navController = navController)
        }
        composable(route = Rutas.CATALOGO) {
            CatalogoScreen(navController = navController)
        }
        composable(route = Rutas.RUTA_GPS) {
            RutaGpsScreen(navController = navController)
        }
        composable(route = Rutas.ROOM_SYNC) {
            RoomSyncScreen(navController = navController)
        }

        // ---------- Detalles ----------
        composable(
            route = Rutas.CAMARA_DETALLE,
            arguments = listOf(navArgument("camaraId") { type = NavType.IntType })
        ) { entry ->
            CamaraDetalleScreen(
                navController = navController,
                viewModel = guardianViewModel,
                camaraId = entry.arguments?.getInt("camaraId") ?: 0
            )
        }
        composable(
            route = Rutas.ALERTA_DETALLE,
            arguments = listOf(navArgument("eventoId") { type = NavType.LongType })
        ) { entry ->
            AlertaDetalleScreen(
                navController = navController,
                viewModel = guardianViewModel,
                eventoId = entry.arguments?.getLong("eventoId") ?: 0L
            )
        }
        composable(
            route = Rutas.INCIDENCIA,
            arguments = listOf(navArgument("eventoId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            val id = entry.arguments?.getLong("eventoId") ?: -1L
            IncidenciaScreen(
                navController = navController,
                viewModel = guardianViewModel,
                eventoId = if (id >= 0) id else null
            )
        }
    }
}
