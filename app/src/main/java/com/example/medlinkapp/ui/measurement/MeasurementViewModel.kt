package com.example.medlinkapp.ui.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.Measurement
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed class MeasurementState {
    object Idle : MeasurementState()
    object Loading : MeasurementState()
    data class Success(val message: String) : MeasurementState()
    data class Error(val message: String, val isOutOfBounds: Boolean = false) : MeasurementState()
}

class MeasurementViewModel(private val dbManager: DBManager) : ViewModel() {

    private val _uiState = MutableStateFlow<MeasurementState>(MeasurementState.Idle)
    val uiState: StateFlow<MeasurementState> = _uiState.asStateFlow()

    
    val measurements: StateFlow<List<Measurement>> = combine(dbManager.measurements, dbManager.currentUserAmka) { list, amka ->
        list.filter { it.patientAmka == amka }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitMeasurement(type: String, valueStr: String, method: String) {
        val value = valueStr.toIntOrNull()
        if (value == null) {
            _uiState.value = MeasurementState.Error("Please enter a valid value.")
            return
        }

        val amka = dbManager.getCurrentUserAmka() ?: return

        viewModelScope.launch {
            _uiState.value = MeasurementState.Loading

            val measurementData = Measurement(
                deviceId = if (method == "Connect device (Bluetooth)") "BT_DEVICE_01" else "MANUAL_ENTRY",
                measurementValue = value,
                measurementType = type,
                timestamp = LocalDateTime.now(),
                patientAmka = amka
            )

            val result = dbManager.saveMeasurement(measurementData)

            result.onSuccess {
                if (!isWithinNormalLimits(type, value)) {
                    _uiState.value = MeasurementState.Success("Measurement recorded, but the value is out of limits! Your doctor has been notified.")
                } else {
                    _uiState.value = MeasurementState.Success("Measurement recorded successfully.")
                }
            }.onFailure {
                _uiState.value = MeasurementState.Error("There was a problem during saving.")
            }
        }
    }

    fun fetchBluetoothData(type: String, onDataReceived: (String) -> Unit) {
        val simulatedValue = when(type) {
            "Blood Pressure" -> "120"
            "Glucose" -> "95"
            "Weight" -> "75"
            "Oxygen" -> "98"
            else -> "0"
        }
        onDataReceived(simulatedValue)
    }

    private fun isWithinNormalLimits(type: String, value: Int): Boolean {
        val limits = dbManager.getNormalLimits(type)
        return value in limits
    }

    fun resetState() {
        _uiState.value = MeasurementState.Idle
    }
}

