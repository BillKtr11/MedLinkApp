package com.example.medlinkapp.data

import android.content.Context
import android.content.SharedPreferences
import com.example.medlinkapp.model.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DBManager {

    private const val PREFS_NAME = "medlink_prefs"
    private const val KEY_MEDICATIONS = "medications"
    private const val KEY_MEASUREMENTS = "measurements"

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
            private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            override fun serialize(src: LocalDateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
                JsonPrimitive(formatter.format(src))
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime =
                LocalDateTime.parse(json.asString, formatter)
        })
        .create()

    private var prefs: SharedPreferences? = null

    private val _medications = MutableStateFlow<List<MedicationData>>(emptyList())
    val medications: StateFlow<List<MedicationData>> = _medications.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadMedications()
            loadMeasurements()
        }
    }

    private fun loadMedications() {
        val json = prefs?.getString(KEY_MEDICATIONS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<MedicationData>>() {}.type
                val list: List<MedicationData> = gson.fromJson(json, type)
                _medications.value = list
            } catch (e: Exception) {
                setDefaultMedications()
            }
        } else {
            setDefaultMedications()
        }
    }

    private fun setDefaultMedications() {
        _medications.value = listOf(
            MedicationData("1", "Metformin", "500mg", 14),
            MedicationData("2", "Lisinopril", "10mg", 5, 7),
            MedicationData("3", "Simvastatin", "20mg", 30)
        )
        saveMedications()
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

    fun addMedication(name: String, dosage: String, stock: Int) {
        val newMed = MedicationData(
            id = (System.currentTimeMillis()).toString(),
            name = name,
            dosage = dosage,
            stockCount = stock
        )
        _medications.update { it + newMed }
        saveMedications()
    }

    private val _measurements = MutableStateFlow<List<DeviceData>>(emptyList())
    val measurements: StateFlow<List<DeviceData>> = _measurements.asStateFlow()

    private fun loadMeasurements() {
        val json = prefs?.getString(KEY_MEASUREMENTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<DeviceData>>() {}.type
                val list: List<DeviceData> = gson.fromJson(json, type)
                _measurements.value = list
            } catch (e: Exception) {
                _measurements.value = emptyList()
            }
        }
    }

    private fun saveMeasurements() {
        val json = gson.toJson(_measurements.value)
        prefs?.edit()?.putString(KEY_MEASUREMENTS, json)?.apply()
    }

    suspend fun saveMeasurement(data: DeviceData): Result<Unit> {
        delay(500)
        _measurements.update { it + data }
        saveMeasurements()
        println("Αποθηκεύτηκε η μέτρηση: ${data.measurementType} = ${data.measurementValue}")
        return Result.success(Unit)
    }

    fun getNormalLimits(type: String): IntRange {
        return when (type) {
            "Σάκχαρο" -> 70..140
            "Οξυγόνο" -> 95..100
            "Βάρος" -> 40..150
            "Πίεση" -> 90..140
            else -> 0..1000
        }
    }


    suspend fun getPatientInformation(patientId: String) = Unit
    suspend fun searchPatientHistory(patientId: String): Result<List<String>> = Result.success(listOf("Ιστορικό 1", "Ιστορικό 2"))
    suspend fun validatePrescription(prescription: Prescription): Boolean = true
    suspend fun checkStock(drugName: String): Int = 10
    suspend fun addDrug(prescription: Prescription): Result<Unit> = Result.success(Unit)
    suspend fun saveAppointment(appointment: Appointment): Result<Unit> = Result.success(Unit)
    fun requestDeviceData(deviceId: String): Flow<DeviceData> = flow { delay(1000) }
    suspend fun triggerEmergencySOS(patientId: String, data: String): Result<String> = Result.success("SOS Στάλθηκε")
}

data class MedicationData(
    val id: String,
    val name: String,
    val dosage: String,
    val stockCount: Int,
    val lowStockThreshold: Int = 10
)
