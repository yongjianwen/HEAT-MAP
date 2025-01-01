package yong.jianwen.heatmap.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {

    private companion object {
        const val TAG = "DataStoreRepository"
        val CAR_ID = intPreferencesKey("car_id")
        val MODE_ID = intPreferencesKey("mode_id")
        val TRIP_ID = longPreferencesKey("trip_id")
        val TRACK_ID = longPreferencesKey("track_id")
        val TRACK_SEGMENT_ID = longPreferencesKey("track_segment_id")
        val IS_PAUSED = booleanPreferencesKey("is_paused")
    }

    val carId: Flow<Int> = dataStore.data
        .catch { handleException(this, it) }
        .map { it[CAR_ID] ?: -1 }
    val modeId: Flow<Int> = dataStore.data
        .catch { handleException(this, it) }
        .map { it[MODE_ID] ?: -1 }
    val tripId: Flow<Long> = dataStore.data
        .catch { handleException(this, it) }
        .map { it[TRIP_ID] ?: -1 }
    val trackId: Flow<Long> = dataStore.data
        .catch { handleException(this, it) }
        .map { it[TRACK_ID] ?: -1 }
    val trackSegmentId: Flow<Long> = dataStore.data
        .catch { handleException(this, it) }
        .map { it[TRACK_SEGMENT_ID] ?: -1 }
    val isPaused: Flow<Boolean> = dataStore.data
        .catch { handleException(this, it) }
        .map { it[IS_PAUSED] ?: false }

    suspend fun saveCarIdPreference(carId: Int) {
        dataStore.edit { it[CAR_ID] = carId }
    }

    suspend fun saveModeIdPreference(modeId: Int) {
        dataStore.edit { it[MODE_ID] = modeId }
    }

    suspend fun saveTripIdPreference(tripId: Long) {
        dataStore.edit { it[TRIP_ID] = tripId }
    }

    suspend fun saveTrackIdPreference(trackId: Long) {
        dataStore.edit { it[TRACK_ID] = trackId }
    }

    suspend fun saveTrackSegmentIdPreference(trackSegmentId: Long) {
        dataStore.edit { it[TRACK_SEGMENT_ID] = trackSegmentId }
    }

    suspend fun saveIsPausedPreference(isPaused: Boolean) {
        dataStore.edit { it[IS_PAUSED] = isPaused }
    }

    suspend fun resetPreferences() {
        dataStore.edit {
            it[TRIP_ID] = -1
            it[TRACK_ID] = -1
            it[TRACK_SEGMENT_ID] = -1
            it[IS_PAUSED] = false
        }
    }

    private suspend fun handleException(
        collector: FlowCollector<Preferences>,
        throwable: Throwable
    ) {
        if (throwable is IOException) {
            Log.e(TAG, "Error reading preferences.", throwable)
            collector.emit(emptyPreferences())
        } else {
            throw throwable
        }
    }
}
