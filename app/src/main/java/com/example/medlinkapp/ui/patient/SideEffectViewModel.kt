package com.example.medlinkapp.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.Medication
import com.example.medlinkapp.model.Message
import com.example.medlinkapp.model.SideEffectReport
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SideEffectViewModel : ViewModel() {

    val medications: StateFlow<List<Medication>> = combine(
        DBManager.medications,
        DBManager.currentUserAmka
    ) { meds, amka ->
        if (amka == null) emptyList() else meds.filter { it.patientAmka == amka }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
            
            if (result.isSuccess && intensity > 7) {
                notifyDoctor(medicationName, symptom, intensity)
            }

            _isSubmitting.value = false
            _submissionSuccess.emit(result.isSuccess)
        }
    }

    private fun notifyDoctor(medicationName: String, symptom: String, intensity: Int) {
        val currentUser = DBManager.getCurrentUser()
        val doctorAmka = currentUser?.assignedDoctorAmka
        
        println("Debug: Notifying doctor. Current user: ${currentUser?.amka}, Assigned doctor: $doctorAmka")
        
        if (doctorAmka != null) {
            val alertMessage = Message(
                id = System.currentTimeMillis().toString(),
                patientAmka = currentUser.amka,
                title = "High Intensity Side Effect Alert",
                content = "Patient ${currentUser.name} reported a high-intensity ($intensity/10) side effect for $medicationName: $symptom",
                timestamp = LocalDateTime.now()
            )
            DBManager.addMessage(alertMessage)
            println("Debug: Message added to DBManager")
        } else {
            println("Debug: No assigned doctor found for patient")
        }
    }
}
