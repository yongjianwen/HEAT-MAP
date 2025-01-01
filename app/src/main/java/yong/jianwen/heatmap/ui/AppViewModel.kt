package yong.jianwen.heatmap.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import yong.jianwen.heatmap.CurrentPage
import yong.jianwen.heatmap.GPXGenerator
import yong.jianwen.heatmap.HeatMapApplication
import yong.jianwen.heatmap.data.Selectable
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.data.entity.Track
import yong.jianwen.heatmap.data.entity.TrackSegment
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.data.helper.AdditionalTripInfo
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val carRepository: CarRepository,
    private val tripRepository: TripRepository,
    private val trackRepository: TrackRepository,
    private val trackSegmentRepository: TrackSegmentRepository,
    private val trackPointRepository: TrackPointRepository,
    private val tripWithTracksRepository: TripWithTracksRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _appUiState: MutableStateFlow<AppUiState> = MutableStateFlow(AppUiState())
    val appUiState: StateFlow<AppUiState> = _appUiState.asStateFlow()

//    val appUiState: StateFlow<AppUiState> =
//        combine(
//            tripRepository.getAll(),
//            carRepository.getAll()
//        ) { trips, cars -> AppUiState(cars = cars, trips = trips) }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//                initialValue = AppUiState()
//            )

    init {
        viewModelScope.launch {
            tripRepository.getAll().collect { trips ->
                _appUiState.update { it.copy(trips = trips) }
            }
        }
        viewModelScope.launch {
            carRepository.getAll().collect { cars ->
                _appUiState.update { it.copy(cars = cars) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.carId.collect { id ->
                _appUiState.update { it.copy(carSelected = carRepository.getById(id)) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.modeId.collect { id ->
                _appUiState.update {
                    it.copy(
                        modeSelected = TripMode.entries.find { tripType -> tripType.id == id }
                    )
                }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.tripId.collect { id ->
                _appUiState.update { it.copy(newTripId = id) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.trackId.collect { id ->
                _appUiState.update { it.copy(newTrackId = id) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.trackSegmentId.collect { id ->
                _appUiState.update { it.copy(newTrackSegmentId = id) }
            }
        }
        viewModelScope.launch {
            dataStoreRepository.isPaused.collect { flag ->
                _appUiState.update { it.copy(isPaused = flag) }
            }
        }
        viewModelScope.launch {
            tripRepository.getAdditionalTripInfo().collect { chips ->
                _appUiState.update { it.copy(chips = chips) }
            }
        }
        viewModelScope.launch {
            tripWithTracksRepository.getAll().collect {trips ->
                _appUiState.update { it.copy(allTrips = trips) }
            }
        }
    }

    fun editTrack(flag: Boolean, selectable: Selectable? = null, trackId: Long = -1) {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    isUpdatingCarOrMode = flag,
                    updatingCarOrModeSelected = selectable,
                    updatingTrackId = trackId
                )
            }
        }
    }

    fun saveCar(car: Car) {
        viewModelScope.launch {
            dataStoreRepository.saveCarIdPreference(car.id)
            _appUiState.update { it.copy(carSelected = car) }
        }
    }

    fun clearCar() {
        viewModelScope.launch {
            dataStoreRepository.saveCarIdPreference(-1)
            _appUiState.update { it.copy(carSelected = null) }
        }
    }

    fun updateCar(car: Car) {
        viewModelScope.launch {
            carRepository.update(car)
        }
    }

    fun saveMode(mode: TripMode) {
        viewModelScope.launch {
            dataStoreRepository.saveModeIdPreference(mode.id)
            _appUiState.update { it.copy(modeSelected = mode) }
        }
    }

    fun addNewCar(car: Car) {
        viewModelScope.launch {
            carRepository.insert(car)
        }
    }

    suspend fun deleteCar(id: Int): Boolean {
        /*viewModelScope.launch {
            try {
                carRepository.delete(id)
                if (appUiState.value.carSelected != null && appUiState.value.carSelected!!.id == id) {
                    clearCar()
                }
            } catch (e: Exception) {
                Log.d("TEST", "Cannot delete car")
                showAlertDialog()
            }
        }*/

        val res = viewModelScope.async {
            try {
                carRepository.delete(id)
                if (appUiState.value.carSelected != null && appUiState.value.carSelected!!.id == id) {
                    clearCar()
                }
                true
            } catch (e: Exception) {
                Log.d("TEST", "Cannot delete car")
//                showAlertDialog()
                false
            }
        }
        return res.await()
    }

    fun startNewTrip(newTripInfo: NewTripInfo) {
        val now = getCurrentDateTime()

        viewModelScope.launch {
            _appUiState.update {
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
            dataStoreRepository.saveTripIdPreference(appUiState.value.newTripId)
            dataStoreRepository.saveIsPausedPreference(false)

            _appUiState.update {
                it.copy(
                    newTrackId = trackRepository.insert(
                        Track(
                            id = 0,
                            tripId = appUiState.value.newTripId,
                            type = newTripInfo.tripMode.getDisplayName(),
                            name = generateTrackName(
                                newTripInfo.trackName,
                                newTripInfo.tripMode,
                                newTripInfo.car
                            ),
                            number = 1,
                            start = now,
                            end = "",
                            carId = appUiState.value.carSelected!!.id
                        )
                    )
                )
            }
            dataStoreRepository.saveTrackIdPreference(appUiState.value.newTrackId)

            _appUiState.update {
                it.copy(
                    newTrackSegmentId = trackSegmentRepository.insert(
                        TrackSegment(
                            id = 0,
                            trackId = appUiState.value.newTrackId,
                            number = 1
                        )
                    )
                )
            }
            dataStoreRepository.saveTrackSegmentIdPreference(appUiState.value.newTrackSegmentId)
        }
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

    fun updateTrackCarIdById(id: Long, carId: Int) {
        viewModelScope.launch {
            trackRepository.updateTrackCarIdById(id, carId)
        }
    }

    fun updateTrackTypeById(id: Long, tripMode: TripMode) {
        viewModelScope.launch {
            trackRepository.updateTrackTypeById(id, tripMode.getDisplayName())
        }
    }

    private fun resetPreferences() {
        viewModelScope.launch {
            dataStoreRepository.resetPreferences()
        }
    }

    fun pauseTrip() {
        viewModelScope.launch {
            _appUiState.update { it.copy(isPaused = true) }
            dataStoreRepository.saveIsPausedPreference(true)
            trackRepository.updateTrackEndById(appUiState.value.newTrackId, getCurrentDateTime())
        }
    }

    fun endTrip() {
        pauseTrip()
        viewModelScope.launch {
            tripRepository.updateTripEndById(appUiState.value.newTripId, getCurrentDateTime())
            _appUiState.update { it.copy(isPaused = false) }
        }
        resetPreferences()
    }

    fun continueTrip(tripInfo: NewTripInfo/*tripMode: TripMode, tripId: Long*/) {
        val start = getCurrentDateTime()

        viewModelScope.launch {
            val trackNumber = trackRepository.getLatestTrackNumberByTripId(tripInfo.tripId)
            val trackId = trackRepository.insert(
                Track(
                    id = 0,
                    tripId = tripInfo.tripId,    // appUiState.value.newTripId
                    type = tripInfo.tripMode.getDisplayName(),
//                    name = tripMode.getDisplayName() + " on " + car.getDisplayName(),
                    name = generateTrackName(
                        tripInfo.trackName,
                        tripInfo.tripMode
                    ),
                    number = trackNumber + 1,
                    start = start,
                    end = "",
                    carId = appUiState.value.carSelected!!.id
                )
            )
            _appUiState.update {
                it.copy(
                    isPaused = false,
                    newTrackId = trackId
                )
            }
            dataStoreRepository.saveTripIdPreference(tripInfo.tripId)
            dataStoreRepository.saveTrackIdPreference(appUiState.value.newTrackId)
            dataStoreRepository.saveIsPausedPreference(false)

            _appUiState.update {
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
            dataStoreRepository.saveTrackSegmentIdPreference(appUiState.value.newTrackSegmentId)
        }
    }

    fun getTripWithTracksById(tripId: Long): Flow<TripWithTracks?> {
        return tripWithTracksRepository.getById(tripId)
    }

    fun getAdditionalTripInfo(): Flow<List<AdditionalTripInfo>> {
        return tripRepository.getAdditionalTripInfo()
    }

    fun generateGPX() {
        viewModelScope.launch {
            val res = yong.jianwen.heatmap.GPXGenerator.generate(tripWithTracksRepository.getByIdSuspend(31))
            Log.d("TEST", res)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun generateGPXByTripId(tripId: Long): String {
        viewModelScope.launch {
            _appUiState.update { it.copy(moreExpanded = true) }
            d("TESTTEST", yong.jianwen.heatmap.GPXGenerator.generate(tripWithTracksRepository.getByIdSuspend(tripId)))
        }

        val res = viewModelScope.async {
            yong.jianwen.heatmap.GPXGenerator.generate(tripWithTracksRepository.getByIdSuspend(tripId))
        }
        return res.await()
        /*res.invokeOnCompletion {
            if (it == null) {
                return@invokeOnCompletion res.getCompleted()
            }
        }*/
    }

    fun showMoreMenu() {
        viewModelScope.launch {
            _appUiState.update { it.copy(moreExpanded = true) }
        }
    }

    fun hideMoreMenu() {
        viewModelScope.launch {
            _appUiState.update { it.copy(moreExpanded = false) }
        }
    }

    fun showDeleteTripDialog(trip: Trip) {
        // TODO: other trip fields
        _appUiState.update { it.copy(tripIdToDelete = trip.id, deleteTripExpanded = true) }
    }

    fun hideDeleteTripDialog() {
        _appUiState.update { it.copy(tripIdToDelete = -1, deleteTripExpanded = false) }
    }

    fun showCarDialog() {
        _appUiState.update { it.copy(carExpanded = true) }
    }

    fun hideCarDialog() {
        _appUiState.update { it.copy(carExpanded = false) }
    }

    fun showModeDialog() {
        _appUiState.update { it.copy(modeExpanded = true) }
    }

    fun hideModeDialog() {
        _appUiState.update { it.copy(modeExpanded = false) }
    }

    fun showAlertDialog() {
        _appUiState.update { it.copy(alertExpanded = true) }
    }

    fun hideAlertDialog() {
        _appUiState.update { it.copy(alertExpanded = false) }
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

data class AppUiState(
    val cars: List<Car> = listOf(),
    val trips: List<Trip> = listOf(),
    var currentPage: yong.jianwen.heatmap.CurrentPage = yong.jianwen.heatmap.CurrentPage.HOME,
    var carSelected: Car? = null,
    var modeSelected: TripMode? = null,
    var newTripId: Long = -1,
    var newTrackId: Long = -1,
    var newTrackSegmentId: Long = -1,
    var isPaused: Boolean = false,
    var tripIdToDelete: Long = -1,
    var deleteTripExpanded: Boolean = false,
    var carExpanded: Boolean = false,
    var modeExpanded: Boolean = false,
    var isUpdatingCarOrMode: Boolean = false,
    var updatingCarOrModeSelected: Selectable? = null,
    var updatingTrackId: Long = -1,
    var moreExpanded: Boolean = false,
    var alertExpanded: Boolean = false,
    var chips: List<AdditionalTripInfo> = listOf(),
    var allTrips: List<TripWithTracks> = listOf()
)
