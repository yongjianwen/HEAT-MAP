package yong.jianwen.heatmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import yong.jianwen.heatmap.data.entity.TrackSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackSegmentDao {
    @Query("SELECT * FROM track_segment")
    fun getAll(): Flow<List<TrackSegment>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trackSegment: TrackSegment): Long

    @Update
    suspend fun update(trackSegment: TrackSegment)

    @Delete
    suspend fun delete(trackSegment: TrackSegment)

    @Query("""
        DELETE FROM track_segment
        WHERE track_id IN (
            SELECT id FROM track WHERE trip_id = :tripId
        )
    """)
    suspend fun deleteByTripId(tripId: Long)
}
