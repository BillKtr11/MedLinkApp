package com.example.medlinkapp.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugRegistrationManager(
    viewModel: MedicationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    
    // Intake Times
    val intakeTimes = remember { mutableStateListOf<String>() }

    var showError by remember { mutableStateOf(false) }
    var showStockWarning by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var stockExhaustionDate by remember { mutableStateOf("") }

    // Update intake times when frequency changes
    LaunchedEffect(frequency) {
        val freqInt = frequency.toIntOrNull() ?: 0
        intakeTimes.clear()
        if (freqInt > 0) {
            val interval = 24 / freqInt
            for (i in 0 until freqInt) {
                val hour = (8 + i * interval) % 24 // Start at 8 AM
                intakeTimes.add(String.format("%02d:00", hour))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Προσθήκη Φαρμάκου") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!showSuccess) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("Όνομα Φαρμάκου") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && name.isBlank()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it; showError = false },
                    label = { Text("Δοσολογία (π.χ. 500mg)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && dosage.isBlank()
                )

                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it; showError = false },
                    label = { Text("Συχνότητα (λήψεις ανά ημέρα)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && frequency.toIntOrNull() == null
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it; showError = false },
                    label = { Text("Διάρκεια (ημέρες)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && duration.toIntOrNull() == null
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it; showError = false },
                    label = { Text("Τρέχον Απόθεμα (τεμάχια)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && stock.toIntOrNull() == null
                )

                if (intakeTimes.isNotEmpty()) {
                    Text("Ώρες Λήψης:", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intakeTimes.forEachIndexed { index, time ->
                            var currentTime by remember { mutableStateOf(time) }
                            OutlinedTextField(
                                value = currentTime,
                                onValueChange = { 
                                    currentTime = it
                                    intakeTimes[index] = it 
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (showError) {
                    Text(
                        text = "Παρακαλώ συμπληρώστε όλα τα πεδία σωστά.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val freqInt = frequency.toIntOrNull()
                        val durInt = duration.toIntOrNull()
                        val stockInt = stock.toIntOrNull()

                        if (name.isBlank() || dosage.isBlank() || freqInt == null || durInt == null || stockInt == null) {
                            showError = true
                        } else {
                            val totalNeeded = freqInt * durInt
                            if (stockInt < totalNeeded) {
                                val daysAvailable = stockInt / freqInt
                                val exhaustionDate = LocalDate.now().plusDays(daysAvailable.toLong())
                                stockExhaustionDate = exhaustionDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                showStockWarning = true
                            } else {
                                viewModel.addMedication(name, dosage, stockInt, freqInt, intakeTimes.toList())
                                showSuccess = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Επιβεβαίωση Καταχώρησης")
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Επιτυχής Καταχώρηση!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateHome) {
                            Text("Επιστροφή στην Αρχική")
                        }
                    }
                }
            }
        }

        if (showStockWarning) {
            AlertDialog(
                onDismissRequest = { showStockWarning = false },
                title = { Text("Χαμηλό Απόθεμα") },
                text = {
                    Text("Το απόθεμα θα εξαντληθεί στις $stockExhaustionDate. Θέλετε υπενθύμιση για αγορά φαρμάκου 2 ημέρες πριν;")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showStockWarning = false
                        viewModel.addMedication(name, dosage, stock.toInt(), frequency.toInt(), intakeTimes.toList())
                        showSuccess = true
                    }) {
                        Text("Ναι")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showStockWarning = false
                        viewModel.addMedication(name, dosage, stock.toInt(), frequency.toInt(), intakeTimes.toList())
                        showSuccess = true
                    }) {
                        Text("Όχι")
                    }
                }
            )
        }
    }
}
