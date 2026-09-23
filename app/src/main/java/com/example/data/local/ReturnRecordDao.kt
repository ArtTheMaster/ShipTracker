package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ReturnRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnRecordDao {
    @Query("SELECT * FROM return_records ORDER BY loggedAt DESC")
    fun getAllReturns(): Flow<List<ReturnRecordEntity>>

    @Query("SELECT * FROM return_records WHERE trackingNumber = :trackingNumber LIMIT 1")
    suspend fun getReturnByTracking(trackingNumber: String): ReturnRecordEntity?

    @Query("SELECT COUNT(*) FROM return_records")
    fun getTotalReturnsCount(): Flow<Int>

    @Query("SELECT * FROM return_records WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncReturns(): List<ReturnRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(returnRecord: ReturnRecordEntity)

    @Update
    suspend fun updateReturn(returnRecord: ReturnRecordEntity)
}
