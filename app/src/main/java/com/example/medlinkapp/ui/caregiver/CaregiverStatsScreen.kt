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
import com.example.medlinkapp.model.IntakeRecord
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
            // Time Period Selection
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ComplianceOverviewCard(stats!!)
                    }

                    item {
                        Text("Medication Adherence History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    if (stats!!.history.isEmpty()) {
                        item {
                            Text("No records found for this period.", color = Color.Gray)
                        }
                    } else {
                        items(stats!!.history) { record ->
                            IntakeRecordItem(record)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        // Simplified Date Range Picker logic
        // In a real app, use DateRangePicker from M3
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Range (Mock)") },
            text = {
                Column {
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().minusDays(30), LocalDate.now())
                        showDatePicker = false
                    }) { Text("Last 30 Days") }
                    Button(onClick = {
                        viewModel.setDateRange(LocalDate.now().minusDays(7), LocalDate.now())
                        showDatePicker = false
                    }) { Text("Last 7 Days") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Close") }
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
            Text("Overall Compliance", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${stats.adherencePercentage.toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Taken: ${stats.totalTaken} / Expected: ${stats.totalExpected}", style = MaterialTheme.typography.bodyMedium)
            
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

@Composable
fun IntakeRecordItem(record: IntakeRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(record.medName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    record.timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                record.status,
                color = if (record.status == "Confirmed") Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
