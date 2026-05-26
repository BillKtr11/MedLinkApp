package com.example.medlinkapp.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalDateTime

data class AdherenceStats(
    val totalExpected: Int,
    val totalTaken: Int,
    val adherencePercentage: Float,
    val history: List<IntakeRecord>
)

class CaregiverViewModel : ViewModel() {

    // Patients specifically assigned to this caregiver
    val myPatients: StateFlow<List<UserData>> = combine(DBManager.users, DBManager.currentUserAmka) { users, caregiverAmka ->
        users.filter { it.role == UserRole.PATIENT && it.assignedCaregiverAmka == caregiverAmka }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // All patients (to allow assignment)
    val allPatients: StateFlow<List<UserData>> = DBManager.users
        .map { users -> users.filter { it.role == UserRole.PATIENT } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedPatient = MutableStateFlow<UserData?>(null)
    val selectedPatient = _selectedPatient.asStateFlow()

    // Real-time monitoring data
    val patientMedications = _selectedPatient.flatMapLatest { patient ->
        if (patient == null) flowOf(emptyList<MedicationData>())
        else DBManager.medications.map { meds -> meds.filter { it.patientAmka == patient.amka } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val patientMeasurements = _selectedPatient.flatMapLatest { patient ->
        if (patient == null) flowOf(emptyList<DeviceData>())
        else DBManager.measurements.map { measurements -> measurements.filter { it.patientAmka == patient.amka } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val patientIntakeRecords = _selectedPatient.flatMapLatest { patient ->
        if (patient == null) flowOf(emptyList<IntakeRecord>())
        else DBManager.intakeRecords.map { records -> records.filter { it.patientAmka == patient.amka } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Stats
    private val _startDate = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(7))
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val endDate = _endDate.asStateFlow()

    val adherenceStats = combine(_selectedPatient, patientIntakeRecords, patientMedications, _startDate, _endDate) { patient, records, meds, start, end ->
        if (patient == null) return@combine null

        val filteredRecords = records.filter { 
            val date = it.timestamp.toLocalDate()
            !date.isBefore(start) && !date.isAfter(end)
        }

        // Simplistic calculation: count "Confirmed" vs total expected in period
        // For a real app, we'd calculate based on medication frequency over days
        val days = (end.toEpochDay() - start.toEpochDay()).toInt() + 1
        val totalExpected = meds.sumOf { it.frequency * days }
        val totalTaken = filteredRecords.count { it.status == "Confirmed" }
        
        val percentage = if (totalExpected > 0) (totalTaken.toFloat() / totalExpected) * 100 else 0f
        
        AdherenceStats(
            totalExpected = totalExpected,
            totalTaken = totalTaken,
            adherencePercentage = percentage,
            history = filteredRecords.sortedByDescending { it.timestamp }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun selectPatient(patient: UserData) {
        _selectedPatient.value = patient
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
    }

    fun assignPatientToMe(patientAmka: String) {
        val caregiverAmka = DBManager.getCurrentUserAmka() ?: return
        DBManager.assignPatientToCaregiver(patientAmka, caregiverAmka)
    }
}
