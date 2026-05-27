package com.example.medlinkapp.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.data.MedicationData
import com.example.medlinkapp.model.SideEffectReport
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SideEffectViewModel : ViewModel() {

    val medications: StateFlow<List<MedicationData>> = DBManager.medications

    private val _isSubmitting = MutableStateFlow(value = false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submissionSuccess = MutableSharedFlow<Boolean>()
    val submissionSuccess = _submissionSuccess.asSharedFlow()

    fun submitReport(
        medicationId: String,
        medicationName: String,
        symptom: String,
        duration: String,
        intensity: Int,
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val report = SideEffectReport(
                id = System.currentTimeMillis().toString(),
                medicationId = medicationId,
                medicationName = medicationName,
                symptom = symptom,
                duration = duration,
                intensity = intensity,
                timestamp = LocalDateTime.now(),
            )
            val result = DBManager.saveSideEffectReport(report)
            _isSubmitting.value = false
            _submissionSuccess.emit(result.isSuccess)
        }
    }
}
