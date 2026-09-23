package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShipmentEntity
import com.example.data.model.ShipmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM shipments ORDER BY scannedAt DESC")
    fun getAllShipments(): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE trackingNumber = :trackingNumber LIMIT 1")
    suspend fun getShipmentByTrackingNumber(trackingNumber: String): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE status = :status ORDER BY scannedAt DESC")
    fun getShipmentsByStatus(status: ShipmentStatus): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE status = 'PREPARED' AND (batchId IS NULL OR batchId = '')")
    fun getPreparedShipmentsForDispatch(): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE batchId = :batchId")
    fun getShipmentsInBatch(batchId: String): Flow<List<ShipmentEntity>>

    @Query("SELECT COUNT(*) FROM shipments")
    fun getTotalShipmentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shipments WHERE status = 'DELIVERED'")
    fun getDeliveredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shipments WHERE status = 'RETURNED'")
    fun getReturnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shipments WHERE syncStatus != 'SYNCED'")
    fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT * FROM shipments WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncShipments(): List<ShipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: ShipmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipments(shipments: List<ShipmentEntity>)

    @Update
    suspend fun updateShipment(shipment: ShipmentEntity)

    @Query("DELETE FROM shipments WHERE trackingNumber = :trackingNumber")
    suspend fun deleteShipment(trackingNumber: String)

    @Query("UPDATE shipments SET status = :newStatus, dispatchedAt = :dispatchedAt, lastModifiedAt = :now, syncStatus = 'PENDING_SYNC' WHERE batchId = :batchId")
    suspend fun markBatchShipmentsDispatched(batchId: String, newStatus: ShipmentStatus = ShipmentStatus.DISPATCHED, dispatchedAt: Long, now: Long)
}
