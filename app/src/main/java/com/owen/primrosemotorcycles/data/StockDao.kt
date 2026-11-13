package com.owen.primrosemotorcycles.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_items ORDER BY quantityOnHand ASC, name ASC")
    fun getAllStock(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE quantityOnHand <= minQuantity ORDER BY quantityOnHand ASC")
    fun getLowStock(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE name LIKE '%' || :query || '%' OR partNumber LIKE '%' || :query || '%' OR barcode = :query")
    fun searchStock(query: String): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE id = :id")
    suspend fun getById(id: String): StockItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockItem)

    @Query("UPDATE stock_items SET quantityOnHand = :newQty, lastUpdated = :time WHERE id = :id")
    suspend fun updateQty(id: String, newQty: Int, time: Long = System.currentTimeMillis())

    @Insert
    suspend fun insertTransaction(tx: StockTransaction)

    @Query("SELECT * FROM stock_transactions WHERE stockItemId = :itemId ORDER BY timestamp DESC")
    fun getTransactions(itemId: String): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC LIMIT 100")
    fun getRecentTransactions(): Flow<List<StockTransaction>>
}