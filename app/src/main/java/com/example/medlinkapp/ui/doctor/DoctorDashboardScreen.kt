package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medlinkapp.data.DBManager

@Composable
fun DoctorDashboardScreen(
    dbManager:DBManager,
    onNavigateToSearch:()->Unit,
    onLogout:()->Unit
){
    val alerts by dbManager.activeAlerts.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Κεντρικό Μενού Γιατρού",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SOS ALERT SECTION
        if(alerts.isNotEmpty()){
            val latestAlert = alerts.last()
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red),
                elevation = CardDefaults.cardElevation(8.dp)
            ){
                Column(modifier = Modifier.padding(16.dp)){
                    Text("EMERGENCY SOS",fontWeight = FontWeight.Bold,color = Color.White,style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Patient: ${latestAlert.patientName}",color = Color.White,fontWeight = FontWeight.Bold)
                    Text("Type: ${latestAlert.measurementType}",color = Color.White)
                    Text("Value: ${latestAlert.value}",color = Color.White,style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick ={dbManager.respondToAlert(latestAlert.id,"Instructions sent by doctor")},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text("Respond & Clear",color = Color.Red,fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Επιλογή: Αναζήτηση Ασθενή
        Button(
            onClick = onNavigateToSearch,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Αναζήτηση Ασθενή / Ιστορικού")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Κουμπι για μελλοντική χρηση
        OutlinedButton(
            onClick = { /* TODO: Πλοήγηση στα ραντεβού */ },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("coming soon")
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Κουμπί logout
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Αποσύνδεση (Logout)", color = MaterialTheme.colorScheme.onError)
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medlinkapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    doctorName: String = "Dr. Smith",
    viewModel: DoctorViewModel = viewModel(),
    onNavigateToSearch: () -> Unit,
    onNavigateToAddAppointment: () -> Unit,
    onNavigateToRegisterPatient: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onLogout: () -> Unit
) {
    val myPatients by viewModel.myPatients.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

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
            item {
                DoctorStatsCard(
                    patientCount = myPatients.size,
                    appointmentCount = appointments.size
                )
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
                    title = "Πρόγραμμα Ραντεβού",
                    icon = Icons.Default.List,
                    summaryText = "Προβολή όλων των προγραμματισμένων ραντεβού σας",
                    onClick = onNavigateToAppointments
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