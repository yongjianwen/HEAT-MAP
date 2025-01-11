package yong.jianwen.heatmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "track_segment",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            childColumns = ["track_id"],
            parentColumns = ["id"]
        )
    ]
)
@Serializable
data class TrackSegment(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "number") val number: Int
)
