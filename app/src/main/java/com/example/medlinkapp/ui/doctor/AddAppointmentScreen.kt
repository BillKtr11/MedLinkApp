package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.example.medlinkapp.model.UserData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    viewModel: DoctorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    var selectedPatient by remember { mutableStateOf<UserData?>(null) }
    var showPatientPicker by remember { mutableStateOf(false) }
    val myPatients by viewModel.myPatients.collectAsState()

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Προσθήκη Ραντεβού") },
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
                // Patient Selection (Limited to assigned patients)
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPatientPicker = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedPatient == null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedPatient == null) "Επιλογή Ασθενή" else "Ασθενής: ${selectedPatient?.name} ${selectedPatient?.surname}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (selectedPatient != null) {
                                Text(text = "AMKA: ${selectedPatient?.amka}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        TextButton(onClick = { showPatientPicker = true }) {
                            Text("Αλλαγή")
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it; showError = false },
                    label = { Text("Ημερομηνία (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && date.isBlank()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it; showError = false },
                    label = { Text("Ώρα (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && time.isBlank()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it; showError = false },
                    label = { Text("Αιτία επίσκεψης") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && reason.isBlank()
                )

                if (showError) {
                    Text(
                        text = errorMessage.ifEmpty { "Παρακαλώ συμπληρώστε όλα τα πεδία." },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (date.isBlank() || time.isBlank() || reason.isBlank() || selectedPatient == null) {
                            showError = true
                            errorMessage = if (selectedPatient == null) "Παρακαλώ επιλέξτε ασθενή από την λίστα σας." else "Όλα τα πεδία είναι υποχρεωτικά."
                        } else {
                            try {
                                val d = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                val t = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                                val dateTime = LocalDateTime.of(d, t)

                                val result = viewModel.addAppointment(dateTime, reason, selectedPatient!!.amka)
                                if (result.isSuccess) {
                                    showSuccess = true
                                } else {
                                    showError = true
                                    errorMessage = result.exceptionOrNull()?.message ?: "Σφάλμα κατά την αποθήκευση."
                                }
                            } catch (e: Exception) {
                                showError = true
                                errorMessage = "Μη έγκυρη μορφή ημερομηνίας ή ώρας."
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Οι υπενθυμίσεις (24ω και 1ω πριν) έχουν προγραμματιστεί.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateHome) {
                            Text("Επιστροφή στην Αρχική")
                        }
                    }
                }
            }
        }
    }

    if (showPatientPicker) {
        AlertDialog(
            onDismissRequest = { showPatientPicker = false },
            title = { Text("Επιλογή Ασθενή (Δικοί σας Ασθενείς)") },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (myPatients.isEmpty()) {
                        Text("Δεν έχετε ακόμη συνδεδεμένους ασθενείς. Χρησιμοποιήστε την 'Σύνδεση Ασθενή' από το μενού.")
                    } else {
                        LazyColumn {
                            items(myPatients) { patient ->
                                ListItem(
                                    headlineContent = { Text("${patient.name} ${patient.surname}") },
                                    supportingContent = { Text("AMKA: ${patient.amka}") },
                                    modifier = Modifier.clickable {
                                        selectedPatient = patient
                                        showPatientPicker = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPatientPicker = false }) {
                    Text("Κλείσιμο")
                }
            }
        )
    }
}