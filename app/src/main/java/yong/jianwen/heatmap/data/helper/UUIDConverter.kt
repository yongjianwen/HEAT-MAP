package yong.jianwen.heatmap.data.helper

import android.util.Log
import androidx.room.TypeConverter
import java.util.UUID

class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun uuidFromString(string: String?): UUID {
        return UUID.fromString(string)
        /*return try {
            UUID.fromString(string)
        } catch (e: IllegalArgumentException) {
            Log.e("TEST", "string: " + (string ?: "null123"))
            UUID.randomUUID()
        }*/
    }
}
