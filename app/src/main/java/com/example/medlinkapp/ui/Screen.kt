package com.example.medlinkapp.ui

sealed class Screen(val route: String) {
    object History : Screen("history")
    object NewMeasurement : Screen("new_measurement")
    object AddMedication : Screen("add_medication")
    object ReportSymptom : Screen("report_symptom")
}
