package yong.jianwen.heatmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_point",
    foreignKeys = [
        ForeignKey(
            entity = TrackSegment::class,
            childColumns = ["track_segment_id"],
            parentColumns = ["id"]
        )
    ]
)
data class TrackPoint(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "track_segment_id") val trackSegmentId: Long,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "elevation") val elevation: Int,
    @ColumnInfo(name = "time") val time: String
)
