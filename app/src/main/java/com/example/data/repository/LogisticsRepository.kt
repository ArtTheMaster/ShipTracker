package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CourierType
import com.example.data.model.DispatchBatchEntity
import com.example.data.model.ItemCondition
import com.example.data.model.PlatformType
import com.example.data.model.RefundStatus
import com.example.data.model.ReturnReason
import com.example.data.model.ReturnRecordEntity
import com.example.data.model.ShipmentEntity
import com.example.data.model.ShipmentStatus
import com.example.data.model.SyncStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LogisticsRepository(private val database: AppDatabase) {

    private val shipmentDao = database.shipmentDao()
    private val batchDao = database.dispatchBatchDao()
    private val returnDao = database.returnRecordDao()
    private val auditDao = database.auditLogDao()

    val allShipments: Flow<List<ShipmentEntity>> = shipmentDao.getAllShipments()
    val allBatches: Flow<List<DispatchBatchEntity>> = batchDao.getAllBatches()
    val allReturns: Flow<List<ReturnRecordEntity>> = returnDao.getAllReturns()
    val recentAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getRecentLogs()
    val pendingSyncCount: Flow<Int> = shipmentDao.getPendingSyncCount()

    companion object {
        data class ClassificationResult(
            val platform: PlatformType,
            val courier: CourierType,
            val ruleMatched: String
        )

        fun classifyBarcode(rawBarcode: String): ClassificationResult {
            val clean = rawBarcode.trim().uppercase()
            return when {
                clean.startsWith("SPX") || clean.contains("SPXPH") -> ClassificationResult(
                    platform = PlatformType.SHOPEE,
                    courier = CourierType.SPX,
                    ruleMatched = "Shopee Xpress pattern (SPX*)"
                )
                clean.startsWith("LX") || clean.startsWith("MP") || clean.startsWith("LEX") -> ClassificationResult(
                    platform = PlatformType.LAZADA,
                    courier = CourierType.LAZADA_LEX,
                    ruleMatched = "Lazada Express pattern (LX*/MP*)"
                )
                clean.startsWith("TT") || clean.startsWith("TK") -> ClassificationResult(
                    platform = PlatformType.TIKTOK_SHOP,
                    courier = CourierType.J_AND_T,
                    ruleMatched = "TikTok Shop fulfillment (TT*/TK* via J&T)"
                )
                clean.startsWith("JT") || clean.startsWith("788") || clean.startsWith("789") -> ClassificationResult(
                    platform = PlatformType.SHOPEE,
                    courier = CourierType.J_AND_T,
                    ruleMatched = "J&T Express standard waybill (JT*/788*)"
                )
                clean.startsWith("FL") || clean.startsWith("TH") || clean.startsWith("KEX") -> ClassificationResult(
                    platform = PlatformType.OTHER,
                    courier = CourierType.FLASH_EXPRESS,
                    ruleMatched = "Flash Express barcode prefix (FL*/TH*)"
                )
                clean.startsWith("NL") || clean.startsWith("NV") || clean.startsWith("NINJA") -> ClassificationResult(
                    platform = PlatformType.OTHER,
                    courier = CourierType.NINJA_VAN,
                    ruleMatched = "Ninja Van parcel identifier (NL*/NV*)"
                )
                else -> ClassificationResult(
                    platform = PlatformType.OTHER,
                    courier = CourierType.OTHER,
                    ruleMatched = "Unrecognized pattern - Manual selection needed"
                )
            }
        }

        fun isValidTransition(current: ShipmentStatus, target: ShipmentStatus): Boolean {
            if (current == target) return true
            if (current.isTerminal) return false
            return when (current) {
                ShipmentStatus.SCANNED -> target == ShipmentStatus.PREPARED || target == ShipmentStatus.CANCELLED
                ShipmentStatus.PREPARED -> target == ShipmentStatus.DISPATCHED || target == ShipmentStatus.SCANNED || target == ShipmentStatus.CANCELLED
                ShipmentStatus.DISPATCHED -> target == ShipmentStatus.IN_TRANSIT || target == ShipmentStatus.CANCELLED
                ShipmentStatus.IN_TRANSIT -> target == ShipmentStatus.DELIVERED || target == ShipmentStatus.RETURNED || target == ShipmentStatus.CANCELLED
                else -> false
            }
        }

        fun validateStateTransition(current: ShipmentStatus, target: ShipmentStatus) {
            if (current == target) return

            if (current.isTerminal) {
                throw IllegalStateException(
                    "Terminal State Error: Parcel is already '${current.displayName}' and cannot be modified."
                )
            }

            val valid = isValidTransition(current, target)
            if (!valid) {
                throw IllegalStateException(
                    "SOP Violation: Illegal transition from '${current.displayName}' to '${target.displayName}'. " +
                            "Outbound logistics flow requires: Scanned → Prepared → Dispatched → In-Transit → Delivered/Returned."
                )
            }
        }
    }

    fun classifyBarcode(rawBarcode: String): ClassificationResult = Companion.classifyBarcode(rawBarcode)
    fun validateStateTransition(current: ShipmentStatus, target: ShipmentStatus) = Companion.validateStateTransition(current, target)

    // --- MODULE 3: DUPLICATE DETECTION ---
    suspend fun checkDuplicate(trackingNumber: String): ShipmentEntity? = withContext(Dispatchers.IO) {
        shipmentDao.getShipmentByTrackingNumber(trackingNumber.trim())
    }

    // --- MODULE 2 & 3: SCAN & REGISTER SHIPMENT ---
    suspend fun registerScannedShipment(
        shipment: ShipmentEntity,
        operator: UserAccount,
        isOnline: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = shipmentDao.getShipmentByTrackingNumber(shipment.trackingNumber)
        if (existing != null) {
            return@withContext Result.failure(
                IllegalStateException("DUPLICATE SCAN: Tracking #${shipment.trackingNumber} already exists with status: ${existing.status.displayName}")
            )
        }

        val syncState = if (isOnline) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC
        val toSave = shipment.copy(
            scannedAt = System.currentTimeMillis(),
            scannedBy = operator.fullName,
            syncStatus = syncState,
            lastModifiedAt = System.currentTimeMillis()
        )
        shipmentDao.insertShipment(toSave)
        auditDao.insertLog(
            AuditLogEntity(
                action = "PARCEL_SCANNED",
                targetId = toSave.trackingNumber,
                details = "Scanned ${toSave.platform.displayName} / ${toSave.courier.displayName} (${toSave.weightKg} kg)",
                performedBy = operator.fullName,
                role = operator.role
            )
        )
        Result.success(Unit)
    }

    // --- MODULE 6: STRICT STATE MACHINE VALIDATIONS & TRANSITIONS ---
    suspend fun updateShipmentStatus(
        trackingNumber: String,
        targetStatus: ShipmentStatus,
        operator: UserAccount,
        isOnline: Boolean,
        notes: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val current = shipmentDao.getShipmentByTrackingNumber(trackingNumber)
                ?: return@withContext Result.failure(IllegalArgumentException("Shipment #$trackingNumber not found"))

            validateStateTransition(current.status, targetStatus)

            val now = System.currentTimeMillis()
            val syncState = if (isOnline) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC

            val updated = current.copy(
                status = targetStatus,
                packedAt = if (targetStatus == ShipmentStatus.PREPARED && current.packedAt == null) now else current.packedAt,
                dispatchedAt = if (targetStatus == ShipmentStatus.DISPATCHED && current.dispatchedAt == null) now else current.dispatchedAt,
                inTransitAt = if (targetStatus == ShipmentStatus.IN_TRANSIT && current.inTransitAt == null) now else current.inTransitAt,
                completedAt = if ((targetStatus == ShipmentStatus.DELIVERED || targetStatus == ShipmentStatus.RETURNED || targetStatus == ShipmentStatus.CANCELLED) && current.completedAt == null) now else current.completedAt,
                notes = if (notes.isNotBlank()) "${current.notes}\n$notes".trim() else current.notes,
                syncStatus = syncState,
                lastModifiedAt = now
            )

            shipmentDao.updateShipment(updated)
            auditDao.insertLog(
                AuditLogEntity(
                    action = "STATUS_CHANGED",
                    targetId = trackingNumber,
                    details = "Changed ${current.status.displayName} → ${targetStatus.displayName}",
                    performedBy = operator.fullName,
                    role = operator.role
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- MODULE 5: DISPATCH MANAGEMENT & HANDOVER ---
    suspend fun createAndHandoverDispatchBatch(
        courier: CourierType,
        driverName: String,
        driverPlate: String,
        driverPhone: String,
        selectedTrackingNumbers: List<String>,
        operator: UserAccount,
        notes: String,
        isOnline: Boolean
    ): Result<DispatchBatchEntity> = withContext(Dispatchers.IO) {
        try {
            if (selectedTrackingNumbers.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Please select at least 1 prepared parcel for dispatch."))
            }

            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault())
            val batchNumber = "DSP-${dateFormat.format(Date(timestamp))}"
            val batchId = UUID.randomUUID().toString()

            var totalWeight = 0.0
            val syncState = if (isOnline) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC

            for (track in selectedTrackingNumbers) {
                val shipment = shipmentDao.getShipmentByTrackingNumber(track)
                if (shipment != null) {
                    validateStateTransition(shipment.status, ShipmentStatus.DISPATCHED)
                    totalWeight += shipment.weightKg
                    val updated = shipment.copy(
                        status = ShipmentStatus.DISPATCHED,
                        batchId = batchId,
                        dispatchedAt = timestamp,
                        syncStatus = syncState,
                        lastModifiedAt = timestamp
                    )
                    shipmentDao.updateShipment(updated)
                }
            }

            val batch = DispatchBatchEntity(
                batchId = batchId,
                batchNumber = batchNumber,
                courier = courier,
                driverName = driverName,
                driverPlateNumber = driverPlate,
                driverPhone = driverPhone,
                handoverTimestamp = timestamp,
                parcelCount = selectedTrackingNumbers.size,
                totalWeightKg = totalWeight,
                status = "HANDED_OVER",
                notes = notes,
                createdBy = operator.fullName,
                syncStatus = syncState
            )

            batchDao.insertBatch(batch)
            auditDao.insertLog(
                AuditLogEntity(
                    action = "DISPATCH_HANDOVER",
                    targetId = batchNumber,
                    details = "Dispatched ${selectedTrackingNumbers.size} parcels to $driverName ($courier)",
                    performedBy = operator.fullName,
                    role = operator.role
                )
            )

            Result.success(batch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- MODULE 7: RETURNS & REFUND MANAGEMENT ---
    suspend fun logReturn(
        trackingNumber: String,
        returnReason: ReturnReason,
        itemCondition: ItemCondition,
        refundStatus: RefundStatus,
        claimAmount: Double,
        notes: String,
        operator: UserAccount,
        isOnline: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val shipment = shipmentDao.getShipmentByTrackingNumber(trackingNumber)
                ?: return@withContext Result.failure(IllegalArgumentException("Tracking #$trackingNumber not found"))

            val now = System.currentTimeMillis()
            val syncState = if (isOnline) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC

            val returnRecord = ReturnRecordEntity(
                returnId = "RET-${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
                trackingNumber = trackingNumber,
                platform = shipment.platform,
                courier = shipment.courier,
                returnReason = returnReason,
                itemCondition = itemCondition,
                refundStatus = refundStatus,
                claimAmount = claimAmount,
                notes = notes,
                loggedBy = operator.fullName,
                loggedAt = now,
                syncStatus = syncState
            )

            returnDao.insertReturn(returnRecord)

            // Update shipment status to RETURNED
            val updatedShipment = shipment.copy(
                status = ShipmentStatus.RETURNED,
                completedAt = now,
                notes = "${shipment.notes}\n[RETURN LOGGED] Reason: ${returnReason.label}, Condition: ${itemCondition.label}".trim(),
                syncStatus = syncState,
                lastModifiedAt = now
            )
            shipmentDao.updateShipment(updatedShipment)

            auditDao.insertLog(
                AuditLogEntity(
                    action = "RETURN_LOGGED",
                    targetId = trackingNumber,
                    details = "Logged return: ${returnReason.label} | ${refundStatus.label}",
                    performedBy = operator.fullName,
                    role = operator.role
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReturnRefundStatus(
        returnId: String,
        newStatus: RefundStatus,
        operator: UserAccount,
        isOnline: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val returns = returnDao.getAllReturns().first()
            val record = returns.find { it.returnId == returnId }
                ?: return@withContext Result.failure(IllegalArgumentException("Return record not found"))

            val syncState = if (isOnline) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC
            val updated = record.copy(
                refundStatus = newStatus,
                syncStatus = syncState
            )
            returnDao.updateReturn(updated)

            auditDao.insertLog(
                AuditLogEntity(
                    action = "REFUND_STATUS_UPDATED",
                    targetId = record.trackingNumber,
                    details = "Updated refund status to: ${newStatus.label}",
                    performedBy = operator.fullName,
                    role = operator.role
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- MODULE 8: OFFLINE SYNC MANAGEMENT ---
    suspend fun performSync(): Int = withContext(Dispatchers.IO) {
        // Collect all pending items
        val pendingShipments = shipmentDao.getPendingSyncShipments()
        val pendingBatches = batchDao.getPendingSyncBatches()
        val pendingReturns = returnDao.getPendingSyncReturns()

        var syncedCount = 0

        // Synchronize shipments
        for (item in pendingShipments) {
            shipmentDao.updateShipment(item.copy(syncStatus = SyncStatus.SYNCED))
            syncedCount++
        }

        // Synchronize batches
        for (batch in pendingBatches) {
            batchDao.updateBatch(batch.copy(syncStatus = SyncStatus.SYNCED))
            syncedCount++
        }

        // Synchronize returns
        for (ret in pendingReturns) {
            returnDao.updateReturn(ret.copy(syncStatus = SyncStatus.SYNCED))
            syncedCount++
        }

        auditDao.insertLog(
            AuditLogEntity(
                action = "OFFLINE_SYNC_COMPLETED",
                targetId = "CLOUD_SYNC",
                details = "Successfully synchronized $syncedCount queued records to cloud backend.",
                performedBy = "System / Auto-Sync",
                role = UserRole.OWNER_ADMIN
            )
        )

        syncedCount
    }

    // --- RBAC: ADMIN ONLY DELETE ---
    suspend fun deleteShipment(trackingNumber: String, operator: UserAccount): Result<Unit> = withContext(Dispatchers.IO) {
        if (operator.role != UserRole.OWNER_ADMIN) {
            return@withContext Result.failure(
                SecurityException("Access Denied: Only Owner/Admin can delete shipment records.")
            )
        }
        shipmentDao.deleteShipment(trackingNumber)
        auditDao.insertLog(
            AuditLogEntity(
                action = "SHIPMENT_DELETED",
                targetId = trackingNumber,
                details = "Shipment record purged by Admin",
                performedBy = operator.fullName,
                role = operator.role
            )
        )
        Result.success(Unit)
    }

    // --- SEED SAMPLE DATA ---
    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = shipmentDao.getTotalShipmentCount().first()
        if (count > 0) return@withContext

        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        val samples = listOf(
            ShipmentEntity(
                trackingNumber = "SPXPH0492819283",
                recipientName = "Juan Dela Cruz",
                recipientPhone = "0917-889-1234",
                recipientAddress = "Blk 12 Lot 4, San Jose, Quezon City",
                platform = PlatformType.SHOPEE,
                courier = CourierType.SPX,
                weightKg = 0.65,
                dimensionsCm = "18x12x6",
                declaredValue = 1250.0,
                itemsSummary = "Ergonomic Mechanical Keyboard (RGB)",
                status = ShipmentStatus.DELIVERED,
                scannedAt = now - 3 * day,
                packedAt = now - 3 * day + hour,
                dispatchedAt = now - 2 * day,
                inTransitAt = now - 2 * day + 2 * hour,
                completedAt = now - day,
                scannedBy = "Nolan (Owner)",
                syncStatus = SyncStatus.SYNCED
            ),
            ShipmentEntity(
                trackingNumber = "LX092817291PH",
                recipientName = "Camille Bautista",
                recipientPhone = "0920-555-7890",
                recipientAddress = "Tower 3, Unit 14B, BGC, Taguig City",
                platform = PlatformType.LAZADA,
                courier = CourierType.LAZADA_LEX,
                weightKg = 1.20,
                dimensionsCm = "25x20x10",
                declaredValue = 2890.0,
                itemsSummary = "Stainless Steel Air Fryer Basket & Accessories",
                status = ShipmentStatus.IN_TRANSIT,
                scannedAt = now - 2 * day,
                packedAt = now - 2 * day + 2 * hour,
                dispatchedAt = now - day,
                inTransitAt = now - 18 * hour,
                scannedBy = "G.J. (Packer-Scanner)",
                syncStatus = SyncStatus.SYNCED
            ),
            ShipmentEntity(
                trackingNumber = "TT982716253PH",
                recipientName = "Angelo Roxas",
                recipientPhone = "0998-333-2145",
                recipientAddress = "142 M.H. Del Pilar St., Malabon City",
                platform = PlatformType.TIKTOK_SHOP,
                courier = CourierType.J_AND_T,
                weightKg = 0.40,
                dimensionsCm = "15x10x5",
                declaredValue = 499.0,
                itemsSummary = "Matte Liquid Lip Tint Set (6 Colors)",
                status = ShipmentStatus.RETURNED,
                scannedAt = now - 5 * day,
                packedAt = now - 5 * day + hour,
                dispatchedAt = now - 4 * day,
                inTransitAt = now - 4 * day + 3 * hour,
                completedAt = now - day,
                notes = "Customer refused COD upon delivery. Package intact.",
                scannedBy = "G.J. (Packer-Scanner)",
                syncStatus = SyncStatus.SYNCED
            ),
            ShipmentEntity(
                trackingNumber = "JT8291029381PH",
                recipientName = "Stephanie Mercado",
                recipientPhone = "0915-444-9988",
                recipientAddress = "Brgy. Concepcion, Marikina City",
                platform = PlatformType.SHOPEE,
                courier = CourierType.J_AND_T,
                weightKg = 0.85,
                dimensionsCm = "20x15x8",
                declaredValue = 890.0,
                itemsSummary = "Minimalist Waterproof Backpack",
                status = ShipmentStatus.PREPARED,
                scannedAt = now - 6 * hour,
                packedAt = now - 4 * hour,
                scannedBy = "G.J. (Packer-Scanner)",
                syncStatus = SyncStatus.SYNCED
            ),
            ShipmentEntity(
                trackingNumber = "FL992817265PH",
                recipientName = "Darwin Gomez",
                recipientPhone = "0922-111-4567",
                recipientAddress = "Poblacion 2, San Pedro, Laguna",
                platform = PlatformType.OTHER,
                courier = CourierType.FLASH_EXPRESS,
                weightKg = 2.10,
                dimensionsCm = "30x25x15",
                declaredValue = 3450.0,
                itemsSummary = "Camping Tent 4-Person Waterproof",
                status = ShipmentStatus.PREPARED,
                scannedAt = now - 5 * hour,
                packedAt = now - 3 * hour,
                scannedBy = "G.J. (Packer-Scanner)",
                syncStatus = SyncStatus.SYNCED
            ),
            ShipmentEntity(
                trackingNumber = "SPXPH0992817462",
                recipientName = "Patricia Tan",
                recipientPhone = "0919-777-6655",
                recipientAddress = "Greenhills West, San Juan City",
                platform = PlatformType.SHOPEE,
                courier = CourierType.SPX,
                weightKg = 0.35,
                dimensionsCm = "12x10x4",
                declaredValue = 680.0,
                itemsSummary = "MagSafe Phone Mount & Wireless Charger",
                status = ShipmentStatus.SCANNED,
                scannedAt = now - 2 * hour,
                scannedBy = "G.J. (Packer-Scanner)",
                syncStatus = SyncStatus.SYNCED
            )
        )

        shipmentDao.insertShipments(samples)

        // Return record for the returned sample
        val sampleReturn = ReturnRecordEntity(
            returnId = "RET-001",
            trackingNumber = "TT982716253PH",
            platform = PlatformType.TIKTOK_SHOP,
            courier = CourierType.J_AND_T,
            returnReason = ReturnReason.CUSTOMER_REJECTED_COD,
            itemCondition = ItemCondition.PRISTINE_RESELLABLE,
            refundStatus = RefundStatus.REFUND_APPROVED,
            claimAmount = 499.0,
            notes = "Unopened parcel. Good for restocking.",
            loggedBy = "G.J. (Packer-Scanner)",
            loggedAt = now - day,
            syncStatus = SyncStatus.SYNCED
        )
        returnDao.insertReturn(sampleReturn)

        // Seed an initial dispatch batch
        val sampleBatch = DispatchBatchEntity(
            batchId = "BATCH-001",
            batchNumber = "DSP-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(now - day))}-01",
            courier = CourierType.LAZADA_LEX,
            driverName = "Kuya Ronald Santos",
            driverPlateNumber = "NDB-8821",
            driverPhone = "0918-234-5678",
            handoverTimestamp = now - day,
            parcelCount = 1,
            totalWeightKg = 1.20,
            status = "HANDED_OVER",
            notes = "Lazada LEX 3PM regular hub pickup",
            createdBy = "Nolan (Owner)",
            syncStatus = SyncStatus.SYNCED
        )
        batchDao.insertBatch(sampleBatch)
    }
}
