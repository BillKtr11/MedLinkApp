package com.example.medlinkapp.ui.doctor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

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
    private val allPatients = listOf(
        Patient("1", "Γιώργος Παπαδόπουλος", "12345678901"),
        Patient("2", "Μαρία Νικολάου", "09876543210")
    )

    private val allRecords = listOf(
        MedicalRecord("r1", "1", LocalDate.of(2023, 5, 10), "Φάρμακο", "Depon 500mg, 2 φορές/μέρα"),
        MedicalRecord("r2", "1", LocalDate.of(2023, 8, 15), "Μέτρηση", "Πίεση 12/8"),
        MedicalRecord("r3", "1", LocalDate.of(2024, 1, 20), "Διάγνωση", "Ελαφριά ίωση"),
        MedicalRecord("r4", "2", LocalDate.of(2024, 2, 5), "Φάρμακο", "Amoxil")
    )

    private val _searchResults = MutableStateFlow<List<Patient>>(emptyList())
    val searchResults: StateFlow<List<Patient>> = _searchResults.asStateFlow()

    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient = _selectedPatient.asStateFlow()

    private val _patientHistory = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val patientHistory = _patientHistory.asStateFlow()

    // Αναζήτηση Ασθενή
    fun searchPatient(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        _searchResults.value = allPatients.filter {
            it.name.contains(query, ignoreCase = true) || it.amka.contains(query)
        }
    }

    // Επιλογή Ασθενή και φόρτωση πλήρους ιστορικού
    fun selectPatient(patient: Patient) {
        _selectedPatient.value = patient
        _patientHistory.value = allRecords.filter { it.patientId == patient.id }.sortedByDescending { it.date }
    }

    // Φιλτράρισμα ιστορικού με χρονικό διάστημα
    fun filterHistoryByDate(startDate: LocalDate, endDate: LocalDate) {
        val patientId = _selectedPatient.value?.id ?: return
        _patientHistory.value = allRecords.filter {
            it.patientId == patientId &&
                    !it.date.isBefore(startDate) &&
                    !it.date.isAfter(endDate)
        }.sortedByDescending { it.date }
    }
}