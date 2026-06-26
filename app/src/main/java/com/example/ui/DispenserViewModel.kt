package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ComplianceLog
import com.example.data.DispenserDatabase
import com.example.data.DispenserRepository
import com.example.data.DispenserSlot
import com.example.data.gemini.GeminiPrescriptionScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class UserRole {
    PATIENT, DOCTOR, FAMILY_MEMBER
}

class DispenserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DispenserDatabase.getDatabase(application, viewModelScope)
    private val repository = DispenserRepository(database.dispenserDao())

    val allSlots: StateFlow<List<DispenserSlot>> = repository.allSlots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<ComplianceLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User Role
    private val _userRole = MutableStateFlow(UserRole.PATIENT)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    fun switchRole(role: UserRole) {
        _userRole.value = role
    }

    // Camera Scan States
    private val _scanLoading = MutableStateFlow(false)
    val scanLoading: StateFlow<Boolean> = _scanLoading.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private val _scannedSuccessfully = MutableStateFlow<Boolean>(false)
    val scannedSuccessfully: StateFlow<Boolean> = _scannedSuccessfully.asStateFlow()

    // Doctor view patient search
    private val _patientSearchId = MutableStateFlow("P-48902")
    val patientSearchId: StateFlow<String> = _patientSearchId.asStateFlow()

    fun updatePatientSearchId(id: String) {
        _patientSearchId.value = id
    }

    // Family tracking settings
    private val _familyNotificationsEnabled = MutableStateFlow(true)
    val familyNotificationsEnabled: StateFlow<Boolean> = _familyNotificationsEnabled.asStateFlow()

    fun toggleFamilyNotifications(enabled: Boolean) {
        _familyNotificationsEnabled.value = enabled
    }

    private val _alerts = MutableStateFlow<List<String>>(
        listOf(
            "Alert: Metformin successfully dispensed at 08:10 AM",
            "Alert: Lisinopril successfully dispensed at 08:10 AM",
            "🚨 Critical Alert: Atorvastatin dose missed yesterday!"
        )
    )
    val alerts: StateFlow<List<String>> = _alerts.asStateFlow()

    fun addAlert(alert: String) {
        _alerts.value = listOf(alert) + _alerts.value
    }

    // Action to manually update slot values
    fun updateSlot(slotNumber: Int, medicineName: String, scheduledTime: String, isActive: Boolean, pillCount: Int, dosage: String) {
        viewModelScope.launch {
            val updatedSlot = DispenserSlot(
                slotNumber = slotNumber,
                medicineName = medicineName,
                scheduledTime = scheduledTime,
                isActive = isActive,
                pillCount = pillCount,
                dosage = dosage
            )
            repository.updateSlot(updatedSlot)
        }
    }

    // Trigger Physical limit switch dispense
    fun triggerDispense(slotNumber: Int) {
        viewModelScope.launch {
            val slot = repository.getSlotByNumber(slotNumber) ?: return@launch
            if (!slot.isActive) {
                addAlert("Cannot dispense: Slot $slotNumber is not active.")
                return@launch
            }
            if (slot.pillCount <= 0) {
                addAlert("Cannot dispense ${slot.medicineName}: Slot $slotNumber is empty.")
                return@launch
            }

            // Decrement pill count
            val updatedSlot = slot.copy(pillCount = slot.pillCount - 1)
            repository.updateSlot(updatedSlot)

            // Record compliance log
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = sdf.format(Date(now))
            val log = ComplianceLog(
                timestamp = now,
                slotNumber = slotNumber,
                medicineName = slot.medicineName,
                status = "TAKEN",
                actualTime = formattedTime
            )
            repository.insertLog(log)

            // Push notification
            val alertMsg = "Alert: Dispensed ${slot.medicineName} from Slot $slotNumber at $formattedTime"
            addAlert(alertMsg)
        }
    }

    // Trigger physical missed dose event
    fun triggerMissedDose(slotNumber: Int) {
        viewModelScope.launch {
            val slot = repository.getSlotByNumber(slotNumber) ?: return@launch
            if (!slot.isActive) return@launch

            val now = System.currentTimeMillis()
            val log = ComplianceLog(
                timestamp = now,
                slotNumber = slotNumber,
                medicineName = slot.medicineName,
                status = "MISSED",
                actualTime = "Missed"
            )
            repository.insertLog(log)

            val alertMsg = "🚨 Critical Alert: Missed dose of ${slot.medicineName} in Slot $slotNumber!"
            addAlert(alertMsg)
        }
    }

    // Call Gemini API to scan prescription image or run preset simulation
    fun scanPrescription(bitmap: Bitmap?, presetIndex: Int = -1) {
        viewModelScope.launch {
            _scanLoading.value = true
            _scanError.value = null
            _scannedSuccessfully.value = false

            try {
                val result = if (presetIndex >= 0) {
                    // Simulate
                    kotlinx.coroutines.delay(1200)
                    GeminiPrescriptionScanner.getSimulatedResult(presetIndex)
                } else if (bitmap != null) {
                    GeminiPrescriptionScanner.scanPrescriptionImage(bitmap)
                } else {
                    null
                }

                if (result != null && result.medications.isNotEmpty()) {
                    // Automatically allot slots and sync to database!
                    for (med in result.medications) {
                        val existing = repository.getSlotByNumber(med.slot)
                        val slot = DispenserSlot(
                            slotNumber = med.slot,
                            medicineName = med.name,
                            scheduledTime = med.time,
                            isActive = true,
                            pillCount = existing?.pillCount?.coerceAtLeast(1) ?: 15,
                            dosage = med.dosage
                        )
                        repository.insertSlot(slot)
                    }
                    _scannedSuccessfully.value = true
                } else {
                    _scanError.value = "Failed to extract prescription. Please try again."
                }
            } catch (e: Exception) {
                _scanError.value = "Error during scan: ${e.message}"
            } finally {
                _scanLoading.value = false
            }
        }
    }

    fun dismissScanSuccess() {
        _scannedSuccessfully.value = false
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}

class DispenserViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DispenserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DispenserViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
