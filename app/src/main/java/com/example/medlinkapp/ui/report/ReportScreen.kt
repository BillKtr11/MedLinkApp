package com.example.medlinkapp.ui.report

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medlinkapp.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBackClick:()->Unit,
    viewModel: reportViewModel = viewModel()
){
    val uiState by viewModel.uiState.collectAsState(
    val context = LocalContext.current
    val currentStep = when(uiState){
        is ReportUiState.Idle -> ReportStep.PATIENT_SEARCH
        is ReportUiState.Loading ->{
            ReportStep.PATIENT_SEARCH
        }
        is ReportUiState.PatientFound -> ReportStep.DATE_SELECTION
        is ReportUiState.Success -> ReportStep.REPORT_READY
        is ReportUiState.Error ->(uiState as ReportUiState.Error).step
    }
    Scaffold(
        topBar ={
            TopAppBar(
                title ={Text("Print Health Report",FontWeight = FontWeight.Bold)},
                navigationIcon ={
                    IconButton(onClick={
                        if(currentStep == ReportStep.PATIENT_SEARCH){
                            onBackClick()
                        }else{
                            val prevStep = if(currentStep == ReportStep.REPORT_READY){
                                ReportStep.DATE_SELECTION
                            }else{
                                ReportStep.PATIENT_SEARCH
                            }
                            viewmodel.resetToStep(prevStep)
                        }
                    }){
                        Icon(Icons.Default.ArrowBack,contentDescription="Back")
                    }
                },
                colors = TopAppBarDefaults.TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ){paddingValues ->
    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.2f),
                    MaterialTheme.colorScheme.background
                )
            )
        )
        .padding(16.dp) 
    ){
        StepIndicator(currentStep = currentStep)
        Spacer(modifier = modifier.height(20.dp))
        AnimatedContent(
            targetState = uiState,
            transitionSpec ={
                fadeIn() togetherWith fadeOut()
            },
            label = "ReportFlowTransition"
        ){state ->
        when(state){
            is ReportUiState.Idle ->{
                PatientSearchSection(onSearch ={id->viewModel.searchPatient(id)})
            }
            is ReportUiState.Loading ->{
                Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }
            is ReportUiState.PatientFound ->{
                DateSelectionSection(
                    patient = state.patient,
                    onGenerate = {start,end -> viewModel.generateReport(start,end)}
                )
            }
            is ReportUiState.Success -> {
                ReportReadySection(
                    report = state.report,
                    onExportPdf ={
                        Toast.makeText(context,"the pdf is saved!",Toast.LENGTH_LONG).show()
                    },
                    onNewSearch = {viewModel,resetToStep(ReportStep.PATIENT_SEARCH)}
                )
            }
            is ReportUiState.Error ->{
                ErrorSection(
                    message = state.message,
                    step = state.step,
                    onRetry ={
                        viewModel.resetToStep(state.step)
                        }
                    )
                }
            }
        }
        }
        }
    )
}


//UI_COMPONENTS


@Composable
fun StepIndicator(currentStep:ReportStep){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ){
        StepNode(stepNumber =1,label="Search",isActive = currentStep == ReportStep.PATIENT_SEARCH)
        Divider(modifier = Modifier.width(30.dp),color=Color.Gray)
        StepNode(stepNumber =2,label="Space",isActive = currentStep == ReportStep.DATE_SELECTION)
        Divider(modifier = Modifier.width(30.dp),color=Color.Gray)
        StepNode(stepNumber =3,label="Report",isActive = currentStep == ReportStep.REPORT_READY)
    }
}

fun StepNode(stepNumber:Int,label:String,isActive:Boolean){
    column(horizontalAlignment = Alignment.centerHorizontally){
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if(isActive)MaterialTheme.colorScheme.primary else Color.LightGray
                ),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = stepNumber.toString(),
                color = if(isActive)Color.White else Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label,fontSize =11.sp,fontWeight = if(isActive)FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PatientSearchSection(onSearch:(String)->Unit){
    var patientId by remember{mutableStateOf("")}
    card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ){
        Column(modifier = Modifier.padding(24.dp)){
            Text(
                text = "Patient Search",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = fontWeight.Bold
            )
            Spacer(modifier = Modifier.heigh(8.dp))
            Text(
                text ="Insert patient ID to retrieve patient details and information",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {if(patientId.isNotBlank())onSearch(patientId.trim())},
                enabled = patientid.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(Icons.Default.Search,contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search")
            }
        }
    }
}

@Composable
fun DateSelectionSection(
    patient:Patient,
    onGenerate:(LocalDateTime,LocalDateTime)->Unit
){
    var startDateStr by remember{mutableStateOf(LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))}
    var endDateStr by remember{mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))}
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ){
        Column(modifier = Modifier.padding(20.dp)){
            Text(
                text = "Patient Information",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(text="Name:",fontWeight.Bold)
                Text(text = patient.name)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(text = "date of birth:",fontWeight=FontWeight.Bold)
                Text(text = patient.dateOfBirth)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Medical History:",fontWeight=FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top=4.dp)
            ){
                patient.medicalHistory.forEach{history->
                Box(
                    modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal=8.dp,vertical=4.dp)
                ){
                    Text(text = history,fontSize = 11.sp,color=MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Choose time interval",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = startDateStr,
                onValueChange = {startDateStr=it},
                label ={Text("Start date(YYYY-MM-DD)")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = endDateStr,
                onValueChange ={endDateStr=it},
                label ={Text("End date(YYYY-MM-DD)")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                TextButton(
                    onClick = {
                        startDateStr = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    },
                    modifier = Modifier.weight(1f)
                ){
                    Text("Last 7 days",fontSize 11.sp)
                }
                TextButton(
                    onClick = {
                        startDateStr = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    },
                    modifier = Modifier.weight(1f)
                ){
                    Text("Last month",fontSize 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick={
                    val start = parseDate(startDateStr,false)
                    val end = parseDate(endDateStr,true)
                    onGenerate(start,end)
                },
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(Icons.Default.CheckCircle,contentDescription=null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Creation!")
            }
        }
    }
}

@Composable