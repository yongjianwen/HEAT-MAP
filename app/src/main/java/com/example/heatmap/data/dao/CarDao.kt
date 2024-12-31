package com.example.heatmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.heatmap.data.entity.Car
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM car")
    fun getAll(): Flow<List<Car>>

    @Query("SELECT * FROM car")
    suspend fun getAllSuspend(): List<Car>

    @Query("SELECT * FROM car WHERE id = :id")
    suspend fun getById(id: Int): Car?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(car: Car)

    @Update
    suspend fun update(car: Car)

    @Query("DELETE FROM car WHERE id = :id")
    suspend fun delete(id: Int)
}
