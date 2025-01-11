package yong.jianwen.heatmap.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class TripWithTracks(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "trip_id",
        entity = Track::class
    )
    val tracks: List<TrackWithTrackSegments>
)
