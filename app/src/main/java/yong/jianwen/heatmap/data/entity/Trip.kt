package yong.jianwen.heatmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import yong.jianwen.heatmap.data.helper.UUIDSerializer
import java.util.UUID

@Entity(tableName = "trip")
@Serializable
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "start") val start: String,
    @ColumnInfo(name = "end") val end: String,
    @Serializable(with = UUIDSerializer::class)
    @ColumnInfo(name = "uuid") val uuid: UUID = UUID.randomUUID()
)
