package yong.jianwen.heatmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import yong.jianwen.heatmap.data.entity.TrackPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    @Query("SELECT * FROM track_point")
    fun getAll(): Flow<List<TrackPoint>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trackPoint: TrackPoint)

    @Update
    suspend fun update(trackPoint: TrackPoint)

    @Delete
    suspend fun delete(trackPoint: TrackPoint)

    @Query("""
        DELETE FROM track_point
        WHERE track_segment_id IN (
            SELECT id FROM track_segment WHERE track_id IN (
                SELECT id FROM track WHERE trip_id = :tripId
            )
        )
    """)
    suspend fun deleteByTripId(tripId: Long)

    @Query("""
        DELETE FROM track_point
        WHERE track_segment_id IN (
            SELECT id FROM track_segment WHERE track_id = :trackId
        )
    """)
    suspend fun deleteByTrackId(trackId: Long)
}
