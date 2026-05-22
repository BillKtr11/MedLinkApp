package com.example.medlinkapp.data

import com.example.medlinkapp.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface DBManager {

    // Patient Management
    suspend fun getPatientInformation(patientId: String): Result<Patient>
    suspend fun searchPatientHistory(patientId: String): Result<List<String>>

    // Prescription & Drug Management
    suspend fun validatePrescription(prescription: Prescription): Boolean
    suspend fun checkStock(drugName: String): Int
    suspend fun addDrug(prescription: Prescription): Result<Unit>

    // Appointments
    suspend fun saveAppointment(appointment: Appointment): Result<Unit>

    // Emergency & Measurements (Using Flow to observe real-time data)
    fun requestDeviceData(deviceId: String): Flow<DeviceData>
    suspend fun saveMeasurement(data: DeviceData): Result<Unit>
    suspend fun triggerEmergencySOS(patientId: String, data: String): Result<String>

    // Health Report Generation (UC10)
    suspend fun getPatientMeasurements(patientId: String, start: LocalDateTime, end: LocalDateTime): Result<List<DeviceData>>
    suspend fun getPatientSideEffects(patientId: String, start: LocalDateTime, end: LocalDateTime): Result<List<SideEffect>>
    suspend fun getPatientPrescriptions(patientId: String): Result<List<Prescription>>
}
