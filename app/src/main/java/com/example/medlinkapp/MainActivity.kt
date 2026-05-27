package com.example.medlinkapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.medlinkapp.ui.login.SystemAdministratorManager
import com.example.medlinkapp.ui.login.LoginViewModel
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.ui.Screen
import com.example.medlinkapp.ui.measurement.MeasurementViewModel
import com.example.medlinkapp.ui.measurement.ManageMeasurementRecording
import com.example.medlinkapp.ui.measurement.ManageSensorData
import com.example.medlinkapp.ui.medication.ManageMedicationIntake
import com.example.medlinkapp.ui.medication.DrugRegistrationManager
import com.example.medlinkapp.ui.medication.IntakeScreen
import com.example.medlinkapp.ui.medication.MedicationViewModel
import com.example.medlinkapp.ui.patient.PatientScreen
import com.example.medlinkapp.ui.patient.SideEffectReportManager
import com.example.medlinkapp.ui.patient.SideEffectViewModel
import com.example.medlinkapp.ui.doctor.DoctorSearchScreen
import com.example.medlinkapp.ui.doctor.ManageSearchHistory
import com.example.medlinkapp.ui.doctor.DoctorViewModel
import com.example.medlinkapp.ui.report.HealthReportManager
import com.example.medlinkapp.ui.doctor.DoctorScreen
import com.example.medlinkapp.ui.patient.AppointmentList as PatientAppointmentList
import com.example.medlinkapp.ui.patient.MessageSystem
import com.example.medlinkapp.ui.prescription.PrescriptionHistoryScreen
import com.example.medlinkapp.ui.doctor.AppointmentAdditionScreen
import com.example.medlinkapp.ui.doctor.ManageDoctorClient
import com.example.medlinkapp.ui.doctor.AppointmentList as DoctorAppointmentList
import com.example.medlinkapp.ui.doctor.PrescriptionSystem
import com.example.medlinkapp.ui.caregiver.*

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
    // This controller manages which screen is currently visible
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
            SystemAdministratorManager(
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

            DoctorScreen(
                doctorName = doctorName,
                viewModel = doctorViewModel,
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
                onNavigateToReport = {
                    navController.navigate("report_screen")
                },
                onNavigateToMessages = {
                    navController.navigate("messages_screen")
                },
                onNavigateToPrescriptionHistory = {
                    navController.navigate("prescription_history_doctor")
                },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
            })
        }

        composable("prescription_history_doctor") {
            val prescriptions by DBManager.prescriptions.collectAsState()
            PrescriptionHistoryScreen(
                prescriptions = prescriptions,
                title = "Ιστορικό Εκδοθέντων Συνταγών",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("add_appointment_screen") {
            AppointmentAdditionScreen(
                viewModel = doctorViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.popBackStack("doctor_dashboard", inclusive = false)
                }
            )
        }

        composable("scheduled_appointments_screen") {
            DoctorAppointmentList(
                viewModel = doctorViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("assign_patient_screen") {
            ManageDoctorClient(
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
                },
            )
        }

        composable("patient_history_screen") {
            ManageSearchHistory(
                viewModel = doctorViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToPrescription = {
                    // Πλοήγηση στη νέα οθόνη της συνταγής
                    navController.navigate("prescription_screen")
                }
            )
        }

        // 2.3 ΝΕΑ ΟΘΟΝΗ: Καταχώριση Συνταγής
        composable("prescription_screen") {
            PrescriptionSystem(
                viewModel = doctorViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. Patient Dashboard
        composable("patient_dashboard") {
            val patientName = (loginState as? LoginState.Success)?.let { success ->
                DBManager.users.value.find { it.amka == success.userAmka }?.let { "${it.name} ${it.surname}" }
            } ?: "Patient"

            PatientScreen(
                patientName = patientName,
                onNavigateToMedications = { navController.navigate("medications_screen") },
                onNavigateToAppointments = { navController.navigate("appointments_screen") },
                onNavigateToResults = { },
                onNavigateToMessages = { navController.navigate("messages_screen") },
                onNavigateToPrescriptionHistory = { navController.navigate("prescription_history_patient") },
                onNavigateToNewMeasurement = { navController.navigate(Screen.NewMeasurement.route) },
                onNavigateToReportSymptom = { navController.navigate(Screen.ReportSymptom.route) },                
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
                },
            )
        }

        composable("prescription_history_patient") {
            val prescriptions by DBManager.prescriptions.collectAsState()
            val userAmka = DBManager.getCurrentUserAmka()
            val myPrescriptions = prescriptions.filter { it.patientAmka == userAmka }
            
            PrescriptionHistoryScreen(
                prescriptions = myPrescriptions,
                title = "Οι Συνταγές Μου",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("appointments_screen") {
            PatientAppointmentList(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("messages_screen") {
            MessageSystem(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("medications_screen") {
            ManageMedicationIntake(
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
            DrugRegistrationManager(
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

            ManageMeasurementRecording(
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
            ManageSensorData(
                viewModel = measurementViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ReportSymptom.route) {
            val sideEffectViewModel: SideEffectViewModel = viewModel()
            SideEffectReportManager(
                viewModel = sideEffectViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable("report_screen") {
            val assignedPatients by doctorViewModel.myPatients.collectAsState()
            HealthReportManager(
                onBackClick = { navController.popBackStack() },
                assignedPatients = assignedPatients
            )
        }

        // 4. Caregiver Dashboard (Placeholder)
        composable("caregiver_dashboard") {
            val caregiverName = (loginState as? LoginState.Success)?.let { success ->
                DBManager.users.value.find { it.amka == success.userAmka }?.let { "${it.name} ${it.surname}" }
            } ?: "Caregiver"

            CaregiverScreen(
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
            ManageCaregiverClient(
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
            PatientComplianceSystem(
                viewModel = caregiverViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
