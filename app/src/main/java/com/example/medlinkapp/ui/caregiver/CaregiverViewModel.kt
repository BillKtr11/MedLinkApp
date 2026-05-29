package com.example.medlinkapp.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class AdherenceStats(
    val totalExpected: Int,
    val totalTaken: Int,
    val adherencePercentage: Float,
    val intakeHistory: List<IntakeRecord>,
    val measurementHistory: List<Measurement>
)

@OptIn(ExperimentalCoroutinesApi::class)
class CaregiverViewModel : ViewModel() {

    
    val myPatients: StateFlow<List<User>> = combine(DBManager.users, DBManager.currentUserAmka) { users, caregiverAmka ->
        users.filter { it.role == UserRole.PATIENT && it.assignedCaregiverAmka == caregiverAmka }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    
    val allPatients: StateFlow<List<User>> = DBManager.users
        .map { users -> users.filter { it.role == UserRole.PATIENT } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedPatient = MutableStateFlow<User?>(null)
    val selectedPatient = _selectedPatient.asStateFlow()

    
    private val _isCommunicationError = MutableStateFlow(false)
    val isCommunicationError = _isCommunicationError.asStateFlow()

    
    val patientMedications: StateFlow<List<Medication>> = _selectedPatient.flatMapLatest { patient ->
        if (patient == null) flowOf(emptyList())
        else DBManager.medications.map { meds -> meds.filter { it.patientAmka == patient.amka } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val patientMeasurements: StateFlow<List<Measurement>> = _selectedPatient.flatMapLatest { patient ->
        if (patient == null) flowOf(emptyList())
        else DBManager.measurements.map { measurements -> measurements.filter { it.patientAmka == patient.amka } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val patientIntakeRecords: StateFlow<List<IntakeRecord>> = _selectedPatient.flatMapLatest { patient ->
        if (patient == null) flowOf(emptyList())
        else DBManager.intakeRecords.map { records -> records.filter { it.patientAmka == patient.amka } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    
    private val _startDate = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(7))
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val endDate = _endDate.asStateFlow()

    
    private val _dateRangeError = MutableStateFlow<String?>(null)
    val dateRangeError = _dateRangeError.asStateFlow()

    
    val adherenceStats: StateFlow<AdherenceStats?> = combine(
        listOf(_selectedPatient, patientIntakeRecords, patientMedications, patientMeasurements, startDate, endDate)
    ) { flows ->
        val patient = flows[0] as? User
        @Suppress("UNCHECKED_CAST")
        val records = flows[1] as List<IntakeRecord>
        @Suppress("UNCHECKED_CAST")
        val meds = flows[2] as List<Medication>
        @Suppress("UNCHECKED_CAST")
        val measurements = flows[3] as List<Measurement>
        val start = flows[4] as LocalDate
        val end = flows[5] as LocalDate

        if (patient == null) return@combine null

        val filteredRecords = records.filter { 
            val date = it.timestamp.toLocalDate()
            (date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))
        }

        val filteredMeasurements = measurements.filter {
            val date = it.timestamp.toLocalDate()
            (date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))
        }

        val days = (end.toEpochDay() - start.toEpochDay()).toInt() + 1
        val totalExpected = meds.sumOf { it.frequency * days }
        val totalTaken = filteredRecords.count { it.status == "Confirmed" }
        
        val percentage = if (totalExpected > 0) (totalTaken.toFloat() / totalExpected) * 100 else 0f
        
        AdherenceStats(
            totalExpected = totalExpected,
            totalTaken = totalTaken,
            adherencePercentage = percentage,
            intakeHistory = filteredRecords.sortedByDescending { it.timestamp },
            measurementHistory = filteredMeasurements.sortedByDescending { it.timestamp }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun selectPatient(patient: User) {
        _selectedPatient.value = patient
    }

    fun toggleCommunicationError(hasError: Boolean) {
        _isCommunicationError.value = hasError
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        if (start.isAfter(end)) {
            _dateRangeError.value = "Start date cannot be after end date."
            return
        }
        if (end.isAfter(LocalDate.now())) {
            _dateRangeError.value = "End date cannot be in the future."
            return
        }
        
        _dateRangeError.value = null
        _startDate.value = start
        _endDate.value = end
    }

    fun clearDateRangeError() {
        _dateRangeError.value = null
    }

    fun assignPatientToMe(patientAmka: String) {
        val caregiverAmka = DBManager.getCurrentUserAmka() ?: return
        DBManager.assignPatientToCaregiver(patientAmka, caregiverAmka)
    }
}

