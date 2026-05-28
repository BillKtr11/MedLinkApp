package com.example.medlinkapp.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMedicationIntake(
    viewModel: MedicationViewModel = viewModel(),
    onBackClick: () -> Unit,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToIntake: (String) -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val prescriptions by viewModel.prescriptions.collectAsState()

    var showRestockDialog by remember { mutableStateOf(false) }
    var restockMedId by remember { mutableStateOf<String?>(null) }
    var restockAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Medications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddMedication) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (prescriptions.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Prescriptions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(prescriptions) { presc ->
                    PrescriptionCard(
                        prescription = presc,
                        onAddMedication = { viewModel.addFromPrescription(presc) }
                    )
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            item {
                Text(
                    text = "Current Inventory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(medications) { med ->
                MedicationCard(
                    medication = med,
                    onTakeDose = { onNavigateToIntake(med.id) },
                    onRestock = { 
                        restockMedId = med.id
                        showRestockDialog = true
                    }
                )
            }
        }

        if (showRestockDialog) {
            AlertDialog(
                onDismissRequest = { showRestockDialog = false },
                title = { Text("Restock Medication") },
                text = {
                    Column {
                        Text("Enter the number of new doses:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = restockAmount,
                            onValueChange = { restockAmount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val amount = restockAmount.toIntOrNull() ?: 0
                        restockMedId?.let { viewModel.restock(it, amount) }
                        showRestockDialog = false
                        restockAmount = ""
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestockDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PrescriptionCard(
    prescription: com.example.medlinkapp.model.Prescription,
    onAddMedication: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = prescription.drugName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "Dosage: ${prescription.drugDosage}mg", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Frequency: ${prescription.drugFreq} times/day", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Duration: ${prescription.drugDuration} days", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onAddMedication,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add to Medications")
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: MedicationUiModel,
    onTakeDose: () -> Unit,
    onRestock: () -> Unit
) {
    val cardColor = if (medication.isLowStock)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = medication.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = medication.dosage, style = MaterialTheme.typography.bodyMedium)
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (medication.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "Stock: ${medication.stockCount}",
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (medication.isLowStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Low Stock", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refill required soon. Threshold: ${medication.lowStockThreshold}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onRestock) {
                    Text("Restock (+30)")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onTakeDose,
                    enabled = medication.stockCount > 0
                ) {
                    Text("Take 1 Dose")
                }
            }
        }
    }
}
