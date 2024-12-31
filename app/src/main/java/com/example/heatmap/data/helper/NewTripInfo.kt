package com.example.heatmap.data.helper

import com.example.heatmap.data.TripMode
import com.example.heatmap.data.entity.Car

data class NewTripInfo(
    val car: Car,
    val tripMode: TripMode,
    val tripName: String,
    val trackName: String,
    val tripId: Long
)
