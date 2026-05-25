package com.example.medlinkapp.ui.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.DeviceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun submitMeasurement(type: String, valueStr: String, method: String) {
        val value = valueStr.toIntOrNull()
        if (value == null) {
            _uiState.value = MeasurementState.Error("Παρακαλώ εισάγετε μια έγκυρη τιμή.")
            return
        }

        viewModelScope.launch {
            _uiState.value = MeasurementState.Loading

            if (!isWithinNormalLimits(type, value)) {
                _uiState.value = MeasurementState.Error(
                    message = "Προειδοποίηση: Η τιμή $value βρίσκεται εκτός των φυσιολογικών ορίων για $type. Παρακαλώ ακολουθήστε τις οδηγίες του ιατρού σας.",
                    isOutOfBounds = true
                )
                return@launch
            }

            val deviceData = DeviceData(
                deviceId = if (method == "Bluetooth") "BT_DEVICE_01" else "MANUAL_ENTRY",
                measurementValue = value,
                measurementType = type,
                timestamp = LocalDateTime.now()
            )

            val result = dbManager.saveMeasurement(deviceData)

            result.onSuccess {
                _uiState.value = MeasurementState.Success("Η μέτρηση καταγράφηκε επιτυχώς.")
            }.onFailure {
                _uiState.value = MeasurementState.Error("Υπήρξε πρόβλημα κατά την αποθήκευση.")
            }
        }
    }

    fun fetchBluetoothData(type: String, onDataReceived: (String) -> Unit) {
        val simulatedValue = when(type) {
            "Πίεση" -> "120"
            "Σάκχαρο" -> "95"
            "Βάρος" -> "75"
            "Οξυγόνο" -> "98"
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