package com.example.medlinkapp.model
import java.time.LocalDateTime
enum class UserRole {
    PATIENT, DOCTOR, CAREGIVER
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: UserRole, val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}


// Represents the data needed for PatientEntryForm and PatientScreen
data class Patient(
    val patientId: String,
    val name: String,
    val dateOfBirth: String,
    val medicalHistory: List<String> = emptyList()
)

// Represents data from DrugRegistrationManager and ManagePrescription
data class Prescription(
    val drugName: String,
    val drugDosage: Int, // e.g., in mg
    val drugFreq: Int,   // e.g., times per day
    val drugDuration: Int, // e.g., in days
    val drugStock: Int
)

// Represents data from AppointmentForm
data class Appointment(
    val appointmentId: String,
    val patientId: String,
    val doctorName: String,
    val date: LocalDateTime,
    val reason: String
)

// Represents data for DeviceManager and PatientComplianceStatusScreen
data class DeviceData(
    val deviceId: String,
    val measurementValue: Int,
    val measurementType: String,
    val timestamp: LocalDateTime
)