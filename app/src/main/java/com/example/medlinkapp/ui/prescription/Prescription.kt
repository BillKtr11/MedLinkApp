package com.example.medlinkapp.ui.prescription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.Prescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManagePrescriptionViewModel(
    private val dbManager: DBManager // Injected via Dependency Injection (e.g., Hilt/Dagger)
) : ViewModel() {

    // Represents the UI state (Loading, Success, Error)
    private val _uiState = MutableStateFlow<PrescriptionUiState>(PrescriptionUiState.Idle)
    val uiState: StateFlow<PrescriptionUiState> = _uiState.asStateFlow()

    // Mirrors checkPrescriptionDetails() and confirmAction() from your diagram
    fun submitPrescription(drugName: String, dosage: Int, freq: Int, duration: Int) {
        viewModelScope.launch {
            _uiState.value = PrescriptionUiState.Loading

            val prescription = Prescription(drugName, dosage, freq, duration, 0)

            try {
                val isValid = dbManager.validatePrescription(prescription)
                if (isValid) {
                    val result = dbManager.addDrug(prescription)
                    result.onSuccess {
                        _uiState.value = PrescriptionUiState.Success("Prescription confirmed!")
                    }.onFailure {
                        _uiState.value = PrescriptionUiState.Error("Failed to save to DB.")
                    }
                } else {
                    _uiState.value = PrescriptionUiState.Error("Invalid prescription details.")
                }
            } catch (e: Exception) {
                _uiState.value = PrescriptionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
sealed class PrescriptionUiState {
    object Idle : PrescriptionUiState()
    object Loading : PrescriptionUiState()
    data class Success(val message: String) : PrescriptionUiState()
    data class Error(val message: String) : PrescriptionUiState()
}