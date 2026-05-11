package com.sssl.asisteciaqr.navigation

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sssl.asisteciaqr.ui.screens.profesor.*
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel
import com.sssl.asisteciaqr.utils.ReportePDFGenerator

sealed class Screen(val route: String) {
    object ProfesorLogin : Screen("profesor_login")
    object ProfesorHome : Screen("profesor_home")
    object CreateGrupo : Screen("create_grupo")
    object Configuracion : Screen("configuracion")  // NUEVA RUTA
    object GrupoDetail : Screen("grupo_detail/{grupoId}") {
        fun createRoute(grupoId: Long) = "grupo_detail/$grupoId"
    }
    object ScanAsistencia : Screen("scan_asistencia/{grupoId}") {
        fun createRoute(grupoId: Long) = "scan_asistencia/$grupoId"
    }
    object Historial : Screen("historial/{grupoId}") {
        fun createRoute(grupoId: Long) = "historial/$grupoId"
    }
    object DetalleAlumno : Screen("detalle_alumno/{grupoId}/{matricula}") {
        fun createRoute(grupoId: Long, matricula: String) = "detalle_alumno/$grupoId/$matricula"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AsistenciaViewModel
) {
    val context = LocalContext.current

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
                onConfiguracion = { navController.navigate(Screen.Configuracion.route) },  // NUEVO
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

        // NUEVA RUTA: Configuración
        composable(Screen.Configuracion.route) {
            ConfiguracionScreen(
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
                onVerHistorial = { navController.navigate(Screen.Historial.createRoute(grupoId)) },
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

        composable(
            route = Screen.Historial.route,
            arguments = listOf(navArgument("grupoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getLong("grupoId") ?: return@composable
            HistorialAsistenciaScreen(
                viewModel = viewModel,
                grupoId = grupoId,
                onAlumnoClick = { matricula ->
                    navController.navigate(Screen.DetalleAlumno.createRoute(grupoId, matricula))
                },
                onGenerarPDFGrupo = {
                    val grupos = viewModel.grupos.value
                    val grupo = grupos.find { it.id == grupoId }
                    val estadisticas = viewModel.estadisticasAlumnos.value
                    val periodoSeleccionado = viewModel.periodoSeleccionado.value
                    val estadisticasGrupo = viewModel.estadisticasGrupo.value

                    if (grupo != null && estadisticas.isNotEmpty() && estadisticasGrupo != null) {
                        val file = ReportePDFGenerator.generarReporteGrupo(
                            context = context,
                            nombreGrupo = grupo.nombreGrupo,
                            materia = grupo.materia,
                            estadisticas = estadisticas,
                            periodo = periodoSeleccionado.nombre,
                            totalClases = estadisticasGrupo.totalClases
                        )

                        if (file != null) {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Reporte"))
                        } else {
                            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DetalleAlumno.route,
            arguments = listOf(
                navArgument("grupoId") { type = NavType.LongType },
                navArgument("matricula") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getLong("grupoId") ?: return@composable
            val matricula = backStackEntry.arguments?.getString("matricula") ?: return@composable

            DetalleAlumnoScreen(
                viewModel = viewModel,
                grupoId = grupoId,
                matricula = matricula,
                onGenerarPDFIndividual = {
                    val grupos = viewModel.grupos.value
                    val grupo = grupos.find { it.id == grupoId }
                    val alumnos = viewModel.alumnosDelGrupo.value
                    val alumno = alumnos.find { it.matricula == matricula }
                    val estadisticas = viewModel.estadisticasAlumnos.value
                    val estadistica = estadisticas.find { it.alumno.matricula == matricula }
                    val asistencias = viewModel.asistenciasAlumnoDetalle.value
                    val periodoSeleccionado = viewModel.periodoSeleccionado.value

                    if (grupo != null && alumno != null && estadistica != null) {
                        val file = ReportePDFGenerator.generarReporteIndividual(
                            context = context,
                            nombreAlumno = alumno.nombre,
                            matricula = alumno.matricula,
                            nombreGrupo = grupo.nombreGrupo,
                            estadistica = estadistica,
                            asistencias = asistencias,
                            periodo = periodoSeleccionado.nombre
                        )

                        if (file != null) {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Reporte Individual"))
                        } else {
                            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}