package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dispatch_batches")
data class DispatchBatchEntity(
    @PrimaryKey
    val batchId: String,
    val batchNumber: String,
    val courier: CourierType,
    val driverName: String,
    val driverPlateNumber: String,
    val driverPhone: String,
    val handoverTimestamp: Long = System.currentTimeMillis(),
    val parcelCount: Int = 0,
    val totalWeightKg: Double = 0.0,
    val status: String = "HANDED_OVER", // DRAFT, HANDED_OVER
    val notes: String = "",
    val createdBy: String = "Staff",
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
