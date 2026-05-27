package com.example.medlinkapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medlinkapp.model.UserRole
import com.example.medlinkapp.ui.login.LoginScreen
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.ui.Screen
import com.example.medlinkapp.ui.measurement.MeasurementViewModel
import com.example.medlinkapp.ui.measurement.NewMeasurementScreen
import com.example.medlinkapp.ui.measurement.MeasurementHistoryScreen
import com.example.medlinkapp.ui.medication.MedicationManagerScreen
import com.example.medlinkapp.ui.medication.AddMedicationScreen
import com.example.medlinkapp.ui.medication.MedicationViewModel
import com.example.medlinkapp.ui.patient.PatientDashboardScreen
import com.example.medlinkapp.ui.patient.ReportSymptomScreen
import com.example.medlinkapp.ui.patient.SideEffectViewModel
 import com.example.medlinkapp.ui.doctor.DoctorSearchScreen
 import com.example.medlinkapp.ui.doctor.PatientHistoryScreen
 import com.example.medlinkapp.ui.doctor.DoctorViewModel
import com.example.medlinkapp.ui.doctor.DoctorDashboardScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DBManager.init(applicationContext)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val doctorViewModel: DoctorViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login_screen") {

        composable("login_screen") {
            LoginScreen(
                onLoginSuccess = { role ->
                    when (role) {
                        UserRole.DOCTOR -> navController.navigate("doctor_dashboard") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                        UserRole.PATIENT -> navController.navigate("patient_dashboard") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                        UserRole.CAREGIVER -> navController.navigate("caregiver_dashboard") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("doctor_dashboard") {
            DoctorDashboardScreen(
                onNavigateToSearch = {
                    navController.navigate("doctor_search_screen")
                },
            ) {
                navController.navigate("login_screen") {
                    popUpTo("doctor_dashboard") { inclusive = true }
                }
            }
        }

        composable("doctor_search_screen") {
            DoctorSearchScreen(
                viewModel = doctorViewModel,
                onPatientSelected = {
                    navController.navigate("patient_history_screen")
                },
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        composable("patient_history_screen") {
            PatientHistoryScreen(
                viewModel = doctorViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("patient_dashboard") {
            PatientDashboardScreen(
                onNavigateToMedications = { navController.navigate("medications_screen") },
                onNavigateToAppointments = { navController.navigate("appointments_screen") },
                onNavigateToResults = { },
                onNavigateToMessages = { },
                onNavigateToNewMeasurement = { navController.navigate(Screen.NewMeasurement.route) },
                onNavigateToReportSymptom = { navController.navigate(Screen.ReportSymptom.route) },
                onTriggerSOS = {
                    println("SOS Triggered!")
                },
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable("medications_screen") {
            MedicationManagerScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToAddMedication = { navController.navigate(Screen.AddMedication.route) }
            )
        }
        composable(route = Screen.AddMedication.route) {
            val medViewModel: MedicationViewModel = viewModel()
            AddMedicationScreen(
                viewModel = medViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.popBackStack("patient_dashboard", inclusive = false)
                }
            )
        }
        composable(route = Screen.NewMeasurement.route) {
            val measurementViewModel: MeasurementViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MeasurementViewModel(DBManager) as T
                    }
                }
            )

            NewMeasurementScreen(
                viewModel = measurementViewModel,
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateHome = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.History.route) {
            MeasurementHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ReportSymptom.route) {
            val sideEffectViewModel: SideEffectViewModel = viewModel()
            ReportSymptomScreen(
                viewModel = sideEffectViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable("caregiver_dashboard") {
            Text(text = "Caregiver Dashboard")
        }
    }
}