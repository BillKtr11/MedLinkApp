package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medlinkapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    doctorName: String = "Dr. Smith",
    onNavigateToSearch: () -> Unit,
    onNavigateToAddAppointment: () -> Unit,
    onNavigateToRegisterPatient: () -> Unit,
    onLogout: () -> Unit
) {
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
                    Button(
                        onClick = { /* SOS simulation */ },
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
            item {
                DoctorStatsCard()
            }

            item {
                DoctorActionCard(
                    title = "Προσθήκη Ραντεβού",
                    icon = Icons.Default.DateRange,
                    summaryText = "Προγραμματισμός νέας επίσκεψη για τους ασθενείς σας",
                    onClick = onNavigateToAddAppointment
                )
            }

            item {
                DoctorActionCard(
                    title = "Σύνδεση Ασθενή",
                    icon = Icons.Default.Person,
                    summaryText = "Προσθήκη υπάρχοντος χρήστη στην λίστα ασθενών σας",
                    onClick = onNavigateToRegisterPatient
                )
            }

            item {
                DoctorActionCard(
                    title = "Αναζήτηση Ασθενή",
                    icon = Icons.Default.Search,
                    summaryText = "Αναζήτηση ιστορικού για τους εγγεγραμμένους ασθενείς σας",
                    onClick = onNavigateToSearch
                )
            }

            item {
                DoctorActionCard(
                    title = "Μηνύματα",
                    icon = Icons.Default.Email,
                    summaryText = "3 Νέα μηνύματα από ασθενείς",
                    onClick = { /* Navigate to messages */ }
                )
            }
        }
    }
}

@Composable
fun DoctorStatsCard() {
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
                Text("Appointments Today: 5")
                Text("Assigned Patients: Live")
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