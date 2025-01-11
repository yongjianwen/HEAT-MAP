package yong.jianwen.heatmap.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class TrackSegmentWithTrackPoints(
    @Embedded val trackSegment: TrackSegment,
    @Relation(
        parentColumn = "id",
        entityColumn = "track_segment_id",
        entity = TrackPoint::class
    )
    val trackPoints: List<TrackPoint>
)
