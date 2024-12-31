package com.example.heatmap.data.repository

import com.example.heatmap.data.dao.TrackSegmentDao
import com.example.heatmap.data.entity.TrackSegment
import kotlinx.coroutines.flow.Flow

class TrackSegmentRepository(
    private val trackSegmentDao: TrackSegmentDao
) {
    fun getAll(): Flow<List<TrackSegment>> {
        return trackSegmentDao.getAll()
    }

    suspend fun insert(trackSegment: TrackSegment): Long {
        return trackSegmentDao.insert(trackSegment)
    }

    suspend fun update(trackSegment: TrackSegment) {
        return trackSegmentDao.update(trackSegment)
    }

    suspend fun delete(trackSegment: TrackSegment) {
        return trackSegmentDao.delete(trackSegment)
    }

    suspend fun deleteByTripId(tripId: Long) {
        return trackSegmentDao.deleteByTripId(tripId)
    }
}
