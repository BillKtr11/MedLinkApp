package com.example.medlinkapp.ui.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import kotlinx.coroutines.flow.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class MedicationUiModel(
    val id: String,
    val name: String,
    val dosage: String,
    val stockCount: Int,
    val lowStockThreshold: Int = 10,
    val intakeTimes: List<String> = emptyList()
) {
    val isLowStock: Boolean get() = stockCount <= lowStockThreshold

    fun getNextIntakeTime(): String? {
        if (intakeTimes.isEmpty()) return null
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        
        // Find the first time that is after now
        val nextTime = intakeTimes.map { LocalTime.parse(it, formatter) }
            .filter { it.isAfter(now) }
            .minOrNull()
        
        // If no time today is after now, the next one is the first one tomorrow
        return (nextTime ?: intakeTimes.map { LocalTime.parse(it, formatter) }.minOrNull())?.format(formatter)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationViewModel : ViewModel() {

    // Fetching data from the persistent singleton DBManager, filtered by user
    val medications: StateFlow<List<MedicationUiModel>> = combine(DBManager.medications, DBManager.currentUserAmka) { list, amka ->
        list.filter { it.patientAmka == amka }
            .map { 
                MedicationUiModel(it.id, it.name, it.dosage, it.stockCount, it.lowStockThreshold, it.intakeTimes ?: emptyList())
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // New: Fetch prescriptions for the current patient
    val prescriptions: StateFlow<List<com.example.medlinkapp.model.Prescription>> = DBManager.prescriptions.map { list ->
        val amka = DBManager.getCurrentUserAmka()
        list.filter { it.patientAmka == amka }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun confirmIntake(medId: String) {
        val currentMed = medications.value.find { it.id == medId }
        val amka = DBManager.getCurrentUserAmka() ?: return
        if (currentMed != null && currentMed.stockCount > 0) {
            DBManager.updateStock(medId, currentMed.stockCount - 1)
            DBManager.addIntakeRecord(
                com.example.medlinkapp.model.IntakeRecord(
                    medId = medId,
                    medName = currentMed.name,
                    timestamp = java.time.LocalDateTime.now(),
                    patientAmka = amka,
                    status = "Confirmed"
                )
            )
        }
    }

    fun skipIntake(medId: String) {
        val currentMed = medications.value.find { it.id == medId }
        val amka = DBManager.getCurrentUserAmka() ?: return
        if (currentMed != null) {
            DBManager.addIntakeRecord(
                com.example.medlinkapp.model.IntakeRecord(
                    medId = medId,
                    medName = currentMed.name,
                    timestamp = java.time.LocalDateTime.now(),
                    patientAmka = amka,
                    status = "Skipped"
                )
            )
        }
    }

    fun restock(medId: String, amount: Int) {
        val currentMed = medications.value.find { it.id == medId }
        if (currentMed != null) {
            DBManager.updateStock(medId, currentMed.stockCount + amount)
        }
    }

    fun addMedication(name: String, dosage: String, stock: Int, frequency: Int, intakeTimes: List<String>) {
        val amka = DBManager.getCurrentUserAmka() ?: return
        DBManager.addMedication(name, dosage, stock, amka, intakeTimes, frequency)
    }

    fun addFromPrescription(prescription: com.example.medlinkapp.model.Prescription) {
        val amka = DBManager.getCurrentUserAmka() ?: return
        
        // Create intake times based on frequency
        val intakeTimes = mutableListOf<String>()
        val freq = prescription.drugFreq
        if (freq > 0) {
            val interval = 24 / freq
            for (i in 0 until freq) {
                val hour = (8 + i * interval) % 24
                intakeTimes.add(String.format(Locale.getDefault(), "%02d:00", hour))
            }
        }
        
        // Determine dosage string with unit (defaulting to mg if none specified, though model is Int)
        val dosageStr = if (prescription.drugDosage > 0) "${prescription.drugDosage}mg" else "As directed"

        DBManager.addMedication(
            name = prescription.drugName,
            dosage = dosageStr,
            stock = prescription.drugStock,
            amka = amka,
            intakeTimes = intakeTimes,
            frequency = freq
        )
    }
}
