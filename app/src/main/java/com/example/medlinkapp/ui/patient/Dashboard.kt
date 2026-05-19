package com.example.medlinkapp.ui.patient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    patientName: String = "Alex Johnson",
    onNavigateToMedications: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onTriggerSOS: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, $patientName") },
                actions = {
                    // Emergency SOS Button (ManageEmergencySOS from your diagram)
                    Button(
                        onClick = onTriggerSOS,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SOS")
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

            // 1. Vitals Summary (Links to DeviceManager / ScreenManager)
            item {
                VitalsCard()
            }

            // 2. Upcoming Appointments (Links to AppointInformationScreen)
            item {
                DashboardActionCard(
                    title = "Upcoming Appointments",
                    icon = Icons.Default.DateRange,
                    summaryText = "Dental Clean - June 1st, 9:00 AM",
                    onClick = onNavigateToAppointments
                )
            }

            // 3. Active Medications (Links to ActiveDrugsScreen)
            item {
                DashboardActionCard(
                    title = "My Medications",
                    icon = Icons.Default.Info, // Use Medication icon if available in extended icons
                    summaryText = "3 Active Prescriptions (Next: Lisinopril at 8 AM)",
                    onClick = onNavigateToMedications
                )
            }

            // 4. Test Results (Links to HealthReport)
            item {
                DashboardActionCard(
                    title = "Test Results & Records",
                    icon = Icons.Default.List,
                    summaryText = "New result available: Lipid Panel",
                    onClick = onNavigateToResults
                )
            }

            // 5. Messages (Links to Message)
            item {
                DashboardActionCard(
                    title = "Messages",
                    icon = Icons.Default.Email,
                    summaryText = "2 Unread Messages from Dr. Lee",
                    onClick = onNavigateToMessages
                )
            }
        }
    }
}

// --- Reusable UI Components ---

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