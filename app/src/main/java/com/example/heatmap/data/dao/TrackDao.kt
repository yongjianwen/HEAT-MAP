package com.example.heatmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.heatmap.data.entity.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAll(): Flow<List<Track>>

    @Query("SELECT MAX(number) AS latest_number FROM track WHERE trip_id = :tripId")
    suspend fun getLatestTrackNumberByTripId(tripId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: Track): Long

    @Update
    suspend fun update(track: Track)

    @Query("UPDATE track SET name = :trackName WHERE id = :id")
    suspend fun updateTrackNameById(id: Long, trackName: String)

    @Query("UPDATE track SET car_id = :carId WHERE id = :id")
    suspend fun updateTrackCarIdById(id: Long, carId: Int)

    @Query("UPDATE track SET type = :type WHERE id = :id")
    suspend fun updateTrackTypeById(id: Long, type: String)

    @Query("UPDATE track SET `end` = :end WHERE id = :id")
    suspend fun updateTrackEndById(id: Long, end: String)

    @Delete
    suspend fun delete(track: Track)

    @Query("DELETE FROM track WHERE trip_id = :tripId")
    suspend fun deleteByTripId(tripId: Long)
}
