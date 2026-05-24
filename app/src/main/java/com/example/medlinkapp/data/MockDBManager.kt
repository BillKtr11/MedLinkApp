package com.example.medlinkapp.data

import com.example.medlinkapp.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow 
import java.time.LocalDateTime

class MockDBManager : DBManager {

    private val mockPatients = mapOf(
        "1" to Patient(
            patientId = "1",
            name = "Alex Ferguson",
            dateOfBirth = "2001-04-24",
            medicalHistory = listOf("Type 2 diabetes", "Hypertension")
        ),
        "2" to Patient(
            patientId = "2",
            name = "Jane Austen",
            dateOfBirth = "1997-08-21",
            medicalHistory = listOf("Asthma")
        )
    )

    private val mockMeasurements = mapOf(
        "1" to listOf(
            DeviceData("d1", 115, "Blood glucose", LocalDateTime.now().minusDays(5)),
            DeviceData("d2", 120, "Blood glucose", LocalDateTime.now().minusDays(3)),
            DeviceData("d3", 95, "Blood glucose", LocalDateTime.now().minusDays(1)),
            DeviceData("d4", 125, "Systolic Blood pressure", LocalDateTime.now().minusDays(4)),
            DeviceData("d5", 80, "Diastolic Blood Pressure", LocalDateTime.now().minusDays(4))
        ),
        "2" to listOf(
            DeviceData("d6", 98, "Oxygen Saturation", LocalDateTime.now().minusDays(2)),
            DeviceData("d7", 97, "Oxygen Saturation", LocalDateTime.now().minusDays(1))
        )
    )

    // Προσθήκη του mockSideEffects για άριστη σχέση με το Παραδοτέο 3 (UC10, Βήμα 7)
    private val mockSideEffects = mapOf(
        "1" to listOf(
            SideEffect("1", "Mild morning dizziness", "Low", LocalDateTime.now().minusDays(4)),
            SideEffect("1", "Headache after taking medication", "Medium", LocalDateTime.now().minusDays(2))
        ),
        "2" to emptyList()
    )

    private val mockPrescriptions = mapOf(
        "1" to listOf(
            Prescription("Metformin", 500, 2, 30, 45),
            Prescription("Lisinopril", 10, 1, 30, 15)
        ),
        "2" to listOf(
            Prescription("Salbutamol Inhaler", 100, 2, 60, 5)
        )
    )

    override suspend fun getPatientInformation(patientId: String): Result<Patient> {
        val patient = mockPatients[patientId]
        return if (patient != null) {
            Result.success(patient)
        } else {
            Result.failure(Exception("Patient not found with this id: $patientId"))
        }
    }

    override suspend fun searchPatientHistory(patientId: String): Result<List<String>> {
        return getPatientInformation(patientId).map { it.medicalHistory }
    }

    override suspend fun validatePrescription(prescription: Prescription): Boolean {
        return prescription.drugName.isNotEmpty() && prescription.drugDosage > 0
    }

    override suspend fun checkStock(drugName: String): Int {
        return 50
    }

    override suspend fun addDrug(prescription: Prescription): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun saveAppointment(appointment: Appointment): Result<Unit> {
        return Result.success(Unit)
    }

    // Το requestDeviceData στην διεπαφή DBManager ΔΕΝ είναι suspend
    override fun requestDeviceData(deviceId: String): Flow<DeviceData> {
        return flow {
            emit(DeviceData(deviceId, 98, "Heart Rate", LocalDateTime.now()))
        }
    }

    override suspend fun saveMeasurement(data: DeviceData): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun triggerEmergencySOS(patientId: String, data: String): Result<String> {
        return Result.success("SOS Sent Successfully")
    }

    override suspend fun getPatientMeasurements(
        patientId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): Result<List<DeviceData>> {
        val list = mockMeasurements[patientId]?.filter {
            !it.timestamp.isBefore(start) && !it.timestamp.isAfter(end)
        } ?: emptyList()
        return Result.success(list)
    }

    override suspend fun getPatientSideEffects(
        patientId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): Result<List<SideEffect>> {
        val list = mockSideEffects[patientId]?.filter {
            !it.timestamp.isBefore(start) && !it.timestamp.isAfter(end)
        } ?: emptyList()
        return Result.success(list)
    }

    override suspend fun getPatientPrescriptions(patientId: String): Result<List<Prescription>> {
        return Result.success(mockPrescriptions[patientId] ?: emptyList())
    }
}