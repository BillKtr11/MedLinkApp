package com.example.medlinkapp // IMPORTANT: Change this to your actual package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medlinkapp.model.UserRole // Adjust import based on your structure
import com.example.medlinkapp.ui.login.LoginScreen // Adjust import based on your structure

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Apply your app's theme here (Android Studio usually generates one like HealthAppTheme)
            MaterialTheme {
                // A surface container using the 'background' color from the theme
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

        // 2. Doctor Dashboard (Placeholder)
        composable("doctor_dashboard") {
            Text(text = "Doctor Dashboard") // Replace with actual DoctorScreen() later
        }

        // 3. Patient Dashboard (Placeholder)
        composable("patient_dashboard") {
            Text(text = "Patient Dashboard") // Replace with actual PatientScreen() later
        }

        // 4. Caregiver Dashboard (Placeholder)
        composable("caregiver_dashboard") {
            Text(text = "Caregiver Dashboard") // Replace with actual CaregiverScreen() later
        }
    }
}