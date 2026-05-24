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
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.UserRole // Adjust import based on your structure
import com.example.medlinkapp.ui.Screen
import com.example.medlinkapp.ui.login.LoginScreen // Adjust import based on your structure
import com.example.medlinkapp.ui.measurement.MeasurementViewModel
import com.example.medlinkapp.ui.measurement.NewMeasurementScreen
import com.example.medlinkapp.ui.measurement.MeasurementHistoryScreen
import com.example.medlinkapp.ui.medication.MedicationManagerScreen
import com.example.medlinkapp.ui.patient.PatientDashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DBManager.init(applicationContext)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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

    NavHost(navController = navController, startDestination = "login_screen") {

        // 1. Login Screen
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
// 2. Doctor Dashboard
        composable("doctor_dashboard") {
            Text(text = "Doctor Dashboard")
        }

// 3. Patient Dashboard
        composable("patient_dashboard") {
            PatientDashboardScreen(
                onNavigateToMedications = { navController.navigate("medications_screen") },
                onNavigateToAppointments = { navController.navigate("appointments_screen") },
                onNavigateToResults = { /* Navigate to results */ },
                onNavigateToMessages = { /* Navigate to messages */ },
                onNavigateToNewMeasurement = { navController.navigate(Screen.NewMeasurement.route) },
                onTriggerSOS = {
                    println("SOS Triggered!")
                }
            )
        }
        composable("medications_screen") {
            MedicationManagerScreen(
                onBackClick = { navController.popBackStack() }
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

        // 4. Caregiver Dashboard (Placeholder)
        composable("caregiver_dashboard") {
            Text(text = "Caregiver Dashboard")
        }
    }
}