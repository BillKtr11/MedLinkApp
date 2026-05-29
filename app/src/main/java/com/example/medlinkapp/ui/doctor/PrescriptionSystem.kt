package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionSystem(
    viewModel: DoctorViewModel,
    onBackClick: () -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()

    
    var medication by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Prescription") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) 
        ) {
            Text(
                text = "Prescribing for: ${patient?.name ?: "Unknown"}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            
            OutlinedTextField(
                value = medication,
                onValueChange = { medication = it },
                label = { Text("Medication (e.g. Amoxil)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g. 500mg)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text("Frequency (e.g. 3 times per day)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (e.g. 7 days)") },
                modifier = Modifier.fillMaxWidth()
            )

            
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            
            Button(
                onClick = {
                    val patientAmka = patient?.amka ?: return@Button

                    
                    val resultError = viewModel.issuePrescription(
                        patientId = patientAmka,
                        medication = medication,
                        dosage = dosage,
                        frequency = frequency,
                        duration = duration
                    )

                    if (resultError != null) {
                        errorMessage = resultError 
                    } else {
                        errorMessage = null
                        showSuccessDialog = true 
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Confirm & Issue (Enter)")
            }
        }
    }

    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("Successful Registration") },
            text = {
                Text("The prescription was saved successfully!\n\nâ€¢ The patient's schedule was updated automatically.\nâ€¢ The notification was sent to their device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBackClick() 
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
