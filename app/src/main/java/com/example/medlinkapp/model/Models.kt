package com.example.medlinkapp.model
import java.time.LocalDate
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
    val id: String,
    val patientId: String,
    val medication: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val dateIssued: LocalDate
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