package com.example.heatmap.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.heatmap.data.entity.TripWithTracks
import kotlinx.coroutines.flow.Flow

@Dao
interface TripWithTracksDao {
    @Query("SELECT * FROM trip WHERE id = :id")
    fun getById(id: Long): Flow<TripWithTracks?>

    @Query("SELECT * FROM trip WHERE id = :id")
    suspend fun getByIdSuspend(id: Long): TripWithTracks

    @Query("SELECT * FROM trip")
    fun getAll(): Flow<List<TripWithTracks>>
}
