package com.sssl.asisteciaqr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sssl.asisteciaqr.ui.screens.main.MainScreen
import com.sssl.asisteciaqr.ui.screens.alumno.AlumnoQRScreen
import com.sssl.asisteciaqr.ui.screens.profesor.ProfesorLoginScreen
import com.sssl.asisteciaqr.ui.screens.profesor.ProfesorHomeScreen
import com.sssl.asisteciaqr.ui.screens.profesor.CreateGrupoScreen
import com.sssl.asisteciaqr.ui.screens.profesor.GrupoDetailScreen
import com.sssl.asisteciaqr.ui.screens.profesor.AddAlumnosScreen
import com.sssl.asisteciaqr.ui.screens.profesor.ScanAsistenciaScreen
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AlumnoQR : Screen("alumno_qr")
    object ProfesorLogin : Screen("profesor_login")
    object ProfesorHome : Screen("profesor_home")
    object CreateGrupo : Screen("create_grupo")
    object GrupoDetail : Screen("grupo_detail/{grupoId}") {
        fun createRoute(grupoId: Long) = "grupo_detail/$grupoId"
    }
    object AddAlumnos : Screen("add_alumnos/{grupoId}") {
        fun createRoute(grupoId: Long) = "add_alumnos/$grupoId"
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
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onAlumnoClick = { navController.navigate(Screen.AlumnoQR.route) },
                onProfesorClick = { navController.navigate(Screen.ProfesorLogin.route) }
            )
        }

        composable(Screen.AlumnoQR.route) {
            AlumnoQRScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfesorLogin.route) {
            ProfesorLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ProfesorHome.route) {
                        popUpTo(Screen.Main.route)
                    }
                },
                onBack = { navController.popBackStack() }
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
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
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
                onAddAlumnos = { navController.navigate(Screen.AddAlumnos.createRoute(grupoId)) },
                onScanAsistencia = { navController.navigate(Screen.ScanAsistencia.createRoute(grupoId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddAlumnos.route,
            arguments = listOf(navArgument("grupoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getLong("grupoId") ?: return@composable
            AddAlumnosScreen(
                viewModel = viewModel,
                grupoId = grupoId,
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