package yong.jianwen.heatmap.data.repository

import yong.jianwen.heatmap.data.dao.TrackPointDao
import yong.jianwen.heatmap.data.entity.TrackPoint
import kotlinx.coroutines.flow.Flow

class TrackPointRepository(
    private val trackPointDao: TrackPointDao
) {
    fun getAll(): Flow<List<TrackPoint>> {
        return trackPointDao.getAll()
    }

    suspend fun insert(trackPoint: TrackPoint): Long {
        return trackPointDao.insert(trackPoint)
    }

    suspend fun update(trackPoint: TrackPoint) {
        return trackPointDao.update(trackPoint)
    }

    suspend fun delete(trackPoint: TrackPoint) {
        return trackPointDao.delete(trackPoint)
    }

    suspend fun deleteByTripId(tripId: Long) {
        return trackPointDao.deleteByTripId(tripId)
    }

    suspend fun deleteByTrackId(trackId: Long) {
        return trackPointDao.deleteByTrackId(trackId)
    }
}
