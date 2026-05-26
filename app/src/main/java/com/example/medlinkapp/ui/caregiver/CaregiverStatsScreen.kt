package com.example.medlinkapp.ui.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverStatsScreen(
    viewModel: CaregiverViewModel,
    onNavigateBack: () -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val stats by viewModel.adherenceStats.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Στατιστικά: ${patient?.name ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Time Period Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Επιλογή Χρονικής Περιόδου", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Από: ${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                        Text("Έως: ${endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Αλλαγή Περιόδου")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (stats == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ComplianceOverviewCard(stats!!)
                    }

                    item {
                        Text("Ιστορικό Συμμόρφωσης Φαρμάκων", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    if (stats!!.intakeHistory.isEmpty()) {
                        item {
                            Text("Δεν βρέθηκαν εγγραφές για αυτή την περίοδο.", color = Color.Gray)
                        }
                    } else {
                        items(stats!!.intakeHistory) { record ->
                            IntakeRecordItem(record)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ιστορικό Μετρήσεων (Vitals)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    if (stats!!.measurementHistory.isEmpty()) {
                        item {
                            Text("Δεν υπάρχουν ιστορικά δεδομένα μετρήσεων.", color = Color.Gray)
                        }
                    } else {
                        items(stats!!.measurementHistory) { measurement ->
                            MeasurementItem(measurement)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Επιλογή Εύρους") },
            text = {
                Column {
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().minusDays(30), LocalDate.now())
                        showDatePicker = false
                    }, modifier = Modifier.fillMaxWidth()) { Text("Τελευταίες 30 Ημέρες") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().minusDays(7), LocalDate.now())
                        showDatePicker = false
                    }, modifier = Modifier.fillMaxWidth()) { Text("Τελευταία Εβδομάδα") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Κλείσιμο") }
            }
        )
    }
}

@Composable
fun ComplianceOverviewCard(stats: AdherenceStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Συνολική Συμμόρφωση", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${stats.adherencePercentage.toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Λήψεις: ${stats.totalTaken} / Προγραμματισμένες: ${stats.totalExpected}", style = MaterialTheme.typography.bodyMedium)
            
            LinearProgressIndicator(
                progress = { stats.adherencePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
