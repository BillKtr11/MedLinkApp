package com.example.medlinkapp.ui.measurement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMeasurementRecording(
    viewModel: MeasurementViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val measurementTypes = listOf("Πίεση", "Σάκχαρο", "Βάρος", "Οξυγόνο")
    var selectedType by remember { mutableStateOf(measurementTypes[0]) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val methods = listOf("Χειροκίνητη εισαγωγή", "Σύνδεση συσκευής (Bluetooth)")
    var selectedMethod by remember { mutableStateOf(methods[0]) }
    var measurementValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Νέα Μέτρηση") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onNavigateHome()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToHistory) {
                        Text("Ιστορικό", color = MaterialTheme.colorScheme.primary)
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
            Text("Επιλέξτε Τύπο Μέτρησης", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    measurementTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                isDropdownExpanded = false
                                measurementValue = ""
                                viewModel.resetState()
                            }
                        )
                    }
                }
            }

            Text("Τρόπος Εισαγωγής", style = MaterialTheme.typography.titleMedium)
            Column {
                methods.forEach { method ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (method == selectedMethod),
                            onClick = {
                                selectedMethod = method
                                measurementValue = ""
                                viewModel.resetState()
                            }
                        )
                        Text(method)
                    }
                }
            }

            if (selectedMethod == "Σύνδεση συσκευής (Bluetooth)") {
                Button(
                    onClick = {
                        viewModel.fetchBluetoothData(selectedType) { fetchedValue ->
                            measurementValue = fetchedValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Αναζήτηση Συσκευής & Λήψη Δεδομένων")
                }
            }

            OutlinedTextField(
                value = measurementValue,
                onValueChange = { measurementValue = it },
                label = { Text("Τιμή Μέτρησης (π.χ. 90 mg/dL)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMethod != "Σύνδεση συσκευής (Bluetooth)"
            )

            when (val state = uiState) {
                is MeasurementState.Error -> {
                    Text(
                        text = state.message,
                        color = if (state.isOutOfBounds) MaterialTheme.colorScheme.error else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is MeasurementState.Success -> {
                    Text(
                        text = state.message,
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            measurementValue = ""
                            viewModel.resetState()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Νέα Καταχώρηση")
                    }
                }
                is MeasurementState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                else -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState !is MeasurementState.Success) {
                Button(
                    onClick = { viewModel.submitMeasurement(selectedType, measurementValue, selectedMethod) },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Καταγραφή Μέτρησης")
                }
            }
        }
    }
}