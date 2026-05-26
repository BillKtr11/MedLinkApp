package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.layout.*
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
        }
    }
}