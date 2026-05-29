package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medlinkapp.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDoctorClient(
    viewModel: DoctorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allPatients by viewModel.allPatients.collectAsState()
    
    
    val filteredPatients = allPatients.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.surname.contains(searchQuery, ignoreCase = true) || 
        it.amka.contains(searchQuery)
    }

    var selectedPatient by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Patient") },
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
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Patient (Name or AMKA)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select patient to register in your list:", style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredPatients) { patient ->
                    ListItem(
                        headlineContent = { Text("${patient.name} ${patient.surname}") },
                        supportingContent = { Text("AMKA: ${patient.amka}") },
                        trailingContent = {
                            RadioButton(
                                selected = selectedPatient?.amka == patient.amka,
                                onClick = { selectedPatient = patient }
                            )
                        },
                        modifier = Modifier.clickable { selectedPatient = patient }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedPatient?.let {
                        viewModel.assignPatient(it.amka)
                        onNavigateHome()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = selectedPatient != null
            ) {
                Text("Confirm Registration")
            }
        }
    }
}
