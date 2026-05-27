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


data class Patient(
    val patientId: String,
    val name: String,
    val dateOfBirth: String,
    val medicalHistory: List<String> = emptyList()
)

data class Prescription(
    val drugName: String,
    val drugDosage: Int,
    val drugFreq: Int,
    val drugDuration: Int,
    val drugStock: Int
)

data class Appointment(
    val appointmentId: String,
    val patientId: String,
    val doctorName: String,
    val date: LocalDateTime,
    val reason: String
)

data class DeviceData(
    val deviceId: String,
    val measurementValue: Int,
    val measurementType: String,
    val timestamp: LocalDateTime,
)

data class SideEffectReport(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val symptom: String,
    val duration: String,
    val intensity: Int,
    val timestamp: LocalDateTime,
)