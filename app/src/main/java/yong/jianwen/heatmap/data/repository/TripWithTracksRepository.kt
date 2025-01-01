package yong.jianwen.heatmap.data.repository

import yong.jianwen.heatmap.data.dao.TripWithTracksDao
import yong.jianwen.heatmap.data.entity.TripWithTracks
import kotlinx.coroutines.flow.Flow

class TripWithTracksRepository(
    private val tripWithTracksDao: TripWithTracksDao
) {
    fun getById(id: Long): Flow<TripWithTracks?> {
        return tripWithTracksDao.getById(id)
    }

    suspend fun getByIdSuspend(id: Long): TripWithTracks {
        return tripWithTracksDao.getByIdSuspend(id)
    }

    fun getAll(): Flow<List<TripWithTracks>> {
        return tripWithTracksDao.getAll()
    }
}
