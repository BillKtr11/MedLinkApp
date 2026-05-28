package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medlinkapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScreen(
    doctorName: String = "Dr. Smith",
    viewModel: DoctorViewModel = viewModel(),
    onNavigateToSearch: () -> Unit,
    onNavigateToAddAppointment: () -> Unit,
    onNavigateToRegisterPatient: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToPrescriptionHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val myPatients by viewModel.myPatients.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val alerts by viewModel.activeAlerts.collectAsState()
    val pendingAlerts = alerts.filter { it.status == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.medlink),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Welcome, $doctorName")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SOS ALERT SECTION
            if (pendingAlerts.isNotEmpty()) {
                val latestAlert = pendingAlerts.last()
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Red),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("EMERGENCY SOS", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Patient: ${latestAlert.patientName}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Type: ${latestAlert.measurementType}", color = Color.White)
                            Text("Value: ${latestAlert.value}", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.respondToAlert(latestAlert.id, "Instructions sent by doctor") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Respond & Clear", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                DoctorStatsCard(
                    patientCount = myPatients.size,
                    appointmentCount = appointments.size
                )
            }

            item {
                DoctorActionCard(
                    title = "Add Appointment",
                    icon = Icons.Default.DateRange,
                    summaryText = "Schedule a new visit for your patients",
                    onClick = onNavigateToAddAppointment
                )
            }

            item {
                DoctorActionCard(
                    title = "Appointment Schedule",
                    icon = Icons.Default.List,
                    summaryText = "View all your scheduled appointments",
                    onClick = onNavigateToAppointments
                )
            }

            item {
                DoctorActionCard(
                    title = "Connect Patient",
                    icon = Icons.Default.Person,
                    summaryText = "Add an existing user to your patient list",
                    onClick = onNavigateToRegisterPatient
                )
            }

            item {
                DoctorActionCard(
                    title = "Patient Search",
                    icon = Icons.Default.Search,
                    summaryText = "Search history for your registered patients",
                    onClick = onNavigateToSearch
                )
            }

            item {
                DoctorActionCard(
                    title = "Prescription History",
                    icon = Icons.Default.List,
                    summaryText = "View all prescriptions you have issued",
                    onClick = onNavigateToPrescriptionHistory
                )
            }

            item {
                DoctorActionCard(
                    title = "Health Report",
                    icon = Icons.Default.Info,
                    summaryText = "Create a health report (PDF) for a specific patient and time period",
                    onClick = onNavigateToReport
                )
            }

            item {
                DoctorActionCard(
                    title = "Messages",
                    icon = Icons.Default.Email,
                    summaryText = "Communicate with your patients",
                    onClick = onNavigateToMessages
                )
            }
        }
    }
}

@Composable
fun DoctorStatsCard(patientCount: Int, appointmentCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Appointments: $appointmentCount")
                Text("Assigned Patients: $patientCount")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Updated just now", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun DoctorActionCard(
    title: String,
    icon: ImageVector,
    summaryText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = summaryText, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}
