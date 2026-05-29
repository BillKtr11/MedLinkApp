package com.example.medlinkapp.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.ReportRepository
import com.example.medlinkapp.model.HealthReport
import com.example.medlinkapp.model.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    data class PatientFound(val patient: Patient) : ReportUiState()
    data class Success(val report: HealthReport) : ReportUiState()
    data class Error(val message: String, val step: ReportStep) : ReportUiState()
}

enum class ReportStep {
    PATIENT_SEARCH, DATE_SELECTION, REPORT_READY
}

class ReportViewModel(private val repository: ReportRepository = ReportRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var selectedPatient: Patient? = null

    fun searchPatient(patientId: String) {
        _uiState.value = ReportUiState.Loading
        viewModelScope.launch {
            val result = repository.getPatient(patientId)
            result.onSuccess { patient ->
                selectedPatient = patient
                _uiState.value = ReportUiState.PatientFound(patient)
            }.onFailure {
                _uiState.value = ReportUiState.Error("Patient not found", ReportStep.PATIENT_SEARCH)
            }
        }
    }

    fun generateReport(startDate: LocalDateTime, endDate: LocalDateTime) {
        val patient = selectedPatient ?: return
        _uiState.value = ReportUiState.Loading
        viewModelScope.launch {
            val result = repository.generateHealthReport(patient.patientId, startDate, endDate)
            result.onSuccess { report ->
                _uiState.value = ReportUiState.Success(report)
            }.onFailure {
                _uiState.value = ReportUiState.Error(it.message ?: "Error generating report", ReportStep.DATE_SELECTION)
            }
        }
    }

    fun resetToStep(step: ReportStep) {
        when (step) {
            ReportStep.PATIENT_SEARCH -> {
                selectedPatient = null
                _uiState.value = ReportUiState.Idle
            }
            ReportStep.DATE_SELECTION -> {
                selectedPatient?.let {
                    _uiState.value = ReportUiState.PatientFound(it)
                } ?: run { _uiState.value = ReportUiState.Idle }
            }
            else -> {}
        }
    }
}

