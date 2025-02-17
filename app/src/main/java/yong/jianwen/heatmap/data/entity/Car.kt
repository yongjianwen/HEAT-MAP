package yong.jianwen.heatmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import yong.jianwen.heatmap.data.Selectable

@Entity(tableName = "car")
@Serializable
data class Car(
    @PrimaryKey(autoGenerate = true) override val id: Int,
    @ColumnInfo(name = "registration_number") val registrationNumber: String,
    @ColumnInfo(name = "manufacturer") val manufacturer: String,
    @ColumnInfo(name = "model") val model: String
) : Selectable {
    override fun getDisplayName(): String {
        return String.format("${this.manufacturer} ${this.model} (${this.registrationNumber})")
    }

    override fun getLabelName(): String {
        return "${this.manufacturer} ${this.model}"
    }
}
