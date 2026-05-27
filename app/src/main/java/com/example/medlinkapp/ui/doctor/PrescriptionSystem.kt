package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    // ΒΗΜΑ 1: State για τα στοιχεία της συνταγής
    var medication by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Νέα Συνταγή") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Πίσω")
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
                .verticalScroll(rememberScrollState()) // Για να μην κρύβεται πίσω από το πληκτρολόγιο
        ) {
            Text(
                text = "Συνταγογράφηση για: ${patient?.name ?: "Άγνωστος"}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Πεδία Εισαγωγής (ΒΗΜΑ 1)
            OutlinedTextField(
                value = medication,
                onValueChange = { medication = it },
                label = { Text("Φάρμακο (e.g. Amoxil)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Δοσολογία (e.g. 500mg)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text("Συχνότητα (e.g. 3 φορές την ημέρα)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Διάρκεια (e.g. 7 ημέρες)") },
                modifier = Modifier.fillMaxWidth()
            )

            // ΒΗΜΑ 2: Εμφάνιση σφάλματος αν αποτύχει το Validation
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ΒΗΜΑ 3: Επιβεβαίωση ενέργειας (Κουμπί Enter / Αποθήκευση)
            Button(
                onClick = {
                    val patientAmka = patient?.amka ?: return@Button

                    // Εκτέλεση ελέγχου και αποθήκευσης
                    val resultError = viewModel.issuePrescription(
                        patientId = patientAmka,
                        medication = medication,
                        dosage = dosage,
                        frequency = frequency,
                        duration = duration
                    )

                    if (resultError != null) {
                        errorMessage = resultError // Εμφάνιση του λάθους στο UI
                    } else {
                        errorMessage = null
                        showSuccessDialog = true // Εμφάνιση επιτυχίας (Βήματα 4, 5, 6)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Επιβεβαίωση & Έκδοση (Enter)")
            }
        }
    }

    // Dialog Επιβεβαίωσης Επιτυχίας
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Δεν επιτρέπουμε κλείσιμο χωρίς πάτημα */ },
            title = { Text("Επιτυχής Καταχώριση") },
            text = {
                Text("Η συνταγή αποθηκεύτηκε επιτυχώς!\n\n• Το πρόγραμμα του ασθενή ενημερώθηκε αυτόματα.\n• Η ειδοποίηση στάλθηκε στη συσκευή του.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick() // Επιστροφή στο ιστορικό
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}