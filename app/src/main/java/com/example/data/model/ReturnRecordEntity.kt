package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "return_records")
data class ReturnRecordEntity(
    @PrimaryKey
    val returnId: String,
    val trackingNumber: String,
    val platform: PlatformType,
    val courier: CourierType,
    val returnReason: ReturnReason,
    val itemCondition: ItemCondition,
    val refundStatus: RefundStatus = RefundStatus.PENDING_VERIFICATION,
    val claimAmount: Double = 0.0,
    val notes: String = "",
    val loggedBy: String = "Staff",
    val loggedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
