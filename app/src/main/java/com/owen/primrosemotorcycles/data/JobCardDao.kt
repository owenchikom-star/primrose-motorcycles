package com.owen.primrosemotorcycles.data


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardDao {

    // --- GETS ---
    @Query("SELECT * FROM job_cards WHERE branchId = :branchId ORDER BY createdAt DESC")
    fun getAll(branchId: String = "primrose_motorcycles"): Flow<List<JobCard>>

    @Query("SELECT * FROM job_cards WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): JobCard?

    @Query("SELECT * FROM job_cards WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: String): Flow<JobCard?>


    @Query("SELECT * FROM job_cards WHERE branchId = :branchId AND status = :status ORDER BY createdAt DESC")
    fun getByStatus(branchId: String, status: JobStatus): Flow<List<JobCard>>

    @Query("""
        SELECT * FROM job_cards 
        WHERE branchId = :branchId 
        AND (customerName LIKE '%' || :query || '%' 
        OR bikeReg LIKE '%' || :query || '%' 
        OR jobNumber LIKE '%' || :query || '%' 
        OR bikeModel LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun search(branchId: String, query: String): Flow<List<JobCard>>

    // --- COUNTS for Job Number Generation ---
    @Query("SELECT COUNT(*) FROM job_cards WHERE branchId = :branchId")
    suspend fun count(branchId: String): Int

    @Query("SELECT COUNT(*) FROM job_cards WHERE branchId = :branchId AND status = :status")
    fun countByStatusFlow(branchId: String, status: JobStatus): Flow<Int>


    @Query("UPDATE job_cards SET branchId = 'germiston_main'")
    suspend fun fixBranchIds()

    // --- WRITE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobCard: JobCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<JobCard>)

    @Update
    suspend fun update(jobCard: JobCard)

    // Fast status change from list swipe
    @Query("UPDATE job_cards SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: JobStatus)

    // For PDF flags
    @Query("UPDATE job_cards SET checklistDone = :done WHERE id = :id")
    suspend fun setChecklistDone(id: String, done: Boolean)

    @Query("UPDATE job_cards SET quotationDone = :done WHERE id = :id")
    suspend fun setQuotationDone(id: String, done: Boolean)

    // Sync helpers for Firebase single shop login
    @Query("SELECT * FROM job_cards WHERE isSynced = 0")
    suspend fun getUnsynced(): List<JobCard>

    @Query("UPDATE job_cards SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    // --- DELETE (only for admin) ---
    @Query("DELETE FROM job_cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM job_cards WHERE branchId = :branchId")
    suspend fun clearBranch(branchId: String)
}