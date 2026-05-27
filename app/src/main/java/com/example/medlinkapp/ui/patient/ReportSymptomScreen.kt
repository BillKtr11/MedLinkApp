package com.example.medlinkapp.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medlinkapp.data.MedicationData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSymptomScreen(
    viewModel: SideEffectViewModel,
    onNavigateBack: () -> Unit,
) {
    val medications by viewModel.medications.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionSuccess by viewModel.submissionSuccess.collectAsState(initial = null)

    var selectedMedication by remember { mutableStateOf<MedicationData?>(null) }
    var symptom by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var intensity by remember { mutableFloatStateOf(3f) }
    var expanded by remember { mutableStateOf(value = false) }

    LaunchedEffect(submissionSuccess) {
        if (submissionSuccess == true) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Side Effect") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = selectedMedication?.name ?: "Select Medication",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Medication") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    medications.forEach { medication: MedicationData ->
                        DropdownMenuItem(
                            text = { Text(medication.name) },
                            onClick = {
                                selectedMedication = medication
                                expanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = symptom,
                onValueChange = {
                    if (it.length <= 500) {
                        symptom = it
                    }
                },
                label = { Text("Describe the Symptom/Side Effect") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                supportingText = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${symptom.length} / 500",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.CenterEnd),
                        )
                    }
                },
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (e.g., 2 hours, 3 days)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text("Intensity: ${intensity.toInt()}")
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    valueRange = 1f..10f,
                    steps = 8,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedMedication?.let {
                        viewModel.submitReport(
                            medicationId = it.id,
                            medicationName = it.name,
                            symptom = symptom,
                            duration = duration,
                            intensity = intensity.toInt(),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (selectedMedication != null) && symptom.isNotBlank() && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Submit Report")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportSymptomScreenPreview() {
    MaterialTheme {
        ReportSymptomScreen(
            viewModel = viewModel(),
        ) { }
    }
}
