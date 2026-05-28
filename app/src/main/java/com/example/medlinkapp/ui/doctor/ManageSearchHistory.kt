package com.example.medlinkapp.ui.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DateTimeException
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSearchHistory(
    viewModel: DoctorViewModel,
    onBackClick: () -> Unit,
    onNavigateToPrescription: () -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val history by viewModel.patientHistory.collectAsState()

    var isFilterExpanded by remember { mutableStateOf(false) }

    // State για τις ημερομηνίες
    val currentDate = LocalDate.now()
    var fromDay by remember { mutableStateOf(currentDate.dayOfMonth) }
    var fromMonth by remember { mutableStateOf(currentDate.monthValue) }
    var fromYear by remember { mutableStateOf(currentDate.year - 1) }

    var toDay by remember { mutableStateOf(currentDate.dayOfMonth) }
    var toMonth by remember { mutableStateOf(currentDate.monthValue) }
    var toYear by remember { mutableStateOf(currentDate.year) }

    // Μήνυμα λάθους
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            // ΔΙΟΡΘΩΣΗ: Προσθήκη και ενεργοποίηση του TopAppBar με το βέλος "Πίσω"
            TopAppBar(
                title = { Text("Medical History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            Button(
                onClick = onNavigateToPrescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Issue Prescription")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Στοιχεία Ασθενή
            patient?.let {
                Text("Patient: ${it.name}", style = MaterialTheme.typography.headlineSmall)
                Text("ΑΜΚΑ: ${it.amka}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Button(
                        onClick = { isFilterExpanded = !isFilterExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isFilterExpanded) "Hide Filters" else "Date Filter")
                    }

                    if (isFilterExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))

                        DateRow(
                            label = "FROM:",
                            day = fromDay, month = fromMonth, year = fromYear,
                            onDayChange = { fromDay = it },
                            onMonthChange = { fromMonth = it },
                            onYearChange = { fromYear = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DateRow(
                            label = "TO:",
                            day = toDay, month = toMonth, year = toYear,
                            onDayChange = { toDay = it },
                            onMonthChange = { toMonth = it },
                            onYearChange = { toYear = it }
                        )

                        // Μηνύματος Λάθους
                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Enter
                        Button(
                            onClick = {
                                try {
                                    // Έλεγχος εγκυρότητας ημερομηνιών
                                    val startDate = LocalDate.of(fromYear, fromMonth, fromDay)
                                    val endDate = LocalDate.of(toYear, toMonth, toDay)

                                    if (startDate.isAfter(endDate)) {
                                        errorMessage = "Error: 'From' date must be before 'To' date."
                                    } else {
                                        errorMessage = null
                                        viewModel.filterHistoryByDate(startDate, endDate)
                                        isFilterExpanded = false
                                    }
                                } catch (e: DateTimeException) {
                                    errorMessage = "Invalid date! Please check if the days correspond correctly to the month (e.g., February doesn't have 30 days)."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply (Enter)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

@Composable
fun DateRow(
    label: String,
    day: Int, month: Int, year: Int,
    onDayChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    val days = (1..31).toList()
    val months = (1..12).toList()
    val years = (2000..2026).toList()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, modifier = Modifier.width(45.dp), style = MaterialTheme.typography.labelLarge)

        Spacer(modifier = Modifier.width(8.dp))

        SimpleDropdown(items = days, selectedItem = day, onItemSelected = onDayChange, modifier = Modifier.weight(1f))
        Text(" / ", modifier = Modifier.padding(horizontal = 4.dp))
        SimpleDropdown(items = months, selectedItem = month, onItemSelected = onMonthChange, modifier = Modifier.weight(1f))
        Text(" / ", modifier = Modifier.padding(horizontal = 4.dp))
        SimpleDropdown(items = years, selectedItem = year, onItemSelected = onYearChange, modifier = Modifier.weight(1.2f))
    }
}

@Composable
fun SimpleDropdown(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(selectedItem.toString().padStart(2, '0'))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString().padStart(2, '0')) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}