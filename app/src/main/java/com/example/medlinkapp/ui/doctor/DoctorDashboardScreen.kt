package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DoctorDashboardScreen(
    doctorName: String = "",
    onNavigateToSearch: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, $doctorName",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

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