package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComplianceLog
import com.example.data.DispenserSlot
import com.example.data.gemini.GeminiPrescriptionScanner
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartDispenserApp(viewModel: DispenserViewModel) {
    val userRole by viewModel.userRole.collectAsState()
    val allSlots by viewModel.allSlots.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()

    var currentTab by remember { mutableStateOf("dashboard") }

    // Synchronize clock every second
    var liveTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            liveTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val liveClockString = remember(liveTimeMillis) {
        SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(liveTimeMillis))
    }

    val liveDateString = remember(liveTimeMillis) {
        SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date(liveTimeMillis))
    }

    // Next dose computation
    val nextDoseInfo = remember(allSlots, liveTimeMillis) {
        getNextDoseText(allSlots, liveTimeMillis)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF)),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x80FFFFFF))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "App Logo",
                                tint = Color(0xFF001F28),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MedDispense IoT",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001F28)
                            )
                        }
                        Text(
                            text = "6-Compartment Smart Servo Dispenser",
                            fontSize = 11.sp,
                            color = Color(0xFF41484D).copy(alpha = 0.7f)
                        )
                    }

                    // Role Switcher Chips
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFE1E2EC), RoundedCornerShape(20.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = userRole == role
                            val text = when (role) {
                                UserRole.PATIENT -> "Patient"
                                UserRole.DOCTOR -> "Doctor"
                                UserRole.FAMILY_MEMBER -> "Family"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF001C38) else Color.Transparent)
                                    .clickable {
                                        viewModel.switchRole(role)
                                        // Reset tab if it is doctor-only or family-only
                                        if (role == UserRole.PATIENT) {
                                            if (currentTab == "doctor" || currentTab == "family") {
                                                currentTab = "dashboard"
                                            }
                                        } else if (role == UserRole.DOCTOR) {
                                            currentTab = "doctor"
                                        } else {
                                            currentTab = "family"
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = text,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF41484D)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            val navColors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF001C38),
                unselectedIconColor = Color(0xFF44474E).copy(alpha = 0.6f),
                selectedTextColor = Color(0xFF001C38),
                unselectedTextColor = Color(0xFF44474E).copy(alpha = 0.6f),
                indicatorColor = Color(0xFFD3E4FF)
            )
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = Color(0xFFF1F4F9),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "dashboard",
                    onClick = { currentTab = "dashboard" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_dashboard")
                )

                NavigationBarItem(
                    selected = currentTab == "slots",
                    onClick = { currentTab = "slots" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Slots") },
                    label = { Text("Slots", fontSize = 11.sp) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_slots")
                )

                NavigationBarItem(
                    selected = currentTab == "scanner",
                    onClick = { currentTab = "scanner" },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Scanner") },
                    label = { Text("AI Scan", fontSize = 11.sp) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_scanner")
                )

                NavigationBarItem(
                    selected = currentTab == "history",
                    onClick = { currentTab = "history" },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_history")
                )

                if (userRole == UserRole.DOCTOR) {
                    NavigationBarItem(
                        selected = currentTab == "doctor",
                        onClick = { currentTab = "doctor" },
                        icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Doctor") },
                        label = { Text("Doctor", fontSize = 11.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_doctor")
                    )
                }

                if (userRole == UserRole.FAMILY_MEMBER) {
                    NavigationBarItem(
                        selected = currentTab == "family",
                        onClick = { currentTab = "family" },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Family") },
                        label = { Text("Family", fontSize = 11.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_family")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF))
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "dashboard" -> PatientDashboardScreen(
                    allSlots = allSlots,
                    liveClockString = liveClockString,
                    liveDateString = liveDateString,
                    nextDoseInfo = nextDoseInfo,
                    onDispenseTrigger = { slotNum -> viewModel.triggerDispense(slotNum) },
                    onMissedTrigger = { slotNum -> viewModel.triggerMissedDose(slotNum) }
                )
                "slots" -> SlotManagerScreen(
                    allSlots = allSlots,
                    onUpdateSlot = { num, name, time, active, pills, dosage ->
                        viewModel.updateSlot(num, name, time, active, pills, dosage)
                    }
                )
                "scanner" -> AIScannerScreen(
                    viewModel = viewModel,
                    onAllot = { index -> viewModel.scanPrescription(null, presetIndex = index) }
                )
                "history" -> ComplianceHistoryScreen(
                    allLogs = allLogs,
                    onClearLogs = { viewModel.clearAllLogs() }
                )
                "doctor" -> DoctorPortalScreen(
                    viewModel = viewModel,
                    onPushPrescription = { slot, name, time, dose ->
                        viewModel.updateSlot(slot, name, time, true, 10, dose)
                        viewModel.addAlert("Doctor pushed Virtual Prescription: $name $dose directly to Slot $slot")
                    }
                )
                "family" -> FamilyTrackingScreen(
                    viewModel = viewModel,
                    allLogs = allLogs
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PATIENT DASHBOARD SCREEN
// -----------------------------------------------------------------------------
@Composable
fun PatientDashboardScreen(
    allSlots: List<DispenserSlot>,
    liveClockString: String,
    liveDateString: String,
    nextDoseInfo: Pair<String, String>,
    onDispenseTrigger: (Int) -> Unit,
    onMissedTrigger: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Live Clock & Countdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD3E4FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE DISPENSER SYSTEM CLOCK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001C38),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = liveClockString,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF001C38),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = liveDateString,
                        fontSize = 12.sp,
                        color = Color(0xFF001C38).copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF001C38).copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "COUNTDOWN TO NEXT SCHEDULED DOSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001C38),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextDoseInfo.second,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF001C38),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextDoseInfo.first,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF001C38),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Physical Compartments (6 Servo Slots)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF41484D)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE1E2EC), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "IoT SYNCED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E)
                    )
                }
            }
        }

        // 6 Slots Cards
        items(allSlots.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { slot ->
                    SlotDashboardCard(
                        slot = slot,
                        modifier = Modifier.weight(1f),
                        onDispenseTrigger = { onDispenseTrigger(slot.slotNumber) },
                        onMissedTrigger = { onMissedTrigger(slot.slotNumber) }
                    )
                }
                if (pair.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SlotDashboardCard(
    slot: DispenserSlot,
    modifier: Modifier = Modifier,
    onDispenseTrigger: () -> Unit,
    onMissedTrigger: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFC1C7CE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SLOT ${slot.slotNumber}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006492)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (slot.isActive) Color.Green else Color(0xFFCBD5E1), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (slot.isActive && slot.medicineName.isNotBlank()) {
                Text(
                    text = slot.medicineName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1E),
                    maxLines = 1
                )
                Text(
                    text = "${slot.dosage} • ${slot.scheduledTime}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pill level indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pills Left: ${slot.pillCount}",
                        fontSize = 10.sp,
                        color = if (slot.pillCount < 3) Color(0xFFE11D48) else Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Progress Bar of Remaining Pills
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                ) {
                    val progress = (slot.pillCount / 20f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                if (slot.pillCount < 3) Color(0xFFE11D48) else Color(0xFF4ADE80),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Physical trigger simulator buttons
                Button(
                    onClick = onDispenseTrigger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .testTag("dispense_btn_${slot.slotNumber}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F28)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Trigger Dispense",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trigger Switch", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onMissedTrigger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    border = BorderDefaults.outlinedBorder,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Simulate Missed", fontSize = 9.sp)
                }

            } else {
                Text(
                    text = "Empty Slot",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF191C1E).copy(alpha = 0.4f)
                )
                Text(
                    text = "Configure in Settings",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }
}

// Border helper
object BorderDefaults {
    val outlinedBorder = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.4f))
}

// -----------------------------------------------------------------------------
// SLOT MANAGER SCREEN
// -----------------------------------------------------------------------------
@Composable
fun SlotManagerScreen(
    allSlots: List<DispenserSlot>,
    onUpdateSlot: (Int, String, String, Boolean, Int, String) -> Unit
) {
    var editingSlotNum by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Manual Slot & Medication Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E)
            )
            Text(
                text = "Calibrate medication parameters for each individual servo slot.",
                fontSize = 12.sp,
                color = Color(0xFF41484D)
            )
        }

        items(allSlots) { slot ->
            val isEditing = editingSlotNum == slot.slotNumber

            var nameInput by remember(slot) { mutableStateOf(slot.medicineName) }
            var timeInput by remember(slot) { mutableStateOf(slot.scheduledTime) }
            var dosageInput by remember(slot) { mutableStateOf(slot.dosage) }
            var activeState by remember(slot) { mutableStateOf(slot.isActive) }
            var pillsCountInput by remember(slot) { mutableStateOf(slot.pillCount.toString()) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFFD3E4FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S${slot.slotNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF001C38)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (slot.medicineName.isBlank()) "Unconfigured Compartment" else slot.medicineName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191C1E)
                                )
                                Text(
                                    text = "Schedule: ${slot.scheduledTime} • Refill level: ${slot.pillCount} pills",
                                    fontSize = 12.sp,
                                    color = Color(0xFF41484D)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                editingSlotNum = if (isEditing) null else slot.slotNumber
                            }
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                                contentDescription = "Edit Slot",
                                tint = if (isEditing) Color(0xFF4ADE80) else Color(0xFF001F28)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isEditing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFE1E2EC))
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Compartment Active State", color = Color(0xFF41484D), fontSize = 13.sp)
                                Switch(
                                    checked = activeState,
                                    onCheckedChange = { activeState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF001F28)
                                    )
                                )
                            }

                            val textFieldColors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF001F28),
                                unfocusedBorderColor = Color(0xFFC1C7CE),
                                focusedLabelColor = Color(0xFF001F28),
                                unfocusedLabelColor = Color(0xFF41484D),
                                focusedTextColor = Color(0xFF191C1E),
                                unfocusedTextColor = Color(0xFF191C1E)
                            )

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Medication Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("edit_name_${slot.slotNumber}"),
                                colors = textFieldColors
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dosageInput,
                                    onValueChange = { dosageInput = it },
                                    label = { Text("Dosage") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = textFieldColors
                                )

                                OutlinedTextField(
                                    value = timeInput,
                                    onValueChange = { timeInput = it },
                                    label = { Text("Time (HH:MM)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("edit_time_${slot.slotNumber}"),
                                    colors = textFieldColors
                                )
                            }

                            OutlinedTextField(
                                value = pillsCountInput,
                                onValueChange = { pillsCountInput = it },
                                label = { Text("Pills Refilled") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = textFieldColors
                            )

                            Button(
                                onClick = {
                                    val pills = pillsCountInput.toIntOrNull() ?: 0
                                    onUpdateSlot(
                                        slot.slotNumber,
                                        nameInput,
                                        timeInput,
                                        activeState,
                                        pills,
                                        dosageInput
                                    )
                                    editingSlotNum = null
                                },
                                modifier = Modifier.fillMaxWidth().testTag("save_slot_${slot.slotNumber}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F28))
                            ) {
                                Text("Save Compartment Settings")
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// AI PRESCRIPTION SCANNER SCREEN
// -----------------------------------------------------------------------------
@Composable
fun AIScannerScreen(
    viewModel: DispenserViewModel,
    onAllot: (Int) -> Unit
) {
    val scanLoading by viewModel.scanLoading.collectAsState()
    val scanError by viewModel.scanError.collectAsState()
    val scannedSuccessfully by viewModel.scannedSuccessfully.collectAsState()

    var showPromptDetails by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "AI Vision Prescription Reader",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E)
            )
            Text(
                text = "Use Gemini-3.5-Flash to analyze medication paper or drug label text, automatically alloting dispenser times.",
                fontSize = 12.sp,
                color = Color(0xFF41484D)
            )
        }

        // Camera View Finder Mockup
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFF001F28).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background visual camera scanning effect
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color(0xFFF1F4F9),
                        size = size
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera viewfinder",
                        tint = Color(0xFF001F28).copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ALIGN PHARMACY RX LABEL HERE",
                        color = Color(0xFF191C1E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Or choose a preset medication card below to simulate",
                        color = Color(0xFF41484D),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Scanning Beam overlay when loading
                if (scanLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF6C5CE7), Color(0xFF00B894))
                                )
                            )
                            .drawBehind {
                                // Draw simple glowing overlay
                            }
                    )
                    CircularProgressIndicator(
                        color = Color(0xFF6C5CE7)
                    )
                }
            }
        }

        // Action Trigger buttons
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Simulate Rx Prescription Presets",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAllot(0) },
                        modifier = Modifier.weight(1f).testTag("scan_preset_1"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3A)),
                        enabled = !scanLoading
                    ) {
                        Text("Card A (Metformin)", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { onAllot(1) },
                        modifier = Modifier.weight(1f).testTag("scan_preset_2"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3A)),
                        enabled = !scanLoading
                    ) {
                        Text("Card B (Aspirin)", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { onAllot(2) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3A)),
                        enabled = !scanLoading
                    ) {
                        Text("Card C (Lisinopril)", fontSize = 11.sp, color = Color.White)
                    }
                }

                Button(
                    onClick = { viewModel.scanPrescription(null, 0) },
                    modifier = Modifier.fillMaxWidth().testTag("scan_real_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                    enabled = !scanLoading
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger AI Prescription Scanning")
                }
            }
        }

        // Output results of parsing
        item {
            AnimatedVisibility(visible = scannedSuccessfully) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2C24)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF00B894)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI Scan Successful!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            "Medications extracted correctly and mapped directly to IoT Slots! Check dashboard.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Button(
                            onClick = { viewModel.dismissScanSuccess() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))
                        ) {
                            Text("Acknowledge Sync", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(visible = scanError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1B1B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFFF7675)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Scanning Status Note",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = scanError ?: "",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Developer Prompt and API Structure Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI API Prompt & JSON Architecture",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (showPromptDetails) "Hide" else "Show Details",
                            fontSize = 11.sp,
                            color = Color(0xFF6C5CE7),
                            modifier = Modifier.clickable { showPromptDetails = !showPromptDetails }
                        )
                    }

                    if (showPromptDetails) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "SYSTEM PROMPT INJECTED:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0C0C12), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = GeminiPrescriptionScanner.SYSTEM_PROMPT,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "EXPECTED JSON OUTPUT STRUCTURE:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0C0C12), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = """{
  "medications": [
    {
      "name": "Metformin",
      "dosage": "500mg",
      "time": "08:00",
      "slot": 1
    }
  ]
}""",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF00B894)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPLIANCE HISTORY SCREEN
// -----------------------------------------------------------------------------
@Composable
fun ComplianceHistoryScreen(
    allLogs: List<ComplianceLog>,
    onClearLogs: () -> Unit
) {
    // Math indicators
    val takenCount = allLogs.count { it.status == "TAKEN" }
    val missedCount = allLogs.count { it.status == "MISSED" }
    val totalCount = takenCount + missedCount
    val complianceRate = if (totalCount > 0) {
        (takenCount.toFloat() / totalCount.toFloat() * 100).toInt()
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Patient Compliance Tracker",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1E)
                    )
                    Text(
                        text = "Aggregated data from dispenser limit switch microswitches.",
                        fontSize = 12.sp,
                        color = Color(0xFF41484D)
                    )
                }
                IconButton(onClick = onClearLogs) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear logs", tint = Color(0xFF41484D))
                }
            }
        }

        // Compliance Ratio Metric Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Draw Canvas compliance rate dial
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0xFFE1E2EC),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx())
                            )
                            drawArc(
                                color = Color(0xFF00B894),
                                startAngle = -90f,
                                sweepAngle = (complianceRate * 3.6f),
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx())
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$complianceRate%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF191C1E)
                            )
                            Text(
                                text = "Rate",
                                fontSize = 10.sp,
                                color = Color(0xFF41484D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column {
                        Text(
                            text = "COMPLIANCE STATS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001F28)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Successful doses: $takenCount",
                            fontSize = 14.sp,
                            color = Color(0xFF191C1E),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Missed doses: $missedCount",
                            fontSize = 14.sp,
                            color = Color(0xFF41484D),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (complianceRate >= 80) Color(0xFFD1FAE5) else Color(0xFFFFE4E6),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (complianceRate >= 80) "EXCELLENT PATIENT CARE" else "ATTENTION NEEDED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (complianceRate >= 80) Color(0xFF047857) else Color(0xFFBE123C)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Limit Switch Activity Logs",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E)
            )
        }

        if (allLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No compliance history found. Trigger some limit switch events on the dashboard!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // List of Logs
        items(allLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (log.status == "TAKEN") Color(0xFFD1FAE5) else Color(0xFFFFE4E6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (log.status == "TAKEN") Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = log.status,
                                tint = if (log.status == "TAKEN") Color(0xFF047857) else Color(0xFFBE123C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = log.medicineName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191C1E)
                            )
                            Text(
                                text = "Slot ${log.slotNumber} • Limit Switch microtrigger",
                                fontSize = 11.sp,
                                color = Color(0xFF41484D)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = log.status,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.status == "TAKEN") Color(0xFF047857) else Color(0xFFBE123C)
                        )
                        Text(
                            text = log.actualTime,
                            fontSize = 11.sp,
                            color = Color(0xFF41484D).copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DOCTOR PORTAL (RBAC FEATURE)
// -----------------------------------------------------------------------------
@Composable
fun DoctorPortalScreen(
    viewModel: DispenserViewModel,
    onPushPrescription: (Int, String, String, String) -> Unit
) {
    val patientSearchId by viewModel.patientSearchId.collectAsState()

    var docMedicineName by remember { mutableStateOf("") }
    var docDosage by remember { mutableStateOf("") }
    var docSlot by remember { mutableStateOf("1") }
    var docTime by remember { mutableStateOf("08:00") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Doctor Portal (Over-The-Air Prescribing)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00B894)
            )
            Text(
                text = "Virtual medical consultation panel. Search patients and directly configure dispenser hardware schedules.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Search Patient Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "QUERY DATABASE FOR DISPENSER REGISTRATION ID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B894)
                    )

                    OutlinedTextField(
                        value = patientSearchId,
                        onValueChange = { viewModel.updatePatientSearchId(it) },
                        label = { Text("Enter Patient ID (e.g., P-48902)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("doc_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00B894),
                            unfocusedBorderColor = Color(0xFF2E2E3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (patientSearchId == "P-48902") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C221F))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF00B894), CircleShape)
                                )
                                Column {
                                    Text(
                                        "Dhanush Beru (ID: P-48902)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        "Dispenser Model: SD-6V1 • Online Sync Active",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No custom patient matched. Showing demo registry for $patientSearchId.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Virtual Prescription Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "OTA VIRTUAL PRESCRIPTION FORM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B894)
                    )

                    OutlinedTextField(
                        value = docMedicineName,
                        onValueChange = { docMedicineName = it },
                        label = { Text("Medication Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("doc_med_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00B894),
                            unfocusedBorderColor = Color(0xFF2E2E3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = docDosage,
                            onValueChange = { docDosage = it },
                            label = { Text("Dosage") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B894),
                                unfocusedBorderColor = Color(0xFF2E2E3A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = docTime,
                            onValueChange = { docTime = it },
                            label = { Text("Time (HH:MM)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("doc_time_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B894),
                                unfocusedBorderColor = Color(0xFF2E2E3A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = docSlot,
                        onValueChange = { docSlot = it },
                        label = { Text("Target Dispenser Slot (1-6)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("doc_slot_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00B894),
                            unfocusedBorderColor = Color(0xFF2E2E3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Button(
                        onClick = {
                            val slotNum = docSlot.toIntOrNull() ?: 1
                            onPushPrescription(slotNum, docMedicineName, docTime, docDosage)
                            docMedicineName = ""
                            docDosage = ""
                        },
                        modifier = Modifier.fillMaxWidth().testTag("doc_push_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))
                    ) {
                        Text("Transmit OTA Prescription Schedule")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// FAMILY ALERTS & TRACKING (RBAC FEATURE)
// -----------------------------------------------------------------------------
@Composable
fun FamilyTrackingScreen(
    viewModel: DispenserViewModel,
    allLogs: List<ComplianceLog>
) {
    val familyNotificationsEnabled by viewModel.familyNotificationsEnabled.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Family Compliance & Push Alerts Dashboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDCB6E)
            )
            Text(
                text = "View-only compliance portal for designated caretakers with push alert simulations.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Alert Config Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mobile Phone Push Alerts",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Send immediate warning notifications if a dosage microtrigger is missed.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }

                    Switch(
                        checked = familyNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleFamilyNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFDCB6E)
                        )
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Simulated Alert Stream",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFFFDCB6E)
                )
            }
        }

        if (!familyNotificationsEnabled) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Alert Stream is muted.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(alerts) { alert ->
                val isCritical = alert.contains("🚨") || alert.contains("missed") || alert.contains("Cannot")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCritical) Color(0xFF2C1E1C) else Color(0xFF1C221F)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isCritical) Color(0xFFFF7675) else Color(0xFF00B894),
                                    CircleShape
                                )
                        )
                        Text(
                            text = alert,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Compliance Log (Read-Only)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Read only list of logs
        items(allLogs.take(5)) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15151F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = log.medicineName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Slot ${log.slotNumber} • Limit switch verified",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = log.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.status == "TAKEN") Color(0xFF00B894) else Color(0xFFFF7675)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TIMER HELPERS
// -----------------------------------------------------------------------------
fun getNextDoseText(slots: List<DispenserSlot>, currentTimeMillis: Long): Pair<String, String> {
    val activeSlots = slots.filter { it.isActive && it.medicineName.isNotBlank() }
    if (activeSlots.isEmpty()) {
        return Pair("No active medications configured", "00:00:00")
    }

    val currentFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTimeMillis))
    val currentParts = currentFormatted.split(":")
    if (currentParts.size != 3) {
        return Pair("No active medications configured", "00:00:00")
    }
    val currentSecsOfDay = currentParts[0].toInt() * 3600 + currentParts[1].toInt() * 60 + currentParts[2].toInt()

    var minDiffSecs = Int.MAX_VALUE
    var nextSlot: DispenserSlot? = null

    for (slot in activeSlots) {
        val timeParts = slot.scheduledTime.split(":")
        if (timeParts.size != 2) continue
        val slotSecsOfDay = timeParts[0].toInt() * 3600 + timeParts[1].toInt() * 60

        var diffSecs = slotSecsOfDay - currentSecsOfDay
        if (diffSecs < 0) {
            diffSecs += 24 * 3600
        }

        if (diffSecs < minDiffSecs) {
            minDiffSecs = diffSecs
            nextSlot = slot
        }
    }

    if (nextSlot == null) {
        return Pair("No active medications configured", "00:00:00")
    }

    val hours = minDiffSecs / 3600
    val minutes = (minDiffSecs % 3600) / 60
    val seconds = minDiffSecs % 60
    val countdownStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    return Pair("${nextSlot.medicineName} (Slot ${nextSlot.slotNumber}) scheduled for ${nextSlot.scheduledTime}", countdownStr)
}
