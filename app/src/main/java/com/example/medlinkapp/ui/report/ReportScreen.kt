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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.medlinkapp.data.DBManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBackClick:()->Unit,
    viewModel: ReportViewModel = viewModel()
){
    val uiState by viewModel.uiState.collectAsState(ReportUiState.Idle)
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
                            viewModel.resetToStep(prevStep)
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
        Spacer(modifier = Modifier.height(20.dp))
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
                    onNewSearch = {viewModel.resetToStep(ReportStep.PATIENT_SEARCH)}
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
        StepNode(stepNumber =2,label="Date Selection",isActive = currentStep == ReportStep.DATE_SELECTION)
        Divider(modifier = Modifier.width(30.dp),color=Color.Gray)
        StepNode(stepNumber =3,label="Report",isActive = currentStep == ReportStep.REPORT_READY)
    }
}

@Composable
fun StepNode(stepNumber:Int,label:String,isActive:Boolean){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ){
        Column(modifier = Modifier.padding(24.dp)){
            Text(
                text = "Patient Search",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text ="Insert patient ID to retrieve patient details and information",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = patientId,
                onValueChange = {patientId=it},
                label ={Text("Patient ID (AMKA)")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {if(patientId.isNotBlank())onSearch(patientId.trim())},
                enabled = patientId.isNotBlank(),
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
                Text(text="Name:",fontWeight = FontWeight.Bold)
                Text(text = patient.name)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(text = "Date of birth:",fontWeight=FontWeight.Bold)
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
                    Text("Last 7 days",fontSize = 11.sp)
                }
                TextButton(
                    onClick = {
                        startDateStr = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    },
                    modifier = Modifier.weight(1f)
                ){
                    Text("Last month",fontSize = 11.sp)
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
fun ReportReadySection(
    report: Report,
    onExportPdf:()->Unit,
    onNewSearch:()->Unit
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ){
        LazyColumn(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){
            item {
                Row(verticalAlignment = Alignment.CenterVertically){
                    Icon(Icons.Default.CheckCircle,contentDescription=null,tint=Color.Green)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Ready",fontWeight = FontWeight.Bold,style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
            }
            item {
                Text(text="Patient Details",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text="Name: ${report.patient.name}")
                Text(text="AMKA: ${report.patient.patientId}")
                Text(text="Period: ${report.startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))} to ${report.endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
            }
            item {
                Divider()
                Spacer(modifier = Modifier.height(4.dp))
                Text(text="Device Measurements",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
            }
            if(report.measurements.isEmpty()){
                item {
                    Text(text="No measurements in this period",color=Color.Gray,fontSize=13.sp)
                }
            }else{
                items(report.measurements){m->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp,Color.LightGray,RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column {
                            Text(text=m.measurementType,fontWeight=FontWeight.Bold)
                            Text(text=m.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),fontSize=11.sp,color=Color.Gray)
                        }
                        Text(text="${m.measurementValue}",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                Divider()
                Spacer(modifier = Modifier.height(4.dp))
                Text(text="Medications",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
            }
            if(report.medications.isEmpty()){
                item {
                    Text(text="No medications assigned",color=Color.Gray,fontSize=13.sp)
                }
            }else{
                items(report.medications){med->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.2f),RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column {
                            Text(text=med.name,fontWeight=FontWeight.Bold)
                            Text(text="Dosage: ${med.dosage}",fontSize=11.sp,color=Color.Gray)
                        }
                        Text(text="Stock: ${med.stockCount}",fontWeight=FontWeight.Bold)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick=onExportPdf,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Icon(Icons.Default.Share,contentDescription=null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export PDF")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick=onNewSearch,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Icon(Icons.Default.Refresh,contentDescription=null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Search")
                }
            }
        }
    }
}

@Composable
fun ErrorSection(
    message:String,
    step:ReportStep,
    onRetry:()->Unit
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ){
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Icon(Icons.Default.Warning,contentDescription=null,tint=MaterialTheme.colorScheme.error,modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text="Error",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text=message,textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick=onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ){
                Icon(Icons.Default.Refresh,contentDescription=null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}


//HELPERS


fun parseDate(dateStr:String,isEndOfDay:Boolean):LocalDateTime{
    return try {
        val date = java.time.LocalDate.parse(dateStr,DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if(isEndOfDay) date.atTime(23,59,59) else date.atStartOfDay()
    }catch(e:Exception){
        if(isEndOfDay) LocalDateTime.now() else LocalDateTime.now().minusDays(7)
    }
}


//DATA_MODELS


enum class ReportStep {
    PATIENT_SEARCH,
    DATE_SELECTION,
    REPORT_READY
}

data class Report(
    val patient:Patient,
    val startDate:LocalDateTime,
    val endDate:LocalDateTime,
    val measurements:List<DeviceData>,
    val medications:List<MedicationData>,
    val generatedAt:LocalDateTime = LocalDateTime.now()
)

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    data class PatientFound(val patient:Patient) : ReportUiState()
    data class Success(val report:Report) : ReportUiState()
    data class Error(val message:String,val step:ReportStep) : ReportUiState()
}


//VIEWMODEL


class ReportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState:StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var selectedPatient:Patient? = null

    fun searchPatient(patientId:String){
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading
            delay(1000)
            val user = DBManager.users.value.find { it.amka == patientId && it.role == UserRole.PATIENT }
            if(user != null){
                val historyResult = DBManager.searchPatientHistory(patientId)
                val medicalHistory = historyResult.getOrDefault(emptyList())
                val patient = Patient(
                    patientId = user.amka,
                    name = "${user.name} ${user.surname}",
                    dateOfBirth = "1990-05-15",
                    medicalHistory = medicalHistory
                )
                selectedPatient = patient
                _uiState.value = ReportUiState.PatientFound(patient)
            }else if(patientId == "12345678901" || patientId == "09876543210" || patientId == "000000"){
                val mockName = when(patientId){
                    "12345678901" -> "Γιώργος Παπαδόπουλος"
                    "09876543210" -> "Μαρία Νικολάου"
                    else -> "Demo Patient"
                }
                val patient = Patient(
                    patientId = patientId,
                    name = mockName,
                    dateOfBirth = "1990-05-15",
                    medicalHistory = listOf("Υπέρταση","Σακχαρώδης Διαβήτης")
                )
                selectedPatient = patient
                _uiState.value = ReportUiState.PatientFound(patient)
            }else{
                _uiState.value = ReportUiState.Error("Patient with AMKA $patientId not found",ReportStep.PATIENT_SEARCH)
            }
        }
    }

    fun generateReport(startDate:LocalDateTime,endDate:LocalDateTime){
        val patient = selectedPatient
        if(patient == null){
            _uiState.value = ReportUiState.Error("No patient selected",ReportStep.PATIENT_SEARCH)
            return
        }
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading
            delay(1000)
            try {
                val measurements = DBManager.getMeasurementsForUser(patient.patientId).filter {
                    !it.timestamp.isBefore(startDate) && !it.timestamp.isAfter(endDate)
                }
                val medications = DBManager.getMedicationsForUser(patient.patientId)
                val report = Report(
                    patient = patient,
                    startDate = startDate,
                    endDate = endDate,
                    measurements = measurements,
                    medications = medications
                )
                _uiState.value = ReportUiState.Success(report)
            }catch(e:Exception){
                _uiState.value = ReportUiState.Error("Failed to generate report: ${e.localizedMessage}",ReportStep.DATE_SELECTION)
            }
        }
    }

    fun resetToStep(step:ReportStep){
        when(step){
            ReportStep.PATIENT_SEARCH -> {
                selectedPatient = null
                _uiState.value = ReportUiState.Idle
            }
            ReportStep.DATE_SELECTION -> {
                val patient = selectedPatient
                if(patient != null){
                    _uiState.value = ReportUiState.PatientFound(patient)
                }else{
                    _uiState.value = ReportUiState.Idle
                }
            }
            ReportStep.REPORT_READY -> {}
        }
    }
}