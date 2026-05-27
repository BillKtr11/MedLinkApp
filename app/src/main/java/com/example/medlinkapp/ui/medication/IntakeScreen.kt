package com.example.medlinkapp.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakeScreen(
    medId: String,
    viewModel: MedicationViewModel,
    onNavigateBack: () -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val med = medications.find { it.id == medId }

    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Καταγραφή Λήψης") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (med == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Το φάρμακο δεν βρέθηκε")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = med.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Δόση: ${med.dosage}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        viewModel.confirmIntake(med.id)
                        val newStock = med.stockCount - 1
                        if (newStock == 0) {
                            resultMessage = "ΕΠΕΙΓΟΝ: Το απόθεμα είναι 0! Η επόμενη δόση δεν μπορεί να καλυφθεί. Ειδοποίηση στάλθηκε στον φροντιστή."
                            isUrgent = true
                        } else if (newStock <= med.lowStockThreshold) {
                            resultMessage = "Χαμηλό απόθεμα για το ${med.name}. Απομένουν $newStock δόσεις."
                            isUrgent = false
                        } else {
                            resultMessage = "Το απόθεμα του φαρμάκου ${med.name} είναι πλέον $newStock."
                            isUrgent = false
                        }
                        showResultDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Επιβεβαίωση", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* Αναβολή 10' logic would go here */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Αναβολή 10'", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.skipIntake(med.id)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Παράλειψη", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (showResultDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showResultDialog = false
                    onNavigateBack()
                },
                title = { Text(if (isUrgent) "Επείγουσα Ειδοποίηση" else "Ενημέρωση Αποθέματος") },
                text = { Text(resultMessage) },
                confirmButton = {
                    TextButton(onClick = {
                        showResultDialog = false
                        onNavigateBack()
                    }) {
                        Text("ΟΚ")
                    }
                }
            )
        }
    }
}
