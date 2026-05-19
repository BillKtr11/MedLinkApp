package com.example.medlinkapp.ui.medication

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Data model for the UI state
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

    // Mock initial data - in production, fetch this from DBManager
    private val _medications = MutableStateFlow(
        listOf(
            MedicationUiModel("1", "Metformin", "500mg", 14),
            MedicationUiModel("2", "Lisinopril", "10mg", 5, 7), // Already low stock
            MedicationUiModel("3", "Simvastatin", "20mg", 30)
        )
    )
    val medications: StateFlow<List<MedicationUiModel>> = _medications.asStateFlow()

    fun takeDose(medId: String) {
        _medications.update { currentList ->
            currentList.map { med ->
                if (med.id == medId && med.stockCount > 0) {
                    med.copy(stockCount = med.stockCount - 1)
                } else med
            }
        }
    }

    fun restock(medId: String, amount: Int = 30) {
        _medications.update { currentList ->
            currentList.map { med ->
                if (med.id == medId) {
                    med.copy(stockCount = med.stockCount + amount)
                } else med
            }
        }
    }
}