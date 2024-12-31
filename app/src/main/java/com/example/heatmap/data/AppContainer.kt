package com.example.heatmap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.heatmap.data.repository.CarRepository
import com.example.heatmap.data.repository.DataStoreRepository
import com.example.heatmap.data.repository.TrackPointRepository
import com.example.heatmap.data.repository.TrackRepository
import com.example.heatmap.data.repository.TrackSegmentRepository
import com.example.heatmap.data.repository.TripRepository
import com.example.heatmap.data.repository.TripWithTracksRepository

interface AppContainer {
    val carRepository: CarRepository
    val tripRepository: TripRepository
    val trackRepository: TrackRepository
    val trackSegmentRepository: TrackSegmentRepository
    val trackPointRepository: TrackPointRepository
    val tripWithTracksRepository: TripWithTracksRepository
    val dataStoreRepository: DataStoreRepository
}

private const val APP_PREFERENCE_NAME = "heat_map_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_PREFERENCE_NAME
)

class AppDataContainer(private val context: Context) : AppContainer {
    override val carRepository: CarRepository by lazy {
        CarRepository(AppDatabase.getDatabase(context).carDao())
    }

    override val tripRepository: TripRepository by lazy {
        TripRepository(AppDatabase.getDatabase(context).tripDao())
    }

    override val trackRepository: TrackRepository by lazy {
        TrackRepository(AppDatabase.getDatabase(context).trackDao())
    }

    override val trackSegmentRepository: TrackSegmentRepository by lazy {
        TrackSegmentRepository(AppDatabase.getDatabase(context).trackSegmentDao())
    }

    override val trackPointRepository: TrackPointRepository by lazy {
        TrackPointRepository(AppDatabase.getDatabase(context).trackPointDao())
    }

    override val tripWithTracksRepository: TripWithTracksRepository by lazy {
        TripWithTracksRepository(AppDatabase.getDatabase(context).tripWithTracksDao())
    }

    override val dataStoreRepository: DataStoreRepository by lazy {
        DataStoreRepository(context.dataStore)
    }
}
