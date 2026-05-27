package com.example.medlinkapp.model
import java.time.LocalDate
import java.time.LocalDateTime

enum class UserRole {
    PATIENT, DOCTOR, CAREGIVER
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: UserRole, val token: String, val userAmka: String = "") : LoginState()
    data class Error(val message: String) : LoginState()
}

data class Patient(
    val patientId: String,
    val name: String,
    val dateOfBirth: String,
    val medicalHistory: List<String> = emptyList()
)

data class Prescription(
    val id: String = "",
    val patientAmka: String = "",
    val drugName: String,
    val drugDosage: Int,
    val drugFreq: Int,
    val drugDuration: Int,
    val drugStock: Int,
    val dateIssued: LocalDate = LocalDate.now()
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
    val patientAmka: String
)

data class SideEffectReport(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val symptom: String,
    val duration: String,
    val intensity: Int,
    val timestamp: LocalDateTime
)

data class SideEffect(
    val patientId: String,
    val description: String,
    val severity: String,
    val timestamp: LocalDateTime
)

data class HealthReport(
    val patient: Patient,
    val measurements: List<DeviceData>,
    val sideEffects: List<SideEffect>,
    val medications: List<Prescription>,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

data class EmergencyAlert(
    val id:String,
    val patientId:String,
    val patientName:String,
    val measurementType:String,
    val value:Int,
    val timestamp:LocalDateTime,
    var status:String = "PENDING",
    var doctorInstructions:String = "",
    val patientAmka: String
)

data class MedicationData(
    val id: String,
    val name: String,
    val dosage: String,
    val stockCount: Int,
    val lowStockThreshold: Int = 10,
    val patientAmka: String,
    val intakeTimes: List<String> = emptyList(), // e.g. ["08:00", "20:00"]
    val frequency: Int = 1 // times per day
) {
    fun getNextIntakeTime(): String? {
        if (intakeTimes == null || intakeTimes.isEmpty()) return null
        // Simplistic logic for demonstration: return first intake time
        return intakeTimes.first()
    }
}

data class IntakeRecord(
    val medId: String,
    val medName: String,
    val timestamp: LocalDateTime,
    val patientAmka: String,
    val status: String // "Confirmed", "Skipped"
)

data class UserData(
    val name: String,
    val surname: String,
    val amka: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.PATIENT,
    val assignedDoctorAmka: String? = null,
    val assignedCaregiverAmka: String? = null
)

data class Message(
    val id: String,
    val patientAmka: String,
    val title: String,
    val content: String,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false
)
