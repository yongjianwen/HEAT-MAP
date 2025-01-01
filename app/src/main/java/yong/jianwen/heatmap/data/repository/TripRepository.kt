package yong.jianwen.heatmap.data.repository

import yong.jianwen.heatmap.data.dao.TripDao
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.helper.AdditionalTripInfo
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao
) {
    fun getAll(): Flow<List<Trip>> {
        return tripDao.getAll()
    }

    suspend fun getAllSuspend(): List<Trip> {
        return tripDao.getAllSuspend()
    }

    fun getAdditionalTripInfo(): Flow<List<AdditionalTripInfo>> {
        return tripDao.getAdditionalTripInfo()
    }

    suspend fun insert(trip: Trip): Long {
        return tripDao.insert(trip)
    }

    suspend fun update(trip: Trip) {
        return tripDao.update(trip)
    }

    suspend fun updateTripNameById(id: Long, tripName: String) {
        return tripDao.updateTripNameById(id, tripName)
    }

    suspend fun updateTripEndById(id: Long, end: String) {
        return tripDao.updateTripEndById(id, end)
    }

    suspend fun delete(id: Long) {
        return tripDao.delete(id)
    }
}
