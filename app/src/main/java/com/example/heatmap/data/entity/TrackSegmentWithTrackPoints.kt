package com.example.heatmap.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TrackSegmentWithTrackPoints(
    @Embedded val trackSegment: TrackSegment,
    @Relation(
        parentColumn = "id",
        entityColumn = "track_segment_id",
        entity = TrackPoint::class
    )
    val trackPoints: List<TrackPoint>
)
