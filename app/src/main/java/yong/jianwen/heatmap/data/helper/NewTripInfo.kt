package yong.jianwen.heatmap.data.helper

import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car

data class NewTripInfo(
    val car: Car,
    val tripMode: TripMode,
    val tripName: String,
    val trackName: String,
    val tripId: Long
)
