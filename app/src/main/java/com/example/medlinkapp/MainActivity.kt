package com.example.medlinkapp // IMPORTANT: Change this to your actual package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medlinkapp.model.UserRole // Adjust import based on your structure
import com.example.medlinkapp.ui.login.LoginScreen // Adjust import based on your structure
import com.example.medlinkapp.ui.medication.MedicationManagerScreen
import com.example.medlinkapp.ui.patient.PatientDashboardScreen
import com.example.medlinkapp.ui.report.ReportScreen
import com.example.medlinkapp.data.MockDBManager
import com.example.medlinkapp.ui.doctor.DoctorDashboardScreen
import com.example.medlinkapp.ui.doctor.DoctorSearchScreen
import com.example.medlinkapp.ui.doctor.DoctorViewModel
import com.example.medlinkapp.ui.doctor.PatientHistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Apply your app's theme here (Android Studio usually generates one like HealthAppTheme)
            MaterialTheme {
                // A surface container using the 'background' color from the theme
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
    val dbManager = remember { MockDBManager() }
    // This controller manages which screen is currently visible
    val navController = rememberNavController()

    // The NavHost defines all the possible screens in your app
    NavHost(navController = navController, startDestination = "login_screen") {

        // 1. Login Screen
        composable("login_screen") {
            LoginScreen(
                onLoginSuccess = { role ->
                    // Navigate to the correct dashboard based on the user's role
                    when (role) {
                        UserRole.DOCTOR -> navController.navigate("doctor_dashboard") {
                            popUpTo("login_screen") { inclusive = true } // Removes login from backstack
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
            DoctorDashboardScreen(
                dbManager = dbManager,
                onNavigateToSearch = { navController.navigate("doctor_search") },
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo("doctor_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("doctor_search") {
            val doctorViewModel = remember { DoctorViewModel() }
            DoctorSearchScreen(
                viewModel = doctorViewModel,
                onPatientSelected = { navController.navigate("patient_history") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("patient_history") {
            // we need a shared viewmodel or to fetch it differently, for now simple:
            val doctorViewModel = remember { DoctorViewModel() }
            PatientHistoryScreen(
                viewModel = doctorViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. Patient Dashboard
        composable("patient_dashboard") {
            PatientDashboardScreen(
                onNavigateToMedications = { navController.navigate("medications_screen") },
                onNavigateToAppointments = { navController.navigate("appointments_screen") },
                onNavigateToResults = { navController.navigate("report_screen") },
                onNavigateToMessages = { /* Navigate to messages */ },
                onTriggerSOS = {
                    // In a real app, this would trigger an Alert Dialog or call the ViewModel
                    println("SOS Triggered!")
                },
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo("patient_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("medications_screen") {
            MedicationManagerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("report_screen") {
            ReportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 4. Caregiver Dashboard (Placeholder)
        composable("caregiver_dashboard") {
            Text(text = "Caregiver Dashboard") // Replace with actual CaregiverScreen() later
        }
    }
}