package com.example.heatmap.data.helper

import androidx.room.Embedded
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.heatmap.data.entity.Car

data class AdditionalTripInfo(
    val tripId: Long,
    val mode: String,
    @Embedded
    val car: Car
)
