package yong.jianwen.heatmap.data.repository

import yong.jianwen.heatmap.data.dao.TrackDao
import yong.jianwen.heatmap.data.entity.Track
import kotlinx.coroutines.flow.Flow

class TrackRepository(
    private val trackDao: TrackDao
) {
    fun getAll(): Flow<List<Track>> {
        return trackDao.getAll()
    }

    suspend fun getLatestTrackNumberByTripId(tripId: Long): Int {
        return trackDao.getLatestTrackNumberByTripId(tripId)
    }

    suspend fun insert(track: Track): Long {
        return trackDao.insert(track)
    }

    suspend fun update(track: Track) {
        return trackDao.update(track)
    }

    suspend fun updateTrackNameById(id: Long, trackName: String) {
        return trackDao.updateTrackNameById(id, trackName)
    }

    suspend fun updateTrackCarIdById(id: Long, carId: Int) {
        return trackDao.updateTrackCarIdById(id, carId)
    }

    suspend fun updateTrackTypeById(id: Long, type: String) {
        return trackDao.updateTrackTypeById(id, type)
    }

    suspend fun updateTrackEndById(id: Long, end: String) {
        return trackDao.updateTrackEndById(id, end)
    }

    /*suspend fun delete(track: Track) {
        return trackDao.delete(track)
    }*/

    suspend fun delete(id: Long) {
        return trackDao.delete(id)
    }

    suspend fun deleteByTripId(tripId: Long) {
        return trackDao.deleteByTripId(tripId)
    }

    suspend fun cleanUpTrackNumberByTripId(tripId: Long) {
        return trackDao.cleanUpTrackNumberByTripId(tripId)
    }
}
