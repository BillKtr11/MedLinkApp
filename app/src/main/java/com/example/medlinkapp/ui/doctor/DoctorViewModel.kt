package com.example.medlinkapp.ui.doctor

import androidx.lifecycle.ViewModel
import com.example.medlinkapp.model.Prescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.Appointment
import com.example.medlinkapp.model.EmergencyAlert
import com.example.medlinkapp.model.User
import com.example.medlinkapp.model.UserRole
import kotlinx.coroutines.flow.*
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
    
    // Emergency Alerts
    val activeAlerts: StateFlow<List<EmergencyAlert>> = DBManager.activeAlerts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun respondToAlert(alertId: String, instructions: String) {
        DBManager.respondToAlert(alertId, instructions)
    }

    // Live list of ALL patients in the system (for assignment)
    val allPatients: StateFlow<List<User>> = DBManager.users
        .map { users -> users.filter { it.role == UserRole.PATIENT } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Patients specifically assigned to this doctor (for search and appointments)
    // Uses combine to react to both user list changes and current doctor changes
    val myPatients: StateFlow<List<User>> = combine(DBManager.users, DBManager.currentUserAmka) { users, doctorAmka ->
        users.filter { it.role == UserRole.PATIENT && it.assignedDoctorAmka == doctorAmka }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val allRecords = listOf(
        MedicalRecord("r1", "1", LocalDate.of(2023, 5, 10), "Medication", "Depon 500mg, 2 times/day"),
        MedicalRecord("r2", "1", LocalDate.of(2023, 8, 15), "Measurement", "Blood Pressure 12/8"),
        MedicalRecord("r3", "1", LocalDate.of(2024, 1, 20), "Diagnosis", "Mild virus"),
        MedicalRecord("r4", "2", LocalDate.of(2024, 2, 5), "Medication", "Amoxil")
    )

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _selectedPatient = MutableStateFlow<User?>(null)
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

    fun selectPatient(patient: User) {
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
    // Λίστα με τις αποθηκευμένες συνταγές
    private val _prescriptions = MutableStateFlow<List<Prescription>>(emptyList())
    val prescriptions = _prescriptions.asStateFlow()

    /**
     * Υλοποίηση Βημάτων 2, 4, 5 και 6
     * Επιστρέφει null αν όλα είναι εντάξει, ή μήνυμα λάθους αν αποτύχει ο έλεγχος.
     */
    fun issuePrescription(
        patientId: String,
        medication: String,
        dosage: String,
        frequency: String,
        duration: String
    ): String? {

        // ΒΗΜΑ 2: Έλεγχος εγκυρότητας στοιχείων
        if (medication.isBlank()) return "Medication name cannot be empty."
        if (dosage.isBlank()) return "Dosage cannot be empty."
        if (frequency.isBlank()) return "Frequency cannot be empty."
        if (duration.isBlank()) return "Duration cannot be empty."

        // ΒΗΜΑ 4: Αποθήκευση της συνταγής στο σύστημα
        val dosageInt = dosage.filter { it.isDigit() }.toIntOrNull() ?: 0
        val freqInt = frequency.filter { it.isDigit() }.toIntOrNull() ?: 1
        val durInt = duration.filter { it.isDigit() }.toIntOrNull() ?: 30
        
        val newPrescription = Prescription(
            id = "presc_${System.currentTimeMillis()}",
            patientAmka = patientId,
            drugName = medication,
            drugDosage = dosageInt,
            drugFreq = freqInt,
            drugDuration = durInt,
            drugStock = freqInt * durInt, // Suggest enough stock for the whole duration
            dateIssued = LocalDate.now()
        )
        DBManager.addPrescription(newPrescription)

        // --- ΠΡΟΣΘΗΚΗ: Αυτόματη εισαγωγή της συνταγής στο Ιατρικό Ιστορικό του Ασθενή ---
        // Δημιουργούμε ένα record με τα στοιχεία που έγραψε ο γιατρός
        val newRecord = MedicalRecord(
            id = "rec_${System.currentTimeMillis()}",
            patientId = patientId,
            date = LocalDate.now(),
            type = "Prescription", // Shows with blue/primary color on card
            description = "Medication: $medication\nDosage: $dosage\nFrequency: $frequency\nDuration: $duration"
        )

        // Ενημερώνουμε τη λίστα του ιστορικού.
        // Το προσθέτουμε στην αρχή της λίστας (newRecord + it) για να φαίνεται πρώτο-πρώτο πάνω στην οθόνη
        _patientHistory.update { listOf(newRecord) + it }

        // ΒΗΜΑ 5: Αυτόματη ενημέρωση του προγράμματος φαρμάκων του ασθενή (Simulated)
        println("SYSTEM: Το πρόγραμμα φαρμάκων του ασθενή $patientId ενημερώθηκε με το φάρμακο $medication.")

        // ΒΗΜΑ 6: Αποστολή ειδοποίησης στον ασθενή (Simulated)
        println("SYSTEM: Εστάλη ειδοποίηση (Notification) στον ασθενή $patientId: 'Ο γιατρός σας εξέδωσε νέα συνταγή'.")

        return null // Επιστροφή null σημαίνει επιτυχία χωρίς σφάλματα
    }

    fun addAppointment(date: LocalDateTime, reason: String, patientAmka: String): Result<Unit> {
        if (reason.isBlank()) return Result.failure(Exception("Reason cannot be empty"))
        if (patientAmka.isBlank()) return Result.failure(Exception("Please select a patient"))
        
        val doctor = DBManager.getCurrentUser()
        val doctorName = if (doctor != null) "${doctor.name} ${doctor.surname}" else "Doctor"

        if (!DBManager.isSlotAvailable(date)) {
            return Result.failure(Exception("The selected time is not available. Please choose another time."))
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
