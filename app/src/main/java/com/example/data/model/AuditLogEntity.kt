package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val targetId: String,
    val details: String,
    val performedBy: String,
    val role: UserRole,
    val timestamp: Long = System.currentTimeMillis()
)
