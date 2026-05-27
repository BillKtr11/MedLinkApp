package com.example.medlinkapp.ui.patient

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medlinkapp.R
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.ui.medication.MedicationViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    patientName: String = "Alex Johnson",
    medViewModel: MedicationViewModel = viewModel(),
    onNavigateToMedications: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToNewMeasurement: () -> Unit,
    onNavigateToTakeMedication: (String) -> Unit,
    onTriggerSOS: () -> Unit,
    onLogout: () -> Unit
) {
    val medications by medViewModel.medications.collectAsState()
    val appointments by DBManager.appointments.collectAsState()
    val messages by DBManager.messages.collectAsState()
    val userAmka = DBManager.getCurrentUserAmka()
    
    val myAppointments = appointments.filter { it.patientId == userAmka }.sortedBy { it.date }
    val myMessages = messages.filter { it.patientAmka == userAmka }
    val unreadMessagesCount = myMessages.count { !it.isRead }

    // Find the next medication to take
    val nextMed = medications.filter { it.intakeTimes.isNotEmpty() }
        .mapNotNull { med -> 
            med.getNextIntakeTime()?.let { time -> med to time }
        }
        .minByOrNull { it.second }

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
                        Text("Welcome, $patientName")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Test Notification logic simulation: open intake screen for the next med
                        if (nextMed != null) {
                            onNavigateToTakeMedication(nextMed.first.id)
                        }
                    }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Test Notification", tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = onTriggerSOS,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SOS")
                    }
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

            item {
                VitalsCard()
            }


            item {
                DashboardActionCard(
                    title = "New Measurement",
                    icon = Icons.Default.Add,
                    summaryText = "Record Blood Pressure, Glucose, Weight, etc.",
                    onClick = onNavigateToNewMeasurement
                )
            }

            item {
                val nextAppt = myAppointments.firstOrNull()
                val apptText = if (nextAppt != null) {
                    "Next: ${nextAppt.date.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))} with ${nextAppt.doctorName}"
                } else {
                    "No upcoming appointments"
                }
                DashboardActionCard(
                    title = "Upcoming Appointments",
                    icon = Icons.Default.DateRange,
                    summaryText = apptText,
                    onClick = onNavigateToAppointments
                )
            }

            item {
                val medText = if (nextMed != null) {
                    "Next: ${nextMed.first.name} at ${nextMed.second}"
                } else {
                    "No active prescriptions"
                }
                DashboardActionCard(
                    title = "My Medications",
                    icon = Icons.Default.Info,
                    summaryText = "$medText (${medications.size} Total)",
                    onClick = onNavigateToMedications
                )
            }

            item {
                DashboardActionCard(
                    title = "Test Results & Records",
                    icon = Icons.Default.List,
                    summaryText = "New result available: Lipid Panel",
                    onClick = onNavigateToResults
                )
            }


            item {
                val msgText = if (unreadMessagesCount > 0) "$unreadMessagesCount unread messages" else "No new messages"
                DashboardActionCard(
                    title = "Messages",
                    icon = Icons.Default.Email,
                    summaryText = msgText,
                    onClick = onNavigateToMessages
                )
            }
        }
    }
}


@Composable
fun VitalsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Latest Vitals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("BP: 118/74 mmHg")
                Text("HR: 68 bpm")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Updated 5 mins ago", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun DashboardActionCard(
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