package yong.jianwen.heatmap.ui

import yong.jianwen.heatmap.CurrentPage
import yong.jianwen.heatmap.data.Selectable
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.data.helper.CarAndModeForTrip

data class UiState(
    var currentPage: CurrentPage = CurrentPage.HOME,
    var allTripsWithTracks: List<TripWithTracks> = listOf(),
    val cars: List<Car> = listOf(),
    var carSelected: Car? = null,
    var modeSelected: TripMode? = null,
    var carsAndModesForEachTrip: List<CarAndModeForTrip> = listOf(),

    var newTripId: Long = -1,
    var newTrackId: Long = -1,
    var newTrackSegmentId: Long = -1,
    var isPaused: Boolean = false,

    var moreExpanded: Boolean = false,
    var bottomBarVisible: Boolean = true,
    var carExpanded: Boolean = false,
    var modeExpanded: Boolean = false,
    var deleteTripExpanded: Boolean = false,
    var tripIdToDelete: Long = -1,
    var alertExpanded: Boolean = false,

    var isUpdatingCarOrMode: Boolean = false,
    var updatingTrackId: Long = -1,
    var updatingCarOrModeSelected: Selectable? = null
)
