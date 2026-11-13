package com.owen.primrosemotorcycles.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckupDao {
    @Query("SELECT * FROM fleet_bikes ORDER BY addedAt ASC")
    fun getAllFleetBikes(): Flow<List<FleetBike>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFleetBike(bike: FleetBike)

    @Delete
    suspend fun deleteFleetBike(bike: FleetBike)

    @Query("DELETE FROM fleet_bikes WHERE registration = :registration")
    suspend fun deleteFleetBikeByReg(registration: String)

    @Query("SELECT * FROM weekly_checkups ORDER BY date DESC")
    fun getAllCheckups(): Flow<List<WeeklyCheckup>>

    @Query("SELECT * FROM checkup_items WHERE checkupId = :checkupId")
    fun getItems(checkupId: Int): Flow<List<CheckupItem>>

    @Insert
    suspend fun insertCheckup(checkup: WeeklyCheckup): Long

    @Insert
    suspend fun insertItems(items: List<CheckupItem>)

    @Query("SELECT * FROM weekly_checkups WHERE bikeReg = :reg ORDER BY date DESC LIMIT 1")
    suspend fun getLastCheckupForBike(reg: String): WeeklyCheckup?

    @Query("SELECT * FROM weekly_checkups WHERE id = :id")
    suspend fun getCheckupById(id: Int): WeeklyCheckup?

    @Query("SELECT * FROM checkup_items WHERE checkupId = :id")
    suspend fun getItemsSync(id: Int): List<CheckupItem>
}
