package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.medlinkapp.model.User
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentAdditionScreen(
    viewModel: DoctorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var reason by remember { mutableStateOf("") }
    
    var selectedPatient by remember { mutableStateOf<User?>(null) }
    var showPatientPicker by remember { mutableStateOf(false) }
    val myPatients by viewModel.myPatients.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

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
                // Patient Selection
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

                // Date Selection
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedDate == null) "Επιλογή Ημερομηνίας" else "Ημερομηνία: ${selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }
                }

                // Time Selection
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTime == null) "Επιλογή Ώρας" else "Ώρα: ${selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Notifications, contentDescription = null) // Using notifications icon for time
                    }
                }

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
                        if (selectedDate == null || selectedTime == null || reason.isBlank() || selectedPatient == null) {
                            showError = true
                            errorMessage = when {
                                selectedPatient == null -> "Παρακαλώ επιλέξτε ασθενή."
                                selectedDate == null -> "Παρακαλώ επιλέξτε ημερομηνία."
                                selectedTime == null -> "Παρακαλώ επιλέξτε ώρα."
                                else -> "Όλα τα πεδία είναι υποχρεωτικά."
                            }
                        } else {
                            val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                            val result = viewModel.addAppointment(dateTime, reason, selectedPatient!!.amka)
                            if (result.isSuccess) {
                                showSuccess = true
                            } else {
                                showError = true
                                errorMessage = result.exceptionOrNull()?.message ?: "Σφάλμα κατά την αποθήκευση."
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

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    // Patient Picker Dialog
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
