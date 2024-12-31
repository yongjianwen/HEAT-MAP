package com.example.heatmap.data.repository

import com.example.heatmap.data.dao.CarDao
import com.example.heatmap.data.entity.Car
import kotlinx.coroutines.flow.Flow

class CarRepository(
    private val carDao: CarDao
) {
    fun getAll(): Flow<List<Car>> {
        return carDao.getAll()
    }

    suspend fun getAllSuspend(): List<Car> {
        return carDao.getAllSuspend()
    }

    suspend fun getById(id: Int): Car? {
        return carDao.getById(id)
    }

    suspend fun insert(car: Car) {
        return carDao.insert(car)
    }

    suspend fun update(car: Car) {
        return carDao.update(car)
    }

    suspend fun delete(id: Int) {
        return carDao.delete(id)
    }
}
