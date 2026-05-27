package com.example.medlinkapp.ui.doctor

import androidx.lifecycle.ViewModel
import com.example.medlinkapp.model.Prescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import kotlinx.coroutines.flow.update

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
        if (medication.isBlank()) return "Το όνομα του φαρμάκου δεν μπορεί να είναι κενό."
        if (dosage.isBlank()) return "Η δοσολογία δεν μπορεί να είναι κενή."
        if (frequency.isBlank()) return "Η συχνότητα δεν μπορεί να είναι κενή."
        if (duration.isBlank()) return "Η διάρκεια δεν μπορεί να είναι κενή."

        // ΒΗΜΑ 4: Αποθήκευση της συνταγής στο σύστημα
        val newPrescription = Prescription(
            id = "presc_${System.currentTimeMillis()}",
            patientId = patientId,
            medication = medication,
            dosage = dosage,
            frequency = frequency,
            duration = duration,
            dateIssued = LocalDate.now()
        )
        _prescriptions.update { it + newPrescription }

        // --- ΠΡΟΣΘΗΚΗ: Αυτόματη εισαγωγή της συνταγής στο Ιατρικό Ιστορικό του Ασθενή ---
        // Δημιουργούμε ένα record με τα στοιχεία που έγραψε ο γιατρός
        val newRecord = MedicalRecord(
            id = "rec_${System.currentTimeMillis()}",
            patientId = patientId,
            date = LocalDate.now(),
            type = "Συνταγογράφηση", // Εμφανίζεται με μπλε/primary χρώμα στην κάρτα
            description = "Φάρμακο: $medication\nΔοσολογία: $dosage\nΣυχνότητα: $frequency\nΔιάρκεια: $duration"
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
}
