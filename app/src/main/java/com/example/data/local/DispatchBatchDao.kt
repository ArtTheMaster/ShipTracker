package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DispatchBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DispatchBatchDao {
    @Query("SELECT * FROM dispatch_batches ORDER BY handoverTimestamp DESC")
    fun getAllBatches(): Flow<List<DispatchBatchEntity>>

    @Query("SELECT * FROM dispatch_batches WHERE batchId = :batchId LIMIT 1")
    suspend fun getBatchById(batchId: String): DispatchBatchEntity?

    @Query("SELECT COUNT(*) FROM dispatch_batches")
    fun getTotalBatchesCount(): Flow<Int>

    @Query("SELECT * FROM dispatch_batches WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncBatches(): List<DispatchBatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: DispatchBatchEntity)

    @Update
    suspend fun updateBatch(batch: DispatchBatchEntity)
}
