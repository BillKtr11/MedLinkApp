package com.example.medlinkapp.data

import android.content.Context
import android.content.SharedPreferences
import com.example.medlinkapp.model.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.reflect.Type
import java.time.format.DateTimeFormatter

object DBManager {

    private val _activeAlerts = MutableStateFlow<List<EmergencyAlert>>(emptyList())
    val activeAlerts: StateFlow<List<EmergencyAlert>> = _activeAlerts.asStateFlow()

    // Patient Management

    private const val PREFS_NAME = "medlink_prefs"
    private const val KEY_MEDICATIONS = "medications"
    private const val KEY_MEASUREMENTS = "measurements"
    private const val KEY_USERS = "users"
    private const val KEY_INTAKES = "intake_records"
    private const val KEY_APPOINTMENTS = "appointments"
    private const val KEY_SIDE_EFFECTS = "side_effects"
    private const val KEY_MESSAGES = "messages"
    private const val KEY_SESSION_EXPIRY = "session_expiry"
    private const val KEY_CURRENT_USER_AMKA_PERSISTENT = "current_user_amka_persistent"

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

    private val _intakeRecords = MutableStateFlow<List<IntakeRecord>>(emptyList())
    val intakeRecords: StateFlow<List<IntakeRecord>> = _intakeRecords.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _sideEffects = MutableStateFlow<List<SideEffect>>(emptyList())
    val sideEffects: StateFlow<List<SideEffect>> = _sideEffects.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Current Session
    private val _currentUserAmka = MutableStateFlow<String?>(null)
    val currentUserAmka: StateFlow<String?> = _currentUserAmka.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadUsers()
            loadMedications()
            loadMeasurements()
            loadIntakeRecords()
            loadAppointments()
            loadSideEffects()
            loadMessages()
            checkPersistentSession()
        }
    }

    private fun checkPersistentSession() {
        val amka = prefs?.getString(KEY_CURRENT_USER_AMKA_PERSISTENT, null)
        val expiryStr = prefs?.getString(KEY_SESSION_EXPIRY, null)
        if (amka != null && expiryStr != null) {
            try {
                val expiry = LocalDateTime.parse(expiryStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                if (expiry.isAfter(LocalDateTime.now())) {
                    _currentUserAmka.value = amka
                } else {
                    clearSession()
                }
            } catch (e: Exception) {
                clearSession()
            }
        }
    }

    fun isSessionValid(): Boolean {
        return _currentUserAmka.value != null
    }

    fun setCurrentUser(amka: String, keepSignedIn: Boolean = false) {
        _currentUserAmka.value = amka
        if (keepSignedIn) {
            val expiry = LocalDateTime.now().plusDays(30)
            prefs?.edit()?.apply {
                putString(KEY_CURRENT_USER_AMKA_PERSISTENT, amka)
                putString(KEY_SESSION_EXPIRY, expiry.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                apply()
            }
        } else {
            prefs?.edit()?.apply {
                remove(KEY_CURRENT_USER_AMKA_PERSISTENT)
                remove(KEY_SESSION_EXPIRY)
                apply()
            }
        }
    }

    fun clearSession() {
        _currentUserAmka.value = null
        prefs?.edit()?.apply {
            remove(KEY_CURRENT_USER_AMKA_PERSISTENT)
            remove(KEY_SESSION_EXPIRY)
            apply()
        }
    }

    fun getCurrentUserAmka(): String? = _currentUserAmka.value

    fun getCurrentUser(): UserData? = _users.value.find { it.amka == _currentUserAmka.value }

    // --- Users ---
    private fun loadUsers() {
        val json = prefs?.getString(KEY_USERS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<UserData>>() {}.type
                _users.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _users.value = emptyList()
            }
        }
        
        var changed = false
        // Add a default doctor
        if (_users.value.none { it.role == UserRole.DOCTOR }) {
            val defaultDoctor = UserData("Dr. Lee", "George", "111111", "doctor", "123", UserRole.DOCTOR)
            _users.update { it + defaultDoctor }
            changed = true
        }
        
        // Add a default caregiver
        if (_users.value.none { it.role == UserRole.CAREGIVER }) {
            val defaultCaregiver = UserData("Anna", "Caregiver", "222222", "caregiver", "123", UserRole.CAREGIVER)
            _users.update { it + defaultCaregiver }
            changed = true
        }

        // Add a default patient linked to the caregiver
        if (_users.value.none { it.email == "patient" }) {
            val defaultPatient = UserData("Demo", "Patient", "000000", "patient", "123", UserRole.PATIENT, assignedCaregiverAmka = "222222")
            _users.update { it + defaultPatient }
            changed = true
        }

        if (changed) {
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

    fun assignPatientToDoctor(patientAmka: String, doctorAmka: String) {
        _users.update { list ->
            list.map { 
                if (it.amka == patientAmka) it.copy(assignedDoctorAmka = doctorAmka) else it 
            }
        }
        saveUsers()
    }

    fun assignPatientToCaregiver(patientAmka: String, caregiverAmka: String) {
        _users.update { list ->
            list.map { 
                if (it.amka == patientAmka) it.copy(assignedCaregiverAmka = caregiverAmka) else it 
            }
        }
        saveUsers()
    }

    // --- Medications ---
    private fun loadMedications() {
        val json = prefs?.getString(KEY_MEDICATIONS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<MedicationData>>() {}.type
                _medications.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _medications.value = emptyList()
            }
        }

        if (_medications.value.none { it.patientAmka == "000000" }) {
            addMedication("Depon", "500mg", 20, "000000", listOf("08:00", "20:00"), 2)
            addMedication("Amoxil", "1g", 10, "000000", listOf("12:00"), 1)
        }
    }

    private fun saveMedications() {
        val json = gson.toJson(_medications.value)
        prefs?.edit()?.putString(KEY_MEDICATIONS, json)?.apply()
    }

    fun getMedicationsForUser(amka: String): List<MedicationData> {
        return _medications.value.filter { it.patientAmka == amka }
    }

    fun addMedication(name: String, dosage: String, stock: Int, amka: String, intakeTimes: List<String> = emptyList(), frequency: Int = 1) {
        val newMed = MedicationData(
            id = System.currentTimeMillis().toString(),
            name = name,
            dosage = dosage,
            stockCount = stock,
            patientAmka = amka,
            intakeTimes = intakeTimes,
            frequency = frequency
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

    // --- Intake Records ---
    private fun loadIntakeRecords() {
        val json = prefs?.getString(KEY_INTAKES, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<IntakeRecord>>() {}.type
                _intakeRecords.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _intakeRecords.value = emptyList()
            }
        }

        if (_intakeRecords.value.none { it.patientAmka == "000000" }) {
            _intakeRecords.update { current -> 
                current + listOf(
                    IntakeRecord("med1", "Depon", LocalDateTime.now().minusDays(1).withHour(8), "000000", "Confirmed"),
                    IntakeRecord("med1", "Depon", LocalDateTime.now().minusDays(1).withHour(20), "000000", "Confirmed"),
                    IntakeRecord("med1", "Depon", LocalDateTime.now().minusDays(2).withHour(8), "000000", "Skipped")
                )
            }
            saveIntakeRecords()
        }
    }

    private fun saveIntakeRecords() {
        val json = gson.toJson(_intakeRecords.value)
        prefs?.edit()?.putString(KEY_INTAKES, json)?.apply()
    }

    fun addIntakeRecord(record: IntakeRecord) {
        _intakeRecords.update { it + record }
        saveIntakeRecords()
    }

    // --- Appointments ---
    private fun loadAppointments() {
        val json = prefs?.getString(KEY_APPOINTMENTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Appointment>>() {}.type
                _appointments.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _appointments.value = emptyList()
            }
        }
    }

    private fun saveAppointments() {
        val json = gson.toJson(_appointments.value)
        prefs?.edit()?.putString(KEY_APPOINTMENTS, json)?.apply()
    }

    fun addAppointment(appointment: Appointment) {
        _appointments.update { it + appointment }
        saveAppointments()
        
        // Send a message to the patient
        addMessage(
            Message(
                id = System.currentTimeMillis().toString(),
                patientAmka = appointment.patientId,
                title = "Νέο Ραντεβού",
                content = "Έχετε ένα νέο ραντεβού με τον/την ${appointment.doctorName} στις ${appointment.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}.",
                timestamp = LocalDateTime.now()
            )
        )
    }

    fun deleteAppointment(appointmentId: String) {
        _appointments.update { list -> list.filter { it.appointmentId != appointmentId } }
        saveAppointments()
    }

    fun deleteMessage(messageId: String) {
        _messages.update { list -> list.filter { it.id != messageId } }
        saveMessages()
    }

    // --- Side Effects ---
    private fun loadSideEffects() {
        val json = prefs?.getString(KEY_SIDE_EFFECTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<SideEffect>>() {}.type
                _sideEffects.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _sideEffects.value = emptyList()
            }
        }

        if (_sideEffects.value.none { it.patientId == "000000" }) {
            val defaultSideEffect = SideEffect("000000", "Zάλη το πρωί", "Low", LocalDateTime.now().minusDays(2))
            _sideEffects.update { it + defaultSideEffect }
            saveSideEffects()
        }
    }

    private fun saveSideEffects() {
        val json = gson.toJson(_sideEffects.value)
        prefs?.edit()?.putString(KEY_SIDE_EFFECTS, json)?.apply()
    }

    fun addSideEffect(sideEffect: SideEffect) {
        _sideEffects.update { it + sideEffect }
        saveSideEffects()
    }

    // --- Messages ---
    private fun loadMessages() {
        val json = prefs?.getString(KEY_MESSAGES, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Message>>() {}.type
                _messages.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _messages.value = emptyList()
            }
        }
    }

    private fun saveMessages() {
        val json = gson.toJson(_messages.value)
        prefs?.edit()?.putString(KEY_MESSAGES, json)?.apply()
    }

    fun addMessage(message: Message) {
        _messages.update { it + message }
        saveMessages()
    }

    fun markMessageAsRead(messageId: String) {
        _messages.update { list ->
            list.map { if (it.id == messageId) it.copy(isRead = true) else it }
        }
        saveMessages()
    }

    fun isSlotAvailable(date: LocalDateTime): Boolean {
        return _appointments.value.none { it.date == date }
    }

    // --- Measurements ---
    private fun loadMeasurements() {
        val json = prefs?.getString(KEY_MEASUREMENTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<DeviceData>>() {}.type
                _measurements.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _measurements.value = emptyList()
            }
        }

        if (_measurements.value.none { it.patientAmka == "000000" }) {
            _measurements.update { current ->
                current + listOf(
                    DeviceData("d1", 120, "Πίεση", LocalDateTime.now().minusHours(2), "000000"),
                    DeviceData("d2", 72, "Σφύξεις", LocalDateTime.now().minusHours(2), "000000"),
                    DeviceData("d3", 98, "Οξυγόνο", LocalDateTime.now().minusDays(1), "000000")
                )
            }
            saveMeasurements()
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

        // SOS Logic: Triggered when value is outside normal limits
        val limits = getNormalLimits(data.measurementType)
        
        // Trigger SOS if value is above the high limit (as requested) or below the low limit
        val isCritical = data.measurementValue !in limits

        if (isCritical) {
            val patient = _users.value.find { it.amka == data.patientAmka }
            val newAlert = EmergencyAlert(
                id = System.currentTimeMillis().toString(),
                patientId = data.patientAmka,
                patientName = "${patient?.name ?: "Unknown"} ${patient?.surname ?: "Patient"}",
                measurementType = data.measurementType,
                value = data.measurementValue,
                timestamp = LocalDateTime.now(),
                patientAmka = data.patientAmka
            )
            _activeAlerts.update { it + newAlert }
        }

        return Result.success(Unit)
    }

    fun getNormalLimits(type: String): IntRange {
        return when (type.lowercase()) {
            "σάκχαρο", "blood glucose", "glucose" -> 70..140
            "οξυγόνο", "oxygen saturation", "oxygen", "spo2" -> 95..100
            "βάρος", "weight" -> 40..150
            "πίεση", "systolic blood pressure", "pressure", "bp" -> 90..140
            else -> 0..1000
        }
    }

    // --- Mocks & Additional Logic ---
    fun getPatientInformation(patientId: String): Result<Patient> {
        val user = _users.value.find { it.amka == patientId }
        return if (user != null) {
            Result.success(Patient(user.amka, user.name + " " + user.surname, "2000-01-01", emptyList()))
        } else {
            Result.failure(Exception("Patient not found"))
        }
    }
    suspend fun searchPatientHistory(patientId: String): Result<List<String>> = Result.success(listOf("Ιστορικό 1", "Ιστορικό 2"))
    suspend fun validatePrescription(prescription: Prescription): Boolean = true
    suspend fun checkStock(drugName: String): Int = 10
    suspend fun addDrug(prescription: Prescription): Result<Unit> = Result.success(Unit)
    suspend fun saveAppointment(appointment: Appointment): Result<Unit> = Result.success(Unit)
    
    fun requestDeviceData(deviceId: String): Flow<DeviceData> = flow { 
        while(true) {
            delay(5000)
            emit(DeviceData(deviceId, (60..100).random(), "Σφύξεις", LocalDateTime.now(), "000000"))
        }
    }

    suspend fun triggerEmergencySOS(patientId: String, data: String): Result<String> {
        val patient = _users.value.find { it.amka == patientId }
        val newAlert = EmergencyAlert(
            id = System.currentTimeMillis().toString(),
            patientId = patientId,
            patientName = "${patient?.name ?: "Unknown"} ${patient?.surname ?: "Patient"}",
            measurementType = "SOS",
            value = 0,
            timestamp = LocalDateTime.now(),
            patientAmka = patientId
        )
        _activeAlerts.update { it + newAlert }
        return Result.success("SOS Στάλθηκε")
    }

    fun respondToAlert(alertId: String, instructions: String) {
        _activeAlerts.update { list ->
            list.map { if (it.id == alertId) it.copy(status = "RESOLVED", doctorInstructions = instructions) else it }
        }
    }

    suspend fun getPatientMeasurements(patientId: String, start: LocalDateTime, end: LocalDateTime): Result<List<DeviceData>> {
        return Result.success(_measurements.value.filter { it.patientAmka == patientId && it.timestamp.isAfter(start) && it.timestamp.isBefore(end) })
    }

    suspend fun getPatientSideEffects(patientId: String, start: LocalDateTime, end: LocalDateTime): Result<List<SideEffect>> {
        return Result.success(_sideEffects.value.filter { it.patientId == patientId && it.timestamp.isAfter(start) && it.timestamp.isBefore(end) })
    }

    suspend fun getPatientPrescriptions(patientId: String): Result<List<Prescription>> {
        val meds = _medications.value.filter { it.patientAmka == patientId }
        val prescriptions = meds.map { Prescription(it.name, it.dosage.filter { c -> c.isDigit() }.toIntOrNull() ?: 0, it.frequency, 30, it.stockCount) }
        return Result.success(prescriptions)
    }
}
