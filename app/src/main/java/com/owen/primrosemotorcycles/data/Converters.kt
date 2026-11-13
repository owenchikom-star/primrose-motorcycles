package com.owen.primrosemotorcycles.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromJobStatus(status: JobStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toJobStatus(value: String?): JobStatus? {
        return value?.let {
            try {
                JobStatus.valueOf(it)
            } catch (e: Exception) {
                JobStatus.OPEN
            }
        }
    }
}
