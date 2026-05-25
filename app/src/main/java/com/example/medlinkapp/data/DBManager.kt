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
    private const val KEY_USERS = "users"

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

    // --- State ---
    private val _medications = MutableStateFlow<List<MedicationData>>(emptyList())
    val medications: StateFlow<List<MedicationData>> = _medications.asStateFlow()

    private val _measurements = MutableStateFlow<List<DeviceData>>(emptyList())
    val measurements: StateFlow<List<DeviceData>> = _measurements.asStateFlow()

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users.asStateFlow()

    // Current Session
    private var currentUserAmka: String? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadUsers()
            loadMedications()
            loadMeasurements()
        }
    }

    fun setCurrentUser(amka: String) {
        currentUserAmka = amka
    }

    fun getCurrentUserAmka(): String? = currentUserAmka

    fun getCurrentUser(): UserData? = _users.value.find { it.amka == currentUserAmka }

    // --- Users ---
    private fun loadUsers() {
        val json = prefs?.getString(KEY_USERS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<UserData>>() {}.type
                _users.value = gson.fromJson(json, type)
            } catch (e: Exception) {
                _users.value = emptyList()
            }
        }
        
        // Add a default doctor if none exists
        if (_users.value.none { it.role == UserRole.DOCTOR }) {
            val defaultDoctor = UserData("Dr. Lee", "George", "111111", "doctor", "123", UserRole.DOCTOR)
            _users.update { it + defaultDoctor }
            saveUsers()
        }
    }

    private fun saveUsers() {
        val json = gson.toJson(_users.value)
        prefs?.edit()?.putString(KEY_USERS, json)?.apply()
    }

    fun registerUser(userData: UserData) {
        _users.update { it + userData }
        saveUsers()
    }

    // --- Medications ---
    private fun loadMedications() {
        val json = prefs?.getString(KEY_MEDICATIONS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<MedicationData>>() {}.type
                _medications.value = gson.fromJson(json, type)
            } catch (e: Exception) {
                _medications.value = emptyList()
            }
        }
    }

    private fun saveMedications() {
        val json = gson.toJson(_medications.value)
        prefs?.edit()?.putString(KEY_MEDICATIONS, json)?.apply()
    }

    fun getMedicationsForUser(amka: String): List<MedicationData> {
        return _medications.value.filter { it.patientAmka == amka }
    }

    fun addMedication(name: String, dosage: String, stock: Int, amka: String) {
        val newMed = MedicationData(
            id = System.currentTimeMillis().toString(),
            name = name,
            dosage = dosage,
            stockCount = stock,
            patientAmka = amka
        )
        _medications.update { it + newMed }
        saveMedications()
    }

    fun updateStock(medId: String, newStock: Int) {
        _medications.update { list ->
            list.map { if (it.id == medId) it.copy(stockCount = newStock) else it }
        }
        saveMedications()
    }

    // --- Measurements ---
    private fun loadMeasurements() {
        val json = prefs?.getString(KEY_MEASUREMENTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<DeviceData>>() {}.type
                _measurements.value = gson.fromJson(json, type)
            } catch (e: Exception) {
                _measurements.value = emptyList()
            }
        }
    }

    private fun saveMeasurements() {
        val json = gson.toJson(_measurements.value)
        prefs?.edit()?.putString(KEY_MEASUREMENTS, json)?.apply()
    }

    fun getMeasurementsForUser(amka: String): List<DeviceData> {
        return _measurements.value.filter { it.patientAmka == amka }
    }

    suspend fun saveMeasurement(data: DeviceData): Result<Unit> {
        delay(500)
        _measurements.update { it + data }
        saveMeasurements()
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

    // --- Mocks ---
    suspend fun getPatientInformation(patientId: String) = Unit
    suspend fun searchPatientHistory(patientId: String): Result<List<String>> = Result.success(listOf("Ιστορικό 1", "Ιστορικό 2"))
    suspend fun validatePrescription(prescription: Prescription): Boolean = true
    suspend fun checkStock(drugName: String): Int = 10
    suspend fun addDrug(prescription: Prescription): Result<Unit> = Result.success(Unit)
    suspend fun saveAppointment(appointment: Appointment): Result<Unit> = Result.success(Unit)
    fun requestDeviceData(deviceId: String): Flow<DeviceData> = flow { delay(1000) }
    suspend fun triggerEmergencySOS(patientId: String, data: String): Result<String> = Result.success("SOS Στάλθηκε")
}
