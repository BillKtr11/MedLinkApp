package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHistoryScreen(
    viewModel: DoctorViewModel,
    onBackClick: () -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val history by viewModel.patientHistory.collectAsState()

    // Για απλότητα φιλτράρουμε τον τελευταίο 1 χρόνο
    val startDate = LocalDate.now().minusYears(1)
    val endDate = LocalDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ιατρικό Ιστορικό") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Επιστροφή"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        // Τα paddingValues που μας δίνει το Scaffold για να μην κρύβεται περιεχόμενο
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            patient?.let {
                Text("Ασθενής: ${it.name}", style = MaterialTheme.typography.headlineSmall)
                Text("ΑΜΚΑ: ${it.amka}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Κουμπί Φίλτρου
            Button(
                onClick = { viewModel.filterHistoryByDate(startDate, endDate) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Φιλτράρισμα (Τελευταίο Έτος)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Εμφάνιση Φιλτραρισμένου
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(history) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = record.date.toString(), style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = record.type,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = record.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}