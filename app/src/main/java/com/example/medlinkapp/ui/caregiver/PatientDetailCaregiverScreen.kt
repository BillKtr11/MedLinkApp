package com.example.medlinkapp.ui.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medlinkapp.model.MedicationData
import com.example.medlinkapp.model.DeviceData
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailCaregiverScreen(
    viewModel: CaregiverViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val medications by viewModel.patientMedications.collectAsState()
    val measurements by viewModel.patientMeasurements.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patient?.let { "${it.name} ${it.surname}" } ?: "Patient Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = onNavigateToStats) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Statistics")
                    }
                }
            )
        }
    ) { padding ->
        if (patient == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No patient selected")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Real-time Medication Adherence", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                if (medications.isEmpty()) {
                    item { Text("No medications prescribed.", color = Color.Gray) }
                } else {
                    items(medications) { med ->
                        MedicationAdherenceItem(med)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recent Vital Signs (Device Data)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                if (measurements.isEmpty()) {
                    item { Text("No measurements retrieved from device.", color = Color.Gray) }
                } else {
                    items(measurements.sortedByDescending { it.timestamp }.take(5)) { measurement ->
                        MeasurementItem(measurement)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationAdherenceItem(med: MedicationData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(med.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("Dosage: ${med.dosage}", style = MaterialTheme.typography.bodyMedium)
                }
                // Status badge
                Surface(
                    color = if (med.stockCount > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (med.stockCount > 0) "Active" else "Low Stock",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Intake Schedule: ${med.intakeTimes.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MeasurementItem(data: DeviceData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(data.measurementType, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    data.timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                "${data.measurementValue}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
