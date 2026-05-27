package com.example.medlinkapp.ui.doctor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.Appointment
import com.example.medlinkapp.model.UserData
import com.example.medlinkapp.model.UserRole
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalDateTime

data class Patient(
    val id: String,
    val name: String,
    val amka: String
)
data class MedicalRecord(
    val id: String,
    val patientId: String,
    val date: LocalDate,
    val type: String,
    val description: String
)

class DoctorViewModel : ViewModel() {
    
    // Live list of ALL patients in the system (for assignment)
    val allPatients: StateFlow<List<UserData>> = DBManager.users
        .map { users -> users.filter { it.role == UserRole.PATIENT } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Patients specifically assigned to this doctor (for search and appointments)
    // Uses combine to react to both user list changes and current doctor changes
    val myPatients: StateFlow<List<UserData>> = combine(DBManager.users, DBManager.currentUserAmka) { users, doctorAmka ->
        users.filter { it.role == UserRole.PATIENT && it.assignedDoctorAmka == doctorAmka }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val allRecords = listOf(
        MedicalRecord("r1", "1", LocalDate.of(2023, 5, 10), "Φάρμακο", "Depon 500mg, 2 φορές/μέρα"),
        MedicalRecord("r2", "1", LocalDate.of(2023, 8, 15), "Μέτρηση", "Πίεση 12/8"),
        MedicalRecord("r3", "1", LocalDate.of(2024, 1, 20), "Διάγνωση", "Ελαφριά ίωση"),
        MedicalRecord("r4", "2", LocalDate.of(2024, 2, 5), "Φάρμακο", "Amoxil")
    )

    private val _searchResults = MutableStateFlow<List<UserData>>(emptyList())
    val searchResults: StateFlow<List<UserData>> = _searchResults.asStateFlow()

    private val _selectedPatient = MutableStateFlow<UserData?>(null)
    val selectedPatient = _selectedPatient.asStateFlow()

    private val _patientHistory = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val patientHistory = _patientHistory.asStateFlow()

    // Appointments for this doctor specifically
    val appointments: StateFlow<List<Appointment>> = combine(DBManager.appointments, DBManager.currentUserAmka) { appointments, doctorAmka ->
        val doctor = DBManager.users.value.find { it.amka == doctorAmka }
        val doctorName = if (doctor != null) "${doctor.name} ${doctor.surname}" else "Doctor"
        appointments.filter { it.doctorName == doctorName }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Search specifically within MY patients
    fun searchPatient(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        _searchResults.value = myPatients.value.filter {
            it.name.contains(query, ignoreCase = true) || 
            it.surname.contains(query, ignoreCase = true) || 
            it.amka.contains(query)
        }
    }

    fun assignPatient(patientAmka: String) {
        val doctorAmka = DBManager.getCurrentUserAmka() ?: return
        DBManager.assignPatientToDoctor(patientAmka, doctorAmka)
    }

    fun selectPatient(patient: UserData) {
        _selectedPatient.value = patient
        _patientHistory.value = allRecords.filter { it.patientId == patient.amka }.sortedByDescending { it.date }
    }

    fun filterHistoryByDate(startDate: LocalDate, endDate: LocalDate) {
        val amka = _selectedPatient.value?.amka ?: return
        _patientHistory.value = allRecords.filter {
            it.patientId == amka &&
                    !it.date.isBefore(startDate) &&
                    !it.date.isAfter(endDate)
        }.sortedByDescending { it.date }
    }

    fun addAppointment(date: LocalDateTime, reason: String, patientAmka: String): Result<Unit> {
        if (reason.isBlank()) return Result.failure(Exception("Reason cannot be empty"))
        if (patientAmka.isBlank()) return Result.failure(Exception("Please select a patient"))
        
        val doctor = DBManager.getCurrentUser()
        val doctorName = if (doctor != null) "${doctor.name} ${doctor.surname}" else "Doctor"

        if (!DBManager.isSlotAvailable(date)) {
            return Result.failure(Exception("Η επιλεγμένη ώρα δεν είναι διαθέσιμη. Παρακαλώ επιλέξτε άλλη ώρα."))
        }

        val newAppointment = Appointment(
            appointmentId = System.currentTimeMillis().toString(),
            patientId = patientAmka,
            doctorName = doctorName,
            date = date,
            reason = reason
        )
        DBManager.addAppointment(newAppointment)
        return Result.success(Unit)
    }

    fun cancelAppointment(appointmentId: String) {
        DBManager.deleteAppointment(appointmentId)
    }
}