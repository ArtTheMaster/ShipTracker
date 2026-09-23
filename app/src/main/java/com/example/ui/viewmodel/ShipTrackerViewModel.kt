package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CourierType
import com.example.data.model.DispatchBatchEntity
import com.example.data.model.ItemCondition
import com.example.data.model.PlatformType
import com.example.data.model.RefundStatus
import com.example.data.model.ReturnReason
import com.example.data.model.ReturnRecordEntity
import com.example.data.model.ShipmentEntity
import com.example.data.model.ShipmentStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.LogisticsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnalyticsSummary(
    val totalShipments: Int = 0,
    val scannedCount: Int = 0,
    val preparedCount: Int = 0,
    val dispatchedCount: Int = 0,
    val inTransitCount: Int = 0,
    val deliveredCount: Int = 0,
    val returnedCount: Int = 0,
    val cancelledCount: Int = 0,
    val totalDeclaredValue: Double = 0.0,
    val returnRatePercent: Double = 0.0,
    val platformCounts: Map<PlatformType, Int> = emptyMap(),
    val courierCounts: Map<CourierType, Int> = emptyMap()
)

class ShipTrackerViewModel(
    private val repository: LogisticsRepository
) : ViewModel() {

    // Current User / RBAC
    private val _currentUser = MutableStateFlow<UserAccount>(UserAccount.DEFAULT_ADMIN)
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    // Offline / Online Connectivity Simulator
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Sync in progress
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Pending sync items count
    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Data streams
    val allShipments: StateFlow<List<ShipmentEntity>> = repository.allShipments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBatches: StateFlow<List<DispatchBatchEntity>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReturns: StateFlow<List<ReturnRecordEntity>> = repository.allReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAuditLogs = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters for Shipments list
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow<ShipmentStatus?>(null)
    val selectedPlatformFilter = MutableStateFlow<PlatformType?>(null)

    // Filtered shipments
    val filteredShipments: StateFlow<List<ShipmentEntity>> = combine(
        allShipments,
        searchQuery,
        selectedStatusFilter,
        selectedPlatformFilter
    ) { list, query, status, platform ->
        list.filter { shipment ->
            val matchesQuery = query.isBlank() ||
                    shipment.trackingNumber.contains(query.trim(), ignoreCase = true) ||
                    shipment.recipientName.contains(query.trim(), ignoreCase = true) ||
                    shipment.recipientPhone.contains(query.trim(), ignoreCase = true) ||
                    shipment.itemsSummary.contains(query.trim(), ignoreCase = true)

            val matchesStatus = status == null || shipment.status == status
            val matchesPlatform = platform == null || shipment.platform == platform

            matchesQuery && matchesStatus && matchesPlatform
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prepared shipments ready for batch dispatch
    val preparedForDispatch: StateFlow<List<ShipmentEntity>> = allShipments.combine(allBatches) { shipments, _ ->
        shipments.filter { it.status == ShipmentStatus.PREPARED && (it.batchId.isNullOrBlank()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analytics summary
    val analytics: StateFlow<AnalyticsSummary> = allShipments.combine(allReturns) { shipments, _ ->
        val total = shipments.size
        val scanned = shipments.count { it.status == ShipmentStatus.SCANNED }
        val prepared = shipments.count { it.status == ShipmentStatus.PREPARED }
        val dispatched = shipments.count { it.status == ShipmentStatus.DISPATCHED }
        val inTransit = shipments.count { it.status == ShipmentStatus.IN_TRANSIT }
        val delivered = shipments.count { it.status == ShipmentStatus.DELIVERED }
        val returned = shipments.count { it.status == ShipmentStatus.RETURNED }
        val cancelled = shipments.count { it.status == ShipmentStatus.CANCELLED }
        val totalValue = shipments.sumOf { it.declaredValue }
        val nonCancelled = total - cancelled
        val returnRate = if (nonCancelled > 0) (returned.toDouble() / nonCancelled) * 100.0 else 0.0

        val platformMap = mutableMapOf<PlatformType, Int>()
        PlatformType.entries.forEach { p ->
            platformMap[p] = shipments.count { it.platform == p }
        }

        val courierMap = mutableMapOf<CourierType, Int>()
        CourierType.entries.forEach { c ->
            courierMap[c] = shipments.count { it.courier == c }
        }

        AnalyticsSummary(
            totalShipments = total,
            scannedCount = scanned,
            preparedCount = prepared,
            dispatchedCount = dispatched,
            inTransitCount = inTransit,
            deliveredCount = delivered,
            returnedCount = returned,
            cancelledCount = cancelled,
            totalDeclaredValue = totalValue,
            returnRatePercent = returnRate,
            platformCounts = platformMap,
            courierCounts = courierMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsSummary())

    // Alerts and UI Events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Dialog States
    val duplicateWarning = MutableStateFlow<ShipmentEntity?>(null)
    val stateTransitionError = MutableStateFlow<String?>(null)
    val selectedShipmentForDetail = MutableStateFlow<ShipmentEntity?>(null)
    val selectedShipmentForStatusChange = MutableStateFlow<ShipmentEntity?>(null)
    val showCreateBatchDialog = MutableStateFlow(false)
    val showLogReturnDialog = MutableStateFlow<ShipmentEntity?>(null)
    val showRoleSwitchDialog = MutableStateFlow(false)

    fun clearDuplicateWarning() {
        duplicateWarning.value = null
    }

    fun clearStateError() {
        stateTransitionError.value = null
    }

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    // Role Switch
    fun switchUser(account: UserAccount) {
        _currentUser.value = account
        viewModelScope.launch {
            _userMessage.emit("Switched profile to: ${account.fullName} (${account.role.displayName})")
        }
    }

    // Offline / Online Toggle
    fun toggleOnlineStatus() {
        val newState = !_isOnline.value
        _isOnline.value = newState
        viewModelScope.launch {
            _userMessage.emit(if (newState) "Network Connected: App is ONLINE." else "Network Disconnected: Offline Mode Active. Scans will be queued.")
        }
    }

    // Trigger Cloud Sync
    fun triggerSync() {
        if (!_isOnline.value) {
            viewModelScope.launch {
                _userMessage.emit("Cannot sync: Currently in Offline Mode. Switch to Online first.")
            }
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val syncedCount = repository.performSync()
                _userMessage.emit("Sync Complete: $syncedCount records updated to cloud.")
            } catch (e: Exception) {
                _userMessage.emit("Sync Failed: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Classification Helper
    fun classifyBarcode(raw: String) = repository.classifyBarcode(raw)

    // Check duplicate
    suspend fun checkDuplicate(trackingNumber: String): ShipmentEntity? {
        return repository.checkDuplicate(trackingNumber)
    }

    // Register Scanned Shipment (Modules 2, 3, 4)
    fun registerScannedParcel(
        trackingNumber: String,
        recipientName: String,
        recipientPhone: String,
        recipientAddress: String,
        platform: PlatformType,
        courier: CourierType,
        weightKg: Double,
        declaredValue: Double,
        itemsSummary: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val duplicate = repository.checkDuplicate(trackingNumber)
            if (duplicate != null) {
                duplicateWarning.value = duplicate
                return@launch
            }

            val entity = ShipmentEntity(
                trackingNumber = trackingNumber.trim(),
                recipientName = recipientName.ifBlank { "Customer / Buyer" },
                recipientPhone = recipientPhone.ifBlank { "09XX-XXX-XXXX" },
                recipientAddress = recipientAddress.ifBlank { "Metro Manila, Philippines" },
                platform = platform,
                courier = courier,
                weightKg = weightKg,
                declaredValue = declaredValue,
                itemsSummary = itemsSummary.ifBlank { "General E-Commerce Merchandise" },
                status = ShipmentStatus.SCANNED,
                notes = notes
            )

            val result = repository.registerScannedShipment(entity, _currentUser.value, _isOnline.value)
            if (result.isSuccess) {
                _userMessage.emit("Parcel #${entity.trackingNumber} successfully scanned & registered!")
            } else {
                stateTransitionError.value = result.exceptionOrNull()?.message ?: "Failed to register scan."
            }
        }
    }

    // Update Status with strict state machine validation (Module 6)
    fun updateShipmentStatus(trackingNumber: String, newStatus: ShipmentStatus, notes: String = "") {
        viewModelScope.launch {
            val result = repository.updateShipmentStatus(
                trackingNumber = trackingNumber,
                targetStatus = newStatus,
                operator = _currentUser.value,
                isOnline = _isOnline.value,
                notes = notes
            )
            if (result.isSuccess) {
                _userMessage.emit("Status updated to: ${newStatus.displayName}")
                selectedShipmentForStatusChange.value = null
                // Refresh detail if open
                if (selectedShipmentForDetail.value?.trackingNumber == trackingNumber) {
                    selectedShipmentForDetail.value = repository.checkDuplicate(trackingNumber)
                }
            } else {
                stateTransitionError.value = result.exceptionOrNull()?.message ?: "State transition error."
            }
        }
    }

    // Dispatch Handover Batch (Module 5)
    fun createDispatchBatch(
        courier: CourierType,
        driverName: String,
        driverPlate: String,
        driverPhone: String,
        selectedTrackingNumbers: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.createAndHandoverDispatchBatch(
                courier = courier,
                driverName = driverName,
                driverPlate = driverPlate,
                driverPhone = driverPhone,
                selectedTrackingNumbers = selectedTrackingNumbers,
                operator = _currentUser.value,
                notes = notes,
                isOnline = _isOnline.value
            )
            if (result.isSuccess) {
                val batch = result.getOrNull()
                _userMessage.emit("Dispatch Handover Recorded! Batch #${batch?.batchNumber} (${batch?.parcelCount} parcels).")
                showCreateBatchDialog.value = false
            } else {
                stateTransitionError.value = result.exceptionOrNull()?.message ?: "Dispatch handover failed."
            }
        }
    }

    // Log Return (Module 7)
    fun logReturn(
        trackingNumber: String,
        reason: ReturnReason,
        condition: ItemCondition,
        refundStatus: RefundStatus,
        claimAmount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.logReturn(
                trackingNumber = trackingNumber,
                returnReason = reason,
                itemCondition = condition,
                refundStatus = refundStatus,
                claimAmount = claimAmount,
                notes = notes,
                operator = _currentUser.value,
                isOnline = _isOnline.value
            )
            if (result.isSuccess) {
                _userMessage.emit("Return logged for #$trackingNumber: ${reason.label}")
                showLogReturnDialog.value = null
            } else {
                stateTransitionError.value = result.exceptionOrNull()?.message ?: "Failed to log return."
            }
        }
    }

    fun updateRefundStatus(returnId: String, newStatus: RefundStatus) {
        viewModelScope.launch {
            val result = repository.updateReturnRefundStatus(
                returnId = returnId,
                newStatus = newStatus,
                operator = _currentUser.value,
                isOnline = _isOnline.value
            )
            if (result.isSuccess) {
                _userMessage.emit("Refund status updated: ${newStatus.label}")
            } else {
                stateTransitionError.value = result.exceptionOrNull()?.message ?: "Failed to update refund."
            }
        }
    }

    // Delete Shipment (Admin Only RBAC)
    fun deleteShipment(trackingNumber: String) {
        viewModelScope.launch {
            val result = repository.deleteShipment(trackingNumber, _currentUser.value)
            if (result.isSuccess) {
                _userMessage.emit("Shipment #$trackingNumber deleted by Admin.")
                selectedShipmentForDetail.value = null
            } else {
                stateTransitionError.value = result.exceptionOrNull()?.message ?: "Delete prohibited."
            }
        }
    }

    // Module 10: Reports & Export Generators
    fun generateShipmentsCsv(): String {
        val list = allShipments.value
        val sb = StringBuilder()
        sb.append("Tracking Number,Platform,Courier,Recipient,Phone,Weight (kg),Value (PHP),Status,Scanned At,Completed At,Batch ID,Notes\n")
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        for (item in list) {
            val scannedStr = df.format(Date(item.scannedAt))
            val completedStr = item.completedAt?.let { df.format(Date(it)) } ?: ""
            sb.append("\"${item.trackingNumber}\",")
            sb.append("\"${item.platform.displayName}\",")
            sb.append("\"${item.courier.displayName}\",")
            sb.append("\"${item.recipientName.replace("\"", "\"\"")}\",")
            sb.append("\"${item.recipientPhone}\",")
            sb.append("${item.weightKg},")
            sb.append("${item.declaredValue},")
            sb.append("\"${item.status.displayName}\",")
            sb.append("\"$scannedStr\",")
            sb.append("\"$completedStr\",")
            sb.append("\"${item.batchId ?: ""}\",")
            sb.append("\"${item.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun generateDispatchManifestText(batch: DispatchBatchEntity, items: List<ShipmentEntity>): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = df.format(Date(batch.handoverTimestamp))
        val sb = StringBuilder()
        sb.append("===================================================\n")
        sb.append("  GJandAsher ShipTracker - OFFICIAL DISPATCH MANIFEST\n")
        sb.append("===================================================\n")
        sb.append("Batch Number     : ${batch.batchNumber}\n")
        sb.append("Courier Partner  : ${batch.courier.displayName}\n")
        sb.append("Driver/Rider Name: ${batch.driverName}\n")
        sb.append("Vehicle Plate    : ${batch.driverPlateNumber}\n")
        sb.append("Driver Phone     : ${batch.driverPhone}\n")
        sb.append("Handover Date    : $dateStr\n")
        sb.append("Dispatched By    : ${batch.createdBy}\n")
        sb.append("Total Parcels    : ${batch.parcelCount}\n")
        sb.append("Total Weight     : ${"%.2f".format(batch.totalWeightKg)} kg\n")
        sb.append("---------------------------------------------------\n")
        sb.append("ITEMIZED PARCEL MANIFEST:\n")
        items.forEachIndexed { i, it ->
            sb.append("${i + 1}. [${it.trackingNumber}] ${it.platform.displayName} | ${it.recipientName} | ${it.weightKg}kg | Val: ₱${it.declaredValue}\n")
        }
        sb.append("---------------------------------------------------\n")
        sb.append("SIGNATURE & ACKNOWLEDGEMENT:\n\n")
        sb.append("Courier Driver Signature: _________________________\n")
        sb.append("Warehouse Supervisor:     _________________________\n")
        sb.append("===================================================\n")
        return sb.toString()
    }
}
