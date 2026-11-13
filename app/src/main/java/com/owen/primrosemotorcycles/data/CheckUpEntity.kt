package com.owen.primrosemotorcycles.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fleet_bikes")
data class FleetBike(
    @PrimaryKey val registration: String,
    val model: String = "",
    val driver: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weekly_checkups")
data class WeeklyCheckup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bikeReg: String, // e.g. "AKM-001" / "CA 123-456"
    val bikeModel: String,
    val date: Long = System.currentTimeMillis(),
    val checkedBy: String = "Primrose",
    val odometer: String = "",
    val overallStatus: String = "PASS", // PASS, NEEDS_SERVICE, NEEDS_REPAIR
    val notes: String = "",
    val driverSignature: String = "" // Base64 signature
)

@Entity(tableName = "checkup_items")
data class CheckupItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val checkupId: Int,
    val name: String, // Tyres, Brakes, Chain, Oil, Lights
    val status: String = "OK", // OK, NOTE, FIX_NOW
    val note: String = "",
    val jobCardId: String? = null // if Fix Now, link to job card
)
