package yong.jianwen.heatmap.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import yong.jianwen.heatmap.GPXGenerator
import yong.jianwen.heatmap.HeatMapApplication
import yong.jianwen.heatmap.data.Selectable
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.data.entity.Track
import yong.jianwen.heatmap.data.entity.TrackPoint
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

    //region Update fields and delete Track in TripDetailScreen
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

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            trackPointRepository.deleteByTrackId(track.id)
        }
        viewModelScope.launch {
            trackSegmentRepository.deleteByTrackId(track.id)
        }
        viewModelScope.launch {
            trackRepository.delete(track.id)
        }
        viewModelScope.launch {
            trackRepository.cleanUpTrackNumberByTripId(track.tripId)
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
        _uiState.update { it.copy(tripToDelete = trip, deleteTripExpanded = true) }
    }

    fun hideDeleteTripDialog() {
        _uiState.update { it.copy(tripToDelete = null, deleteTripExpanded = false) }
    }

    fun showDeleteTrackDialog(track: Track) {
        _uiState.update { it.copy(trackToDelete = track, deleteTrackExpanded = true) }
    }

    fun hideDeleteTrackDialog() {
        _uiState.update { it.copy(trackToDelete = null, deleteTrackExpanded = false) }
    }

    fun showImportDialog() {
        _uiState.update { it.copy(importExpanded = true) }
    }

    fun hideImportDialog() {
        _uiState.update { it.copy(importExpanded = false) }
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

    //region Export and import data
    fun exportData(): String {
//        viewModelScope.launch {
        /*var trips: List<Trip> = listOf()
        var tracks: List<Track> = listOf()
        var trackSegments: List<TrackSegment> = listOf()
        var trackPoints: List<TrackPoint> = listOf()

        uiState.value.allTripsWithTracks.forEach { trip ->
            trips = trips.plus(trip.trip)
            trip.tracks.forEach { track ->
                tracks = tracks.plus(track.track)
                track.trackSegments.forEach { trackSegment ->
                    trackSegments = trackSegments.plus(trackSegment.trackSegment)
                    trackPoints = trackPoints.plus(trackSegment.trackPoints)
                }
            }
        }
        Log.d(
            "TEST",
            "trip: $trips"
        )*/

        val jsonElements = uiState.value.allTripsWithTracks.map {
            Json.encodeToJsonElement(TripWithTracks.serializer(), it)
        }
        val jsonArray = JsonArray(jsonElements)
        // TODO: check whether UUID will be changed when updating trip fields


        return jsonArray.toString()
//        }
    }

    fun updateImportData(json: JsonObject) {
        val carData: MutableList<Car> = mutableListOf()
        val tripData: MutableList<TripWithTracks> = mutableListOf()
        val carsStr = "cars"
        if (json.containsKey(carsStr)) {
            carData.addAll(Json.decodeFromJsonElement<List<Car>>(json[carsStr]!!))
        }
        if ("trips" in json) {
            tripData.addAll(Json.decodeFromJsonElement<List<TripWithTracks>>(json["trips"]!!))
        }

        val cars = _uiState.value.cars
        val carDiffs = carData.filter { c ->
            cars.none { car ->
                car.registrationNumber == c.registrationNumber && car.manufacturer == c.manufacturer && car.model == c.model
            }
        }
        val carIdMap = HashMap<Int, Int>()
        carData.forEach { c ->
            val firstMatch = cars.firstOrNull { car ->
                car.registrationNumber == c.registrationNumber && car.manufacturer == c.manufacturer && car.model == c.model
            }
            if (firstMatch != null) {
                carIdMap[c.id] = firstMatch.id
            }
        }

        val uuids = HashSet(_uiState.value.allTripsWithTracks.map { it.trip.uuid })
        val diffs = tripData.filter { !uuids.contains(it.trip.uuid) }

        _uiState.update {
            it.copy(
                importCarTotal = carData.size,
                importCarDiff = carDiffs,
                carIdMap = carIdMap,
                importDataTotal = tripData.size,
                importDataDiff = diffs
            )
        }
    }

    fun importData() {
        viewModelScope.launch {
            val carIdMap = _uiState.value.carIdMap
            _uiState.value.importCarDiff.forEach { car ->
                val carId = carRepository.insert(
                    Car(
                        id = 0,
                        registrationNumber = car.registrationNumber,
                        manufacturer = car.manufacturer,
                        model = car.model
                    )
                )
                carIdMap[car.id] = carId
            }

            _uiState.update { it.copy(carIdMap = carIdMap) }

//            delay(100)

            _uiState.value.importDataDiff.forEach { tripWithTracks ->
                val trip = tripWithTracks.trip
                val tripId = tripRepository.insert(
                    Trip(
                        id = 0,
                        name = trip.name,
                        start = trip.start,
                        end = trip.end,
                        uuid = trip.uuid
                    )
                )

                tripWithTracks.tracks.forEach { trackWithTrackSegments ->
                    val track = trackWithTrackSegments.track
                    Log.d("TEST", carIdMap.toString())
                    Log.d("TEST", track.carId.toString())
                    Log.d("TEST", carIdMap[track.carId].toString())
                    val trackId = trackRepository.insert(
                        Track(
                            id = 0,
                            tripId = tripId,
                            type = track.type,
                            name = track.name,
                            number = track.number,
                            start = track.start,
                            end = track.end,
                            carId = carIdMap[track.carId]
                                ?: track.carId     // TODO: -1 will never be encountered?
                        )
                    )

                    trackWithTrackSegments.trackSegments.forEach { trackSegmentWithTrackPoints ->
                        val trackSegment = trackSegmentWithTrackPoints.trackSegment
                        val trackSegmentId = trackSegmentRepository.insert(
                            TrackSegment(
                                id = 0,
                                trackId = trackId,
                                number = trackSegment.number
                            )
                        )

                        trackSegmentWithTrackPoints.trackPoints.forEach { trackPoint ->
                            trackPointRepository.insert(
                                TrackPoint(
                                    id = 0,
                                    trackSegmentId = trackSegmentId,
                                    latitude = trackPoint.latitude,
                                    longitude = trackPoint.longitude,
                                    elevation = trackPoint.elevation,
                                    time = trackPoint.time
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun resetAllUUIDs() {
        viewModelScope.launch {
            tripRepository.updateAllTripUUIDs(uiState.value.allTripsWithTracks.map { it.trip.id })
        }
    }
    //endregion

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
