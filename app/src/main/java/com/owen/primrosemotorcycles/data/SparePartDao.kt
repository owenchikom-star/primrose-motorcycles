package com.owen.primrosemotorcycles.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SparePartDao {
    @Query("SELECT * FROM spare_parts WHERE jobCardId = :jobId")
    fun getForJob(jobId: String): Flow<List<SparePart>>

    @Query("SELECT * FROM spare_parts WHERE jobCardId = :jobId")
    suspend fun getForJobSync(jobId: String): List<SparePart>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(part: SparePart)

    @Delete
    suspend fun delete(part: SparePart)

    @Query("SELECT SUM(quantity * unitPrice) FROM spare_parts WHERE jobCardId = :jobId")
    fun getTotalForJob(jobId: String): Flow<Double?>

    @Query("SELECT SUM(quantity * unitPrice) FROM spare_parts WHERE jobCardId = :jobId")
    suspend fun getTotalForJobSync(jobId: String): Double?

    @Query("SELECT * FROM spare_parts WHERE id = :id")
    suspend fun getById(id: String): SparePart?

    @Query("DELETE FROM spare_parts WHERE id = :id")
    suspend fun deleteById(id: String)
}