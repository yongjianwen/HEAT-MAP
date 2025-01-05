package yong.jianwen.heatmap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yong.jianwen.heatmap.GPXGenerator
import yong.jianwen.heatmap.HeatMapApplication
import yong.jianwen.heatmap.data.Selectable
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.data.entity.Track
import yong.jianwen.heatmap.data.entity.TrackSegment
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.data.helper.DebugUtilis.d
import yong.jianwen.heatmap.data.helper.NewTripInfo
import yong.jianwen.heatmap.data.repository.CarRepository
import yong.jianwen.heatmap.data.repository.DataStoreRepository
import yong.jianwen.heatmap.data.repository.TrackPointRepository
import yong.jianwen.heatmap.data.repository.TrackRepository
import yong.jianwen.heatmap.data.repository.TrackSegmentRepository
import yong.jianwen.heatmap.data.repository.TripRepository
import yong.jianwen.heatmap.data.repository.TripWithTracksRepository
import yong.jianwen.heatmap.generateTrackName
import yong.jianwen.heatmap.generateTripName
import yong.jianwen.heatmap.getCurrentDateTime

class AppViewModel(
    private val carRepository: CarRepository,
    private val tripRepository: TripRepository,
    private val trackRepository: TrackRepository,
    private val trackSegmentRepository: TrackSegmentRepository,
    private val trackPointRepository: TrackPointRepository,
    private val tripWithTracksRepository: TripWithTracksRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tripWithTracksRepository.getAll().collect { trips ->
                _uiState.update { it.copy(allTripsWithTracks = trips) }
            }
        }
        viewModelScope.launch {
            carRepository.getAll().collect { cars ->
                _uiState.update { it.copy(cars = cars) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.carId.collect { id ->
                _uiState.update { it.copy(carSelected = carRepository.getById(id)) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.modeId.collect { id ->
                _uiState.update {
                    it.copy(
                        modeSelected = TripMode.entries.find { tripType -> tripType.id == id }
                    )
                }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.tripId.collect { id ->
                _uiState.update { it.copy(newTripId = id) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.trackId.collect { id ->
                _uiState.update { it.copy(newTrackId = id) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.trackSegmentId.collect { id ->
                _uiState.update { it.copy(newTrackSegmentId = id) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.isPaused.collect { flag ->
                _uiState.update { it.copy(isPaused = flag) }
            }
        }
        viewModelScope.launch {
            tripRepository.getCarsAndModesForEachTrip().collect { carsAndModesForTrips ->
                _uiState.update { it.copy(carsAndModesForEachTrip = carsAndModesForTrips) }
            }
        }
    }

    //region Start, pause, continue, end and delete Trip
    fun startTrip(newTripInfo: NewTripInfo) {
        val now = getCurrentDateTime()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    newTripId = tripRepository.insert(
                        Trip(
                            id = 0,
                            name = generateTripName(newTripInfo.tripName),
                            start = now,
                            end = ""
                        )
                    ),
                    isPaused = false,
                )
            }
            dataStoreRepository.saveTripIdPreference(uiState.value.newTripId)
            dataStoreRepository.saveIsPausedPreference(false)

            _uiState.update {
                it.copy(
                    newTrackId = trackRepository.insert(
                        Track(
                            id = 0,
                            tripId = uiState.value.newTripId,
                            type = newTripInfo.tripMode.getDisplayName(),
                            name = generateTrackName(
                                newTripInfo.trackName,
                                newTripInfo.tripMode,
                                newTripInfo.car
                            ),
                            number = 1,
                            start = now,
                            end = "",
                            carId = uiState.value.carSelected!!.id
                        )
                    )
                )
            }
            dataStoreRepository.saveTrackIdPreference(uiState.value.newTrackId)

            _uiState.update {
                it.copy(
                    newTrackSegmentId = trackSegmentRepository.insert(
                        TrackSegment(
                            id = 0,
                            trackId = uiState.value.newTrackId,
                            number = 1
                        )
                    )
                )
            }
            dataStoreRepository.saveTrackSegmentIdPreference(uiState.value.newTrackSegmentId)
        }
    }

    fun pauseTrip() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPaused = true) }
            dataStoreRepository.saveIsPausedPreference(true)
            trackRepository.updateTrackEndById(uiState.value.newTrackId, getCurrentDateTime())
        }
    }

    fun continueTrip(tripInfo: NewTripInfo) {
        val start = getCurrentDateTime()

        viewModelScope.launch {
            val trackNumber = trackRepository.getLatestTrackNumberByTripId(tripInfo.tripId)
            val trackId = trackRepository.insert(
                Track(
                    id = 0,
                    tripId = tripInfo.tripId,
                    type = tripInfo.tripMode.getDisplayName(),
                    name = generateTrackName(
                        tripInfo.trackName,
                        tripInfo.tripMode
                    ),
                    number = trackNumber + 1,
                    start = start,
                    end = "",
                    carId = uiState.value.carSelected!!.id
                )
            )
            _uiState.update {
                it.copy(
                    isPaused = false,
                    newTrackId = trackId
                )
            }
            dataStoreRepository.saveTripIdPreference(tripInfo.tripId)
            dataStoreRepository.saveTrackIdPreference(uiState.value.newTrackId)
            dataStoreRepository.saveIsPausedPreference(false)

            _uiState.update {
                it.copy(
                    newTrackSegmentId = trackSegmentRepository.insert(
                        TrackSegment(
                            id = 0,
                            trackId = trackId,  // cannot use appUiState.value.newTrackId
                            number = 1
                        )
                    )
                )
            }
            dataStoreRepository.saveTrackSegmentIdPreference(uiState.value.newTrackSegmentId)
        }
    }

    fun endTrip() {
        pauseTrip()
        viewModelScope.launch {
            tripRepository.updateTripEndById(uiState.value.newTripId, getCurrentDateTime())
            _uiState.update {
                it.copy(
                    newTripId = -1,
                    newTrackId = -1,
                    newTrackSegmentId = -1,
                    isPaused = false
                )
            }
        }
        resetPreferences()
    }

    fun deleteTrip(id: Long) {
        viewModelScope.launch {
            trackPointRepository.deleteByTripId(id)
        }
        viewModelScope.launch {
            trackSegmentRepository.deleteByTripId(id)
        }
        viewModelScope.launch {
            trackRepository.deleteByTripId(id)
        }
        viewModelScope.launch {
            tripRepository.delete(id)
        }
    }
    //endregion

    //region Create, update and delete Car
    fun createCar(car: Car) {
        viewModelScope.launch {
            carRepository.insert(car)
        }
    }

    fun updateCar(car: Car) {
        viewModelScope.launch {
            carRepository.update(car)
        }
    }

    suspend fun deleteCar(id: Int): Boolean {
        val res = viewModelScope.async {
            try {
                carRepository.delete(id)
                if (uiState.value.carSelected != null && uiState.value.carSelected!!.id == id) {
                    clearCarSelected()
                }
                true
            } catch (e: Exception) {
                false
            }
        }
        return res.await()
    }
    //endregion

    //region Save, clear and reset preferences
    fun saveCarSelected(car: Car) {
        viewModelScope.launch {
            dataStoreRepository.saveCarIdPreference(car.id)
            _uiState.update { it.copy(carSelected = car) }
        }
    }

    private fun clearCarSelected() {
        viewModelScope.launch {
            dataStoreRepository.saveCarIdPreference(-1)
            _uiState.update { it.copy(carSelected = null) }
        }
    }

    fun saveMode(mode: TripMode) {
        viewModelScope.launch {
            dataStoreRepository.saveModeIdPreference(mode.id)
            _uiState.update { it.copy(modeSelected = mode) }
        }
    }

    private fun resetPreferences() {
        viewModelScope.launch {
            dataStoreRepository.resetPreferences()
        }
    }
    //endregion

    //region Entering and exiting TripDetailScreen
    fun getTripWithTracksById(tripId: Long): Flow<TripWithTracks?> {
        return tripWithTracksRepository.getById(tripId)
    }

    fun editTrack(flag: Boolean, selectable: Selectable? = null, trackId: Long = -1) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingCarOrMode = flag,
                    updatingCarOrModeSelected = selectable,
                    updatingTrackId = trackId
                )
            }
        }
    }
    //endregion

    //region Update fields in TripDetailScreen
    fun updateTripNameById(id: Long, tripName: String) {
        viewModelScope.launch {
            tripRepository.updateTripNameById(id, tripName)
        }
    }

    fun updateTrackNameById(id: Long, trackName: String) {
        viewModelScope.launch {
            trackRepository.updateTrackNameById(id, trackName)
        }
    }

    fun updateTrackTypeById(id: Long, tripMode: TripMode) {
        viewModelScope.launch {
            trackRepository.updateTrackTypeById(id, tripMode.getDisplayName())
        }
    }

    fun updateTrackCarIdById(id: Long, carId: Int) {
        viewModelScope.launch {
            trackRepository.updateTrackCarIdById(id, carId)
        }
    }
    //endregion

    //region Show and hide menu, bottom bar and dialogs
    fun showMoreMenu() {
        _uiState.update { it.copy(moreExpanded = true) }
    }

    fun hideMoreMenu() {
        _uiState.update { it.copy(moreExpanded = false) }
    }

    fun showBottomBar() {
        _uiState.update { it.copy(bottomBarVisible = true) }
    }

    fun hideBottomBar() {
        _uiState.update { it.copy(bottomBarVisible = false) }
    }

    fun showCarDialog() {
        _uiState.update { it.copy(carExpanded = true) }
    }

    fun hideCarDialog() {
        _uiState.update { it.copy(carExpanded = false) }
    }

    fun showModeDialog() {
        _uiState.update { it.copy(modeExpanded = true) }
    }

    fun hideModeDialog() {
        _uiState.update { it.copy(modeExpanded = false) }
    }

    fun showDeleteTripDialog(trip: Trip) {
        _uiState.update { it.copy(tripIdToDelete = trip.id, deleteTripExpanded = true) }
    }

    fun hideDeleteTripDialog() {
        _uiState.update { it.copy(tripIdToDelete = -1, deleteTripExpanded = false) }
    }

    fun hideAlertDialog() {
        _uiState.update { it.copy(alertExpanded = false) }
    }
    //endregion

    suspend fun generateGPXByTripId(tripId: Long): String {
        viewModelScope.launch {
            _uiState.update { it.copy(moreExpanded = true) }
            d("TEST", GPXGenerator.generate(tripWithTracksRepository.getByIdSuspend(tripId)))
        }

        val res = viewModelScope.async {
            GPXGenerator.generate(tripWithTracksRepository.getByIdSuspend(tripId))
        }
        return res.await()
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HeatMapApplication)
                AppViewModel(
                    application.container.carRepository,
                    application.container.tripRepository,
                    application.container.trackRepository,
                    application.container.trackSegmentRepository,
                    application.container.trackPointRepository,
                    application.container.tripWithTracksRepository,
                    application.container.dataStoreRepository
                )
            }
        }
    }
}
