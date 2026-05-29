package com.example.medlinkapp.ui.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientComplianceSystem(
    viewModel: CaregiverViewModel,
    onNavigateBack: () -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val stats by viewModel.adherenceStats.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val dateRangeError by viewModel.dateRangeError.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics: ${patient?.name ?: ""}") },
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
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Time Period", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("From: ${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                        Text("To: ${endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Change Period")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (stats == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                
                val isInsufficientData = stats!!.totalExpected == 0 && stats!!.measurementHistory.isEmpty() && stats!!.intakeHistory.isEmpty()

                if (isInsufficientData) {
                    InsufficientDataMessage()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ComplianceOverviewCard(stats!!)
                        }

                        item {
                            Text("Medication Compliance History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        if (stats!!.intakeHistory.isEmpty()) {
                            item {
                                Text("No records found for this period.", color = Color.Gray)
                            }
                        } else {
                            items(stats!!.intakeHistory) { record ->
                                IntakeRecordItem(record)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Measurement History (Vitals)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        if (stats!!.measurementHistory.isEmpty()) {
                            item {
                                Text("No historical measurement data available.", color = Color.Gray)
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
    }

    
    if (dateRangeError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDateRangeError() },
            title = { Text("Selection Error") },
            text = { Text(dateRangeError!!) },
            confirmButton = {
                Button(onClick = { 
                    viewModel.clearDateRangeError()
                    showDatePicker = true 
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Range") },
            text = {
                Column {
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().minusDays(30), LocalDate.now())
                        showDatePicker = false
                    }, modifier = Modifier.fillMaxWidth()) { Text("Last 30 Days") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().minusDays(7), LocalDate.now())
                        showDatePicker = false
                    }, modifier = Modifier.fillMaxWidth()) { Text("Last Week") }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().minusYears(2), LocalDate.now().minusYears(2).plusDays(1))
                        showDatePicker = false
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { 
                        Text("Select period without data (Flow 3)") 
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().plusDays(1), LocalDate.now())
                        showDatePicker = false
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { 
                        Text("Select future date (Error)") 
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun InsufficientDataMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Insufficient Data",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The available data for the selected time period is not sufficient to generate statistics.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Text("Total Compliance", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${stats.adherencePercentage.toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Doses taken: ${stats.totalTaken} / Scheduled: ${stats.totalExpected}", style = MaterialTheme.typography.bodyMedium)
            
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

