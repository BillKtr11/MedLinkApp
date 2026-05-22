package com.example.medlinkapp.data

import com.example.medlinkapp.model.*
import java.time.LocalDateTime

class ReportRepository(private val dbManager: DBManager) {

    suspend fun getPatient(patientId: String): Result<Patient> {
        return dbManager.getPatientInformation(patientId)
    }

    suspend fun generateHealthReport(
        patientId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<HealthReport> {
        val patientResult = dbManager.getPatientInformation(patientId)
        if (patientResult.isFailure) return Result.failure(patientResult.exceptionOrNull()!!)
        
        val patient = patientResult.getOrNull()!!

        val measurements = dbManager.getPatientMeasurements(patientId, startDate, endDate).getOrDefault(emptyList())
        val sideEffects = dbManager.getPatientSideEffects(patientId, startDate, endDate).getOrDefault(emptyList())
        val medications = dbManager.getPatientPrescriptions(patientId).getOrDefault(emptyList())

        if (measurements.isEmpty() && sideEffects.isEmpty() && medications.isEmpty()) {
            return Result.failure(Exception("Not enough data for the selected period"))
        }

        val report = HealthReport(
            patient = patient,
            measurements = measurements,
            sideEffects = sideEffects,
            medications = medications,
            startDate = startDate,
            endDate = endDate
        )

        // Logic to "create PDF" would go here. For now we return the report object.
        return Result.success(report)
    }
}
