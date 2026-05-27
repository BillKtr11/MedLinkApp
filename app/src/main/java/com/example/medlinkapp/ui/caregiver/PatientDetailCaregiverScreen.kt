package com.example.medlinkapp.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    val isError by viewModel.isCommunicationError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patient?.let { "${it.name} ${it.surname}" } ?: "Λεπτομέρειες Ασθενή") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = onNavigateToStats) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Στατιστικά")
                    }
                }
            )
        }
    ) { padding ->
        if (patient == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Δεν έχει επιλεγεί ασθενής")
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                if (isError) {
                    CommunicationErrorWarning(onRetry = { viewModel.toggleCommunicationError(false) })
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Παρακολούθηση Συμμόρφωσης Φαρμάκων", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    if (medications.isEmpty()) {
                        item { Text("Δεν υπάρχουν συνταγογραφημένα φάρμακα.", color = Color.Gray) }
                    } else {
                        items(medications) { med ->
                            MedicationAdherenceItem(med)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Πρόσφατες Μετρήσεις (Τελευταία Γνωστά Δεδομένα)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    if (measurements.isEmpty()) {
                        item { Text("Δεν βρέθηκαν δεδομένα από τη συσκευή.", color = Color.Gray) }
                    } else {
                        items(measurements.sortedByDescending { it.timestamp }.take(10)) { measurement ->
                            MeasurementItem(measurement)
                        }
                    }

                    // Demo simulation button
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.toggleCommunicationError(!isError) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isError) "Εξομοίωση Αποκατάστασης Σύνδεσης" else "Εξομοίωση Σφάλματος Επικοινωνίας")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunicationErrorWarning(onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Σφάλμα",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Σφάλμα Επικοινωνίας",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Δεν ήταν δυνατή η σύνδεση με τη συσκευή του ασθενούς. Εμφανίζονται τα τελευταία διαθέσιμα δεδομένα.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            TextButton(onClick = onRetry) {
                Text("Δοκιμή")
            }
        }
    }
}
