package yong.jianwen.heatmap.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class TrackWithTrackSegments(
    @Embedded val track: Track,
    @Relation(
        parentColumn = "id",
        entityColumn = "track_id",
        entity = TrackSegment::class
    )
    val trackSegments: List<TrackSegmentWithTrackPoints>
)
