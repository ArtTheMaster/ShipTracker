package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shipments")
data class ShipmentEntity(
    @PrimaryKey
    val trackingNumber: String,
    val recipientName: String,
    val recipientPhone: String,
    val recipientAddress: String,
    val platform: PlatformType,
    val courier: CourierType,
    val weightKg: Double,
    val dimensionsCm: String = "15x10x5",
    val declaredValue: Double,
    val itemsSummary: String,
    val status: ShipmentStatus = ShipmentStatus.SCANNED,
    val batchId: String? = null,
    val notes: String = "",
    val scannedAt: Long = System.currentTimeMillis(),
    val packedAt: Long? = null,
    val dispatchedAt: Long? = null,
    val inTransitAt: Long? = null,
    val completedAt: Long? = null,
    val scannedBy: String = "Staff",
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastModifiedAt: Long = System.currentTimeMillis()
)
