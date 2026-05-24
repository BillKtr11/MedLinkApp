package com.example.medlinkapp.ui

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object NewMeasurement : Screen("new_measurement")
}