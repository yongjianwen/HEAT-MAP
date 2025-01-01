package yong.jianwen.heatmap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.helper.AdditionalTripInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip")
    fun getAll(): Flow<List<Trip>>

    @Query("SELECT * FROM trip")
    suspend fun getAllSuspend(): List<Trip>

//    @Query("SELECT * FROM trip WHERE id = :id")
//    fun getGPXById(id: Long): Flow<Trip>

    @Query("""
        SELECT trip.id AS tripId, track.type AS mode, car.*
        FROM trip
        INNER JOIN track ON track.trip_id = trip.id
        INNER JOIN car ON car.id = track.car_id
        GROUP BY trip.id, track.type, car.id
    """)
    fun getAdditionalTripInfo(): Flow<List<AdditionalTripInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Query("UPDATE trip SET name = :tripName WHERE id = :id")
    suspend fun updateTripNameById(id: Long, tripName: String)

    @Query("UPDATE trip SET `end` = :end WHERE id = :id")
    suspend fun updateTripEndById(id: Long, end: String)

    @Query("DELETE FROM trip WHERE id = :id")
    suspend fun delete(id: Long)
}
