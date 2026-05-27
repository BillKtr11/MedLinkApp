package com.example.medlinkapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.medlinkapp.model.LoginState
import com.example.medlinkapp.model.UserRole
import com.example.medlinkapp.ui.login.LoginScreen
import com.example.medlinkapp.ui.login.RegisterScreen
import com.example.medlinkapp.ui.login.LoginViewModel
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.ui.Screen
import com.example.medlinkapp.ui.measurement.MeasurementViewModel
import com.example.medlinkapp.ui.measurement.NewMeasurementScreen
import com.example.medlinkapp.ui.measurement.MeasurementHistoryScreen
import com.example.medlinkapp.ui.medication.MedicationManagerScreen
import com.example.medlinkapp.ui.medication.AddMedicationScreen
import com.example.medlinkapp.ui.medication.IntakeScreen
import com.example.medlinkapp.ui.medication.MedicationViewModel
import com.example.medlinkapp.ui.patient.PatientDashboardScreen
import com.example.medlinkapp.ui.patient.PatientAppointmentsScreen
import com.example.medlinkapp.ui.patient.PatientMessagesScreen
import com.example.medlinkapp.ui.doctor.DoctorSearchScreen
import com.example.medlinkapp.ui.doctor.PatientHistoryScreen
import com.example.medlinkapp.ui.doctor.DoctorViewModel
import com.example.medlinkapp.ui.doctor.DoctorDashboardScreen
import com.example.medlinkapp.ui.doctor.AddAppointmentScreen
import com.example.medlinkapp.ui.doctor.AssignPatientScreen
import com.example.medlinkapp.ui.doctor.ScheduledAppointmentsScreen
import com.example.medlinkapp.ui.caregiver.*

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
    val loginViewModel: LoginViewModel = viewModel()
    val doctorViewModel: DoctorViewModel = viewModel()
    val caregiverViewModel: CaregiverViewModel = viewModel()
    
    val loginState by loginViewModel.loginState.collectAsState()

    // Auto-login logic
    LaunchedEffect(Unit) {
        if (DBManager.isSessionValid()) {
            val user = DBManager.getCurrentUser()
            if (user != null) {
                loginViewModel.autoLogin(user)
                
                when (user.role) {
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
        }
    }

    NavHost(navController = navController, startDestination = "login_screen") {

        composable("login_screen") {
            LoginScreen(
                viewModel = loginViewModel,
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
                },
                onNavigateToRegister = {
                    loginViewModel.resetState()
                    navController.navigate("register_screen")
                }
            )
        }

        composable("register_screen") {
            RegisterScreen(
                viewModel = loginViewModel,
                onRegisterSuccess = {
                    val role = (loginViewModel.loginState.value as? LoginState.Success)?.role
                    when (role) {
                        UserRole.DOCTOR -> navController.navigate("doctor_dashboard") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                        UserRole.CAREGIVER -> navController.navigate("caregiver_dashboard") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                        else -> navController.navigate("patient_dashboard") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                },
                onBackToLogin = {
                    loginViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }

        composable("doctor_dashboard") {
            val doctorName = (loginState as? LoginState.Success)?.let { success ->
                DBManager.users.value.find { it.amka == success.userAmka }?.let { "${it.name} ${it.surname}" }
            } ?: "Doctor"

            DoctorDashboardScreen(
                doctorName = doctorName,
                onNavigateToSearch = {
                    navController.navigate("doctor_search_screen")
                },
                onNavigateToAddAppointment = {
                    navController.navigate("add_appointment_screen")
                },
                onNavigateToRegisterPatient = {
                    navController.navigate("assign_patient_screen")
                },
                onNavigateToAppointments = {
                    navController.navigate("scheduled_appointments_screen")
                },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("add_appointment_screen") {
            AddAppointmentScreen(
                viewModel = doctorViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.popBackStack("doctor_dashboard", inclusive = false)
                }
            )
        }

        composable("scheduled_appointments_screen") {
            ScheduledAppointmentsScreen(
                viewModel = doctorViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("assign_patient_screen") {
            AssignPatientScreen(
                viewModel = doctorViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.popBackStack("doctor_dashboard", inclusive = false)
                }
            )
        }

        composable("doctor_search_screen") {
            DoctorSearchScreen(
                viewModel = doctorViewModel,
                onPatientSelected = {
                    navController.navigate("patient_history_screen")
                },
                onBackClick = {
                    navController.popBackStack()
                }
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
            val patientName = (loginState as? LoginState.Success)?.let { success ->
                DBManager.users.value.find { it.amka == success.userAmka }?.let { "${it.name} ${it.surname}" }
            } ?: "Patient"

            PatientDashboardScreen(
                patientName = patientName,
                onNavigateToMedications = { navController.navigate("medications_screen") },
                onNavigateToAppointments = { navController.navigate("appointments_screen") },
                onNavigateToResults = { /* Navigate to results */ },
                onNavigateToMessages = { /* Navigate to messages */ },
                onNavigateToNewMeasurement = { navController.navigate(Screen.NewMeasurement.route) },
                onNavigateToTakeMedication = { medId ->
                    navController.navigate("intake_screen/$medId")
                },
                onTriggerSOS = {
                    println("SOS Triggered!")
                },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("appointments_screen") {
            PatientAppointmentsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("messages_screen") {
            PatientMessagesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("medications_screen") {
            MedicationManagerScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToAddMedication = { navController.navigate(Screen.AddMedication.route) },
                onNavigateToIntake = { medId -> 
                    navController.navigate("intake_screen/$medId")
                }
            )
        }

        composable("intake_screen/{medId}") { backStackEntry ->
            val medId = backStackEntry.arguments?.getString("medId") ?: ""
            val medViewModel: MedicationViewModel = viewModel()
            IntakeScreen(
                medId = medId,
                viewModel = medViewModel,
                onNavigateBack = { navController.popBackStack() }
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
            val measurementViewModel: MeasurementViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MeasurementViewModel(DBManager) as T
                    }
                }
            )
            MeasurementHistoryScreen(
                viewModel = measurementViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("caregiver_dashboard") {
            val caregiverName = (loginState as? LoginState.Success)?.let { success ->
                DBManager.users.value.find { it.amka == success.userAmka }?.let { "${it.name} ${it.surname}" }
            } ?: "Caregiver"

            CaregiverDashboardScreen(
                caregiverName = caregiverName,
                viewModel = caregiverViewModel,
                onNavigateToAssignPatient = {
                    navController.navigate("assign_patient_caregiver")
                },
                onNavigateToPatientDetails = { patientAmka ->
                    val patient = DBManager.users.value.find { it.amka == patientAmka }
                    if (patient != null) {
                        caregiverViewModel.selectPatient(patient)
                        navController.navigate("patient_detail_caregiver")
                    }
                },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("assign_patient_caregiver") {
            AssignPatientCaregiverScreen(
                viewModel = caregiverViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.popBackStack("caregiver_dashboard", inclusive = false)
                }
            )
        }

        composable("patient_detail_caregiver") {
            PatientDetailCaregiverScreen(
                viewModel = caregiverViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStats = {
                    navController.navigate("caregiver_stats")
                }
            )
        }

        composable("caregiver_stats") {
            CaregiverStatsScreen(
                viewModel = caregiverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
