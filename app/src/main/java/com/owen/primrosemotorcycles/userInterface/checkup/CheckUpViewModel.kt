package com.owen.primrosemotorcycles.userInterface.checkup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owen.primrosemotorcycles.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckupUiState(
    val bikeReg: String = "",
    val bikeModel: String = "",
    val odometer: String = "",
    val checkedBy: String = "Primrose",
    val items: Map<String, String> = emptyMap(), // itemName -> OK / NOTE / FIX_NOW
    val itemNotes: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val savedId: Int? = null,
    val driverSignature: String = ""
)

@HiltViewModel
class CheckupViewModel @Inject constructor(
    val checkupDao: CheckupDao,
    val jobCardDao: JobCardDao // needed for Fix Now -> Job Card
) : ViewModel() {

    val fleetBikes: StateFlow<List<FleetBike>> = checkupDao.getAllFleetBikes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val existing = checkupDao.getAllFleetBikes().first()
            if (existing.isEmpty()) {
                listOf(
                    FleetBike("AKM-001 - Honda ACE 125", "Honda ACE 125"),
                    FleetBike("AKM-002 - Honda ACE 125", "Honda ACE 125"),
                    FleetBike("AKM-003 - Yamaha Crux", "Yamaha Crux"),
                    FleetBike("AKM-004 - Honda ACE 125", "Honda ACE 125")
                ).forEach { checkupDao.insertFleetBike(it) }
            }
        }
    }

    fun addFleetBike(registration: String, model: String = "") {
        if (registration.isBlank()) return
        viewModelScope.launch {
            checkupDao.insertFleetBike(FleetBike(registration = registration.trim(), model = model.trim()))
        }
    }

    fun deleteFleetBike(registration: String) {
        viewModelScope.launch {
            checkupDao.deleteFleetBikeByReg(registration)
        }
    }

    // All past checkups for middle pane
    val allCheckups: StateFlow<List<WeeklyCheckup>> = checkupDao.getAllCheckups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(CheckupUiState())
    val uiState: StateFlow<CheckupUiState> = _uiState.asStateFlow()

    private val _selectedCheckupItems = MutableStateFlow<List<CheckupItem>>(emptyList())
    val selectedCheckupItems = _selectedCheckupItems.asStateFlow()

    fun startCheckup(bikeReg: String) {
        _uiState.value = CheckupUiState(
            bikeReg = bikeReg,
            items = AckermansChecklist.associateWith { "OK" },
            itemNotes = emptyMap()
        )
    }

    fun setOdometer(v: String) { _uiState.value = _uiState.value.copy(odometer = v) }
    fun setDriverSignature(sig: String) { _uiState.value = _uiState.value.copy(driverSignature = sig) }
    fun setItemStatus(item: String, status: String) {
        val newMap = _uiState.value.items.toMutableMap()
        newMap[item] = status
        _uiState.value = _uiState.value.copy(items = newMap)
    }
    fun setItemNote(item: String, note: String) {
        val newMap = _uiState.value.itemNotes.toMutableMap()
        newMap[item] = note
        _uiState.value = _uiState.value.copy(itemNotes = newMap)
    }

    fun loadCheckup(id: Int) {
        viewModelScope.launch {
            val checkup = checkupDao.getCheckupById(id)
            val items = checkupDao.getItemsSync(id)
            if (checkup != null) {
                _uiState.value = CheckupUiState(
                    bikeReg = checkup.bikeReg,
                    odometer = checkup.odometer,
                    items = items.associate { it.name to it.status },
                    itemNotes = items.associate { it.name to it.note },
                    driverSignature = checkup.driverSignature
                )
                _selectedCheckupItems.value = items
            }
        }
    }

    fun saveCheckup(onSaved: (Int) -> Unit) {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            val overall = when {
                s.items.values.any { it == "FIX_NOW" } -> "NEEDS_REPAIR"
                s.items.values.any { it == "NOTE" } -> "NEEDS_SERVICE"
                else -> "PASS"
            }
            val notesCombined = s.itemNotes.filter { s.items[it.key] != "OK" }
                .entries.joinToString("\n") { "${it.key}: ${it.value}" }

            val id = checkupDao.insertCheckup(
                WeeklyCheckup(
                    bikeReg = s.bikeReg,
                    bikeModel = s.bikeReg,
                    odometer = s.odometer,
                    overallStatus = overall,
                    notes = notesCombined,
                    driverSignature = s.driverSignature
                )
            ).toInt()

            val items = s.items.map { (name, status) ->
                CheckupItem(checkupId = id, name = name, status = status, note = s.itemNotes[name] ?: "")
            }
            checkupDao.insertItems(items)
            _uiState.value = s.copy(isSaving = false, savedId = id)
            onSaved(id)
        }
    }

    // FIX NOW - creates Job Card and returns its ID
    suspend fun createJobCardFromIssue(bikeReg: String, issue: String): String {
        val newId = "JC-${System.currentTimeMillis()}"
        jobCardDao.insert(
            JobCard(
                id = newId,
                bikeReg = bikeReg,
                customerName = "Ackermans Pharmacy",
                customerInstructions = "Weekly Checkup Found: $issue",
                status = JobStatus.OPEN.name
            )
        )
        return newId
    }

    fun clearForm() {
        _uiState.value = CheckupUiState(items = AckermansChecklist.associateWith { "OK" })
    }
}
