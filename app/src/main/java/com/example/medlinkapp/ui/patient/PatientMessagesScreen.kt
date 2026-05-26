package com.example.medlinkapp.ui.patient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medlinkapp.data.DBManager
import com.example.medlinkapp.model.Message
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMessagesScreen(
    onBackClick: () -> Unit
) {
    val messages by DBManager.messages.collectAsState()
    val userAmka = DBManager.getCurrentUserAmka()
    
    val myMessages = messages.filter { it.patientAmka == userAmka }.sortedByDescending { it.timestamp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Μηνύματα") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (myMessages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Δεν έχετε νέα μηνύματα.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myMessages) { message ->
                    MessageItem(
                        message = message,
                        onClick = { DBManager.markMessageAsRead(message.id) },
                        onDelete = { DBManager.deleteMessage(message.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (message.isRead) CardDefaults.cardColors() else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = message.title, style = MaterialTheme.typography.titleMedium, fontWeight = if (message.isRead) FontWeight.Normal else FontWeight.Bold)
                    Text(
                        text = message.timestamp.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Message", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}