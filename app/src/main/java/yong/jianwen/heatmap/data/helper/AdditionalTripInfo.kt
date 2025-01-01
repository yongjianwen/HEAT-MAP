package yong.jianwen.heatmap.data.helper

import androidx.room.Embedded
import yong.jianwen.heatmap.data.entity.Car

data class AdditionalTripInfo(
    val tripId: Long,
    val mode: String,
    @Embedded
    val car: Car
)
