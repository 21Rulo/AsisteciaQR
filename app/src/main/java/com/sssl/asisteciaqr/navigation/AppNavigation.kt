package com.sssl.asisteciaqr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sssl.asisteciaqr.ui.screens.profesor.ProfesorLoginScreen
import com.sssl.asisteciaqr.ui.screens.profesor.ProfesorHomeScreen
import com.sssl.asisteciaqr.ui.screens.profesor.CreateGrupoScreen
import com.sssl.asisteciaqr.ui.screens.profesor.GrupoDetailScreen
import com.sssl.asisteciaqr.ui.screens.profesor.ScanAsistenciaScreen
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel

sealed class Screen(val route: String) {
    object ProfesorLogin : Screen("profesor_login")
    object ProfesorHome : Screen("profesor_home")
    object CreateGrupo : Screen("create_grupo")
    object GrupoDetail : Screen("grupo_detail/{grupoId}") {
        fun createRoute(grupoId: Long) = "grupo_detail/$grupoId"
    }
    object ScanAsistencia : Screen("scan_asistencia/{grupoId}") {
        fun createRoute(grupoId: Long) = "scan_asistencia/$grupoId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AsistenciaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ProfesorLogin.route
    ) {
        composable(Screen.ProfesorLogin.route) {
            ProfesorLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ProfesorHome.route) {
                        popUpTo(Screen.ProfesorLogin.route) { inclusive = true }
                    }
                },
                onBack = { /* No hay back en login inicial */ }
            )
        }

        composable(Screen.ProfesorHome.route) {
            ProfesorHomeScreen(
                viewModel = viewModel,
                onCreateGrupo = { navController.navigate(Screen.CreateGrupo.route) },
                onGrupoClick = { grupoId ->
                    navController.navigate(Screen.GrupoDetail.createRoute(grupoId))
                },
                onLogout = {
                    navController.navigate(Screen.ProfesorLogin.route) {
                        popUpTo(Screen.ProfesorLogin.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateGrupo.route) {
            CreateGrupoScreen(
                viewModel = viewModel,
                onGrupoCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GrupoDetail.route,
            arguments = listOf(navArgument("grupoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getLong("grupoId") ?: return@composable
            GrupoDetailScreen(
                viewModel = viewModel,
                grupoId = grupoId,
                onScanAsistencia = { navController.navigate(Screen.ScanAsistencia.createRoute(grupoId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ScanAsistencia.route,
            arguments = listOf(navArgument("grupoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getLong("grupoId") ?: return@composable
            ScanAsistenciaScreen(
                viewModel = viewModel,
                grupoId = grupoId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}