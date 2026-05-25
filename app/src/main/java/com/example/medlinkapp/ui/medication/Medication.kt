package com.example.medlinkapp.ui.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import kotlinx.coroutines.flow.*

data class MedicationUiModel(
    val id: String,
    val name: String,
    val dosage: String,
    val stockCount: Int,
    val lowStockThreshold: Int = 10
) {
    val isLowStock: Boolean get() = stockCount <= lowStockThreshold
}

class MedicationViewModel : ViewModel() {

    // Fetching data from the persistent singleton DBManager, filtered by user
    val medications: StateFlow<List<MedicationUiModel>> = DBManager.medications
        .map { list ->
            val amka = DBManager.getCurrentUserAmka()
            list.filter { it.patientAmka == amka }
                .map { 
                    MedicationUiModel(it.id, it.name, it.dosage, it.stockCount, it.lowStockThreshold)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun takeDose(medId: String) {
        val currentMed = medications.value.find { it.id == medId }
        if (currentMed != null && currentMed.stockCount > 0) {
            DBManager.updateStock(medId, currentMed.stockCount - 1)
        }
    }

    fun restock(medId: String, amount: Int = 30) {
        val currentMed = medications.value.find { it.id == medId }
        if (currentMed != null) {
            DBManager.updateStock(medId, currentMed.stockCount + amount)
        }
    }

    fun addMedication(name: String, dosage: String, stock: Int) {
        val amka = DBManager.getCurrentUserAmka() ?: return
        DBManager.addMedication(name, dosage, stock, amka)
    }
}
