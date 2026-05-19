package com.example.medlinkapp.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MedicationManagerScreen(
    viewModel: MedicationViewModel = viewModel(),
    onBackClick: () -> Boolean
) {
    val medications by viewModel.medications.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Medications", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(medications) { med ->
                MedicationCard(
                    medication = med,
                    onTakeDose = { viewModel.takeDose(med.id) },
                    onRestock = { viewModel.restock(med.id, 30) } // Default refill of 30
                )
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
    // Change card color slightly if low stock
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

                // Stock Badge
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

            // Low Stock Warning Alert
            if (medication.isLowStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Low Stock", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refill required soon. Threshold: ${medication.lowStockThreshold}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
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
                    enabled = medication.stockCount > 0 // Disable if out of stock
                ) {
                    Text("Take 1 Dose")
                }
            }
        }
    }
}