package com.example.medlinkapp.data

import android.content.Context
import android.content.SharedPreferences
import com.example.medlinkapp.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

// Singleton DBManager for simple persistence
object DBManager {

    private const val PREFS_NAME = "medlink_prefs"
    private const val KEY_MEDICATIONS = "medications"
    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    // --- Persistence for Medications ---
    private val _medications = MutableStateFlow<List<MedicationData>>(emptyList())
    val medications: StateFlow<List<MedicationData>> = _medications.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadMedications()
        }
    }

    private fun loadMedications() {
        val json = prefs?.getString(KEY_MEDICATIONS, null)
        if (json != null) {
            val type = object : TypeToken<List<MedicationData>>() {}.type
            _medications.value = gson.fromJson(json, type)
        } else {
            // Initial default data
            _medications.value = listOf(
                MedicationData("1", "Metformin", "500mg", 14),
                MedicationData("2", "Lisinopril", "10mg", 5, 7),
                MedicationData("3", "Simvastatin", "20mg", 30)
            )
            saveMedications()
        }
    }

    private fun saveMedications() {
        val json = gson.toJson(_medications.value)
        prefs?.edit()?.putString(KEY_MEDICATIONS, json)?.apply()
    }

    fun updateStock(medId: String, newStock: Int) {
        _medications.update { list ->
            list.map { if (it.id == medId) it.copy(stockCount = newStock) else it }
        }
        saveMedications()
    }

    // --- Persistence for Measurements ---
    private val _measurements = MutableStateFlow<List<DeviceData>>(emptyList())
    val measurements: StateFlow<List<DeviceData>> = _measurements.asStateFlow()

    suspend fun saveMeasurement(data: DeviceData): Result<Unit> {
        delay(500) // Simulating network
        _measurements.update { it + data }
        println("Αποθηκεύτηκε η μέτρηση: ${data.measurementType} = ${data.measurementValue}")
        return Result.success(Unit)
    }

    // --- Normal Limits Logic (UC2 Step 6) ---
    fun getNormalLimits(type: String): IntRange {
        return when (type) {
            "Σάκχαρο" -> 70..140
            "Οξυγόνο" -> 95..100
            "Βάρος" -> 40..150
            "Πίεση" -> 90..140
            else -> 0..1000
        }
    }

    // --- Existing placeholder methods ---
    suspend fun getPatientInformation(patientId: String) = Unit
    suspend fun searchPatientHistory(patientId: String): Result<List<String>> = Result.success(listOf("Ιστορικό 1", "Ιστορικό 2"))
    suspend fun validatePrescription(prescription: Prescription): Boolean = true
    suspend fun checkStock(drugName: String): Int = 10
    suspend fun addDrug(prescription: Prescription): Result<Unit> = Result.success(Unit)
    suspend fun saveAppointment(appointment: Appointment): Result<Unit> = Result.success(Unit)
    fun requestDeviceData(deviceId: String): Flow<DeviceData> = flow { delay(1000) }
    suspend fun triggerEmergencySOS(patientId: String, data: String): Result<String> = Result.success("SOS Στάλθηκε")
}

// Data class for medication persistence
data class MedicationData(
    val id: String,
    val name: String,
    val dosage: String,
    val stockCount: Int,
    val lowStockThreshold: Int = 10
)
