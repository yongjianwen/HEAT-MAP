package yong.jianwen.heatmap.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.coroutine.awaitCameraForCoordinates
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import kotlinx.coroutines.launch
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.entity.Track
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.data.helper.UpdateTrack
import yong.jianwen.heatmap.data.helper.UpdateTrip
import yong.jianwen.heatmap.formatTrackStartEndTimes
import yong.jianwen.heatmap.formatTripStartEndTimes
import yong.jianwen.heatmap.local.DataSource
import yong.jianwen.heatmap.ui.component.FourButtons
import yong.jianwen.heatmap.ui.component.MyChip
import yong.jianwen.heatmap.ui.theme.HeatMapTheme
import yong.jianwen.heatmap.ui.theme.NotoSans
import yong.jianwen.heatmap.ui.theme.labelMediumSmall

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripDetailScreen(
    uiState: UiState,
    tripWithTracksOrNothing: TripWithTracks?,
    onTrackPointClicked: (String) -> Unit,
    onPauseTrip: () -> Unit,
    onContinueTrip: (tripId: Long) -> Unit,
    onEndTrip: () -> Unit,
    onUpdateTrip: (trip: UpdateTrip) -> Unit,
    onUpdateTrack: (track: UpdateTrack) -> Unit,
    onDeleteClicked: (trip: Trip) -> Unit,
    onChooseVehicleClicked: (track: Track) -> Unit,
    onChooseModeClicked: (track: Track) -> Unit,
    onBack: () -> Unit,
    onMoreClicked: () -> Unit,
    onMoreDismissed: () -> Unit,
    onMoreItem1Clicked: (tripId: Long) -> Unit,
    onDeleteTrackClicked: (track: Track) -> Unit,
    onMapClicked: (Long) -> Unit
) {
    val tripWithTracks = tripWithTracksOrNothing ?: TripWithTracks(
        trip = Trip(
            id = 0,
            name = "",
            start = "",
            end = ""
        ),
        tracks = emptyList()
    )

    BackHandler {
        onBack()
    }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var renameExpanded by rememberSaveable { mutableStateOf(false) }
    var sectionExpanded by rememberSaveable { mutableStateOf(false) }

    var screen by rememberSaveable { mutableIntStateOf(0) }

    var tripOrTrackName by rememberSaveable { mutableStateOf("") }
    var trackId by rememberSaveable { mutableLongStateOf(-1) }

    val points = tripWithTracks.tracks.flatMap { track ->
        track.trackSegments.flatMap { trackSegment ->
            trackSegment.trackPoints.map { trackPoint ->
                Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
            }
        }
    }

    var latitudeSelected by remember { mutableDoubleStateOf(-1.0) }
    var longitudeSelected by remember { mutableDoubleStateOf(-1.0) }

    var toolVisible by remember { mutableStateOf(false) }

    if (renameExpanded) {
        RenameDialog(
            onDismissRequest = { renameExpanded = false },
            screen = screen,
            tripWithTracks = tripWithTracks,
            tripOrTrackName = tripOrTrackName,
            trackId = trackId,
            onUpdateTrip = onUpdateTrip,
            onUpdateTrack = onUpdateTrack,
            onValueChanged = { tripOrTrackName = it }
        )
    }

    Scaffold(
        topBar = {
            HeatMapAppBar(
                title = stringResource(R.string.trip_detail_screen_title),
                onAppBarClicked = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(tripWithTracks.tracks.size - 1)
                    }
                },
                showBackButton = true,
                showMoreButton = true,
                onBackClicked = { onBack() },
                onMoreClicked = onMoreClicked,
                isDone = !toolVisible,
                onDoneClicked = {
                    toolVisible = false
                }
            ) {
                DropdownMenu(
                    expanded = uiState.moreExpanded,
                    onDismissRequest = onMoreDismissed,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_medium_corner_radius))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.copy_gpx),
                                fontFamily = NotoSans
                            )
                        },
                        onClick = { onMoreItem1Clicked(tripWithTracks.trip.id) }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.manage_tracks),
                                fontFamily = NotoSans
                            )
                        },
                        onClick = {
                            toolVisible = !toolVisible
                            onMoreDismissed()
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(1)
                            }
                            sectionExpanded = false
                        }
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            val mapViewportState = rememberMapViewportState() {
                setCameraOptions {
                    zoom(12.0)
//                                center(Point.fromLngLat(102.23142684950145, 2.2432788267098336))
                    pitch(0.0)
                    bearing(0.0)
                }
            }
            Box {
                MapboxMap(
                    mapViewportState = mapViewportState,
                    mapState = rememberMapState {
                        gesturesSettings = GesturesSettings {
                            scrollEnabled = false
                            pinchToZoomEnabled = false
                            rotateEnabled = false
                            pitchEnabled = false
                            doubleTapToZoomInEnabled = false
                            doubleTouchToZoomOutEnabled = false
                            quickZoomEnabled = false
                            pinchToZoomDecelerationEnabled = false
                            rotateDecelerationEnabled = false
                            scrollDecelerationEnabled = false
                            increaseRotateThresholdWhenPinchingToZoom = false
                            increasePinchToZoomThresholdWhenRotating = false
                            pinchScrollEnabled = false
                        }
                    },
                    compass = { },
                    scaleBar = { },
                    logo = { },
                    attribution = { },
                    style = {
                        if (isSystemInDarkTheme()) {
                            GenericStyle(style = Style.DARK)
                        } else {
                            GenericStyle(style = Style.OUTDOORS)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
//                        .height((250 * 1.8).dp)
                        .height(200.dp)
//                        .clickable { onMapClicked(tripWithTracks.trip.id) }
                ) {
                    // Get reference to the raw MapView using MapEffect
                    if (tripWithTracksOrNothing != null && tripWithTracksOrNothing.tracks.isNotEmpty()) {
                        MapEffect(Unit) { mapView ->
                            // Use mapView to access the Mapbox Maps APIs not in the Compose extension.
                            // Changes inside `MapEffect` may conflict with Compose states.
                            var northernmost2 = -90.0
                            var southernmost2 = 90.0
                            var easternmost2 = -180.0
                            var westernmost2 = 180.0
                            points.forEach { point ->
                                northernmost2 =
                                    Math.max(northernmost2, point.latitude())
                                southernmost2 =
                                    Math.min(southernmost2, point.latitude())
                                easternmost2 = Math.max(easternmost2, point.longitude())
                                westernmost2 = Math.min(westernmost2, point.longitude())
                            }
                            val cornerPoints = listOf(
                                Point.fromLngLat(westernmost2, southernmost2),
                                Point.fromLngLat(westernmost2, northernmost2),
                                Point.fromLngLat(easternmost2, northernmost2),
                                Point.fromLngLat(easternmost2, southernmost2)
                            )

                            val cameraPosition =
                                mapView.mapboxMap.awaitCameraForCoordinates(
                                    cornerPoints,
                                    cameraOptions { },
                                    EdgeInsets(150.0, 150.0, 150.0, 150.0)
                                )
                            mapViewportState.setCameraOptions(cameraPosition)

                            val cameraBoundsOptions = CameraBoundsOptions.Builder()
//                                        .maxZoom(15.0)
//                                    .bounds(
//                                        CoordinateBounds(
//                                            Point.fromLngLat(westernmost1, southernmost1),
//                                            Point.fromLngLat(easternmost1, northernmost1),
//                                            false
//                                        )
//                                    )
//                                    .minZoom(2.0) // Set a minimum zoom level
                                .build()
                            mapView.mapboxMap.setBounds(cameraBoundsOptions)
                        }
                    }
                    val colors =
                        arrayOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Blue,
                            Color.Magenta
                        )
                    tripWithTracks.tracks.forEachIndexed { index, track ->
                        PolylineAnnotation(
                            points = track.trackSegments
                                .flatMap { trackSegment -> trackSegment.trackPoints }
                                .map { trackPoint ->
                                    Point.fromLngLat(
                                        trackPoint.longitude,
                                        trackPoint.latitude
                                    )
                                }
                        ) {
                            lineColor = colors[index % 5]
                            lineWidth = 2.0
                        }
                    }

                    val marker = rememberIconImage(R.drawable.map)
//                            points.forEach { point ->
////                                PointAnnotation(
////                                    point = point
////                                ) {
////                                    iconImage = marker
////                                }
//                                CircleAnnotation(point = point) {
//                                    circleColor = Color.Blue
//                                    circleRadius = 1.0
//                                }
//                            }

                    PointAnnotation(
                        point = Point.fromLngLat(
                            longitudeSelected,
                            latitudeSelected
                        )
                    ) {
                        iconImage = marker
                    }

                    /*ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(Point.fromLngLat(longitudeSelected, latitudeSelected))
                            annotationAnchor {
                                anchor(ViewAnnotationAnchor.BOTTOM)
                            }
                            allowOverlap(false)
                        }
                    ) {
                        Button(
                            onClick = {
                            },
                            colors = ButtonDefaults.buttonColors(
                                Color.Red
                            ),
                        ) {
                            Text(
                                "Click me"
                            )
                        }
                    }*/
                }
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .clickable { onMapClicked(tripWithTracks.trip.id) }
                        .fillMaxWidth()
                        .height(200.dp)
                ) {

                }
            }
//            Spacer(
//                modifier = Modifier.height(dimensionResource(R.dimen.card_medium_content_padding))
//            )
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .padding(
                        start = it.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                        end = it.calculateEndPadding(layoutDirection = LayoutDirection.Ltr)
                    )
                    .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal))
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.card_medium_content_padding))
                    ) {
                        Text(
                            text = String.format(
                                stringResource(R.string.trip_id_which),
                                tripWithTracks.trip.id
                            ),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Surface(
                            shape = RoundedCornerShape(dimensionResource(R.dimen.chip_corner_radius))
                        ) {
                            Text(
                                text = tripWithTracks.trip.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .clickable {
                                        tripOrTrackName = tripWithTracks.trip.name
                                        renameExpanded = true
                                        screen = 0
                                    }
                                    .padding(4.dp)
                            )
                        }
                        Text(
                            text = formatTripStartEndTimes(
                                tripWithTracks.trip.start,
                                tripWithTracks.trip.end,
                                "YYYY/MM/dd h:mma",
                                "h:mma"
                            ),
                            style = labelMediumSmall,
                            modifier = Modifier
                                .padding(vertical = dimensionResource(R.dimen.card_inner_vertical_padding))
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_separation)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_separation)),
                            modifier = Modifier
                                .padding(top = dimensionResource(R.dimen.card_inner_vertical_padding))
                        ) {
                            val filteredChips =
                                uiState.carsAndModesForEachTrip.filter { chip -> tripWithTracks.trip.id == chip.tripId }
                            filteredChips.forEach { chip ->
                                MyChip(
                                    label1 = chip.mode,
                                    label2 = chip.car.getDisplayName()
                                )
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.card_medium_content_padding))
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FourButtons(
                                uiState = uiState,
                                trip = tripWithTracks.trip,
                                onContinueTrip = onContinueTrip,
                                onPauseTrip = onPauseTrip,
                                onEndTrip = onEndTrip,
                                onDeleteClicked = onDeleteClicked,
                                modifier = Modifier.width(120.dp)
                            )
                        }
//                        Spacer(
//                            modifier = Modifier.height(dimensionResource(R.dimen.card_medium_content_padding))
//                        )
//                        HorizontalDivider()
                    }
                }
                itemsIndexed(
                    items = tripWithTracks.tracks.reversed(),
                    //                key = { eachTrack -> eachTrack.track.id }
                ) { index, track ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.card_medium_content_padding))
                    ) {
                        HorizontalDivider()
                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                //                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = String.format("Track %s", track.track.number)
                                            + "  •  "
                                            /*+ formatTrackStartEndTimes(
                                        tripWithTracks.trip.start,
                                        tripWithTracks.trip.end,
                                        track.track.start,
                                        track.track.end,
                                        "h:mma",
                                        stringResource(R.string.day_which)
                                    )
                                            + "  •  "*/
                                            + String.format(
                                        stringResource(R.string.track_points_how_many),
                                        track.trackSegments.flatMap { trackSegment -> trackSegment.trackPoints }.size
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = formatTrackStartEndTimes(
                                        tripWithTracks.trip.start,
                                        tripWithTracks.trip.end,
                                        track.track.start,
                                        track.track.end,
                                        "h:mma",
                                        stringResource(R.string.day_which)
                                    ),
                                    style = labelMediumSmall
//                                    style = MaterialTheme.typography.labelSmall
                                )
                                Surface(
                                    shape = RoundedCornerShape(dimensionResource(R.dimen.chip_corner_radius)),
                                ) {
                                    Text(
                                        text = track.track.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .clickable {
                                                tripOrTrackName = track.track.name
                                                trackId = track.track.id
                                                renameExpanded = true
                                                screen = 1
                                            }
                                            .padding(4.dp)
                                    )
                                }
                                /*Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Spacer(
                                        modifier = Modifier.weight(1f)
                                    )
                                }*/
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_separation)),
                                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_separation)),
                                    modifier = Modifier
                                        .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
                                ) {
                                    MyChip(
                                        label1 = track.track.type,
                                        onLabel1Clicked = { onChooseModeClicked(track.track) },
                                        label2 = uiState.cars.first { car -> car.id == track.track.carId }
                                            .getDisplayName(),
                                        onLabel2Clicked = { onChooseVehicleClicked(track.track) }
                                    )
                                }

                                //                    if (sectionExpanded) {
                                ////                        LazyColumn(
                                ////                            modifier = Modifier.heightIn(0.dp, 1000.dp)
                                ////                        ) {
                                ////                            items(
                                ////                                items = track.trackSegments,
                                ////                                key = { eachTrackSegment -> eachTrackSegment.trackSegment.id }
                                ////                            ) { trackSegment ->
                                //                        track.trackSegments.reversed().forEach { trackSegment ->
                                //                            Text(
                                //                                text = "Track Segment ${trackSegment.trackSegment.number}",
                                //                                style = MaterialTheme.typography.titleSmall
                                //                            )
                                //                            Column(
                                //                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                //                                modifier = Modifier
                                //                                    .heightIn(0.dp, 500.dp)
                                //                                    .verticalScroll(rememberScrollState())
                                //                            ) {
                                //                                //                            items(
                                //                                //                                items = trackSegment.trackPoints,
                                //                                //                                key = { eachTrackPoint -> eachTrackPoint.id }
                                //                                //                            ) { trackPoint ->
                                //                                trackSegment.trackPoints.reversed().forEach { trackPoint ->
                                //                                    val latitude = String.format("%.5f", trackPoint.latitude)
                                //                                    val longitude = String.format("%.5f", trackPoint.longitude)
                                //
                                //                                    Column(
                                //                                        modifier = Modifier
                                //                                            .border(1.dp, Color.LightGray, RectangleShape)
                                //                                            .clickable {
                                //                                                onTrackPointClicked(
                                //                                                    String.format(
                                //                                                        "%s, %s",
                                //                                                        latitude,
                                //                                                        longitude
                                //                                                    )
                                //                                                )
                                //                                                latitudeSelected = latitude.toDouble()
                                //                                                longitudeSelected = longitude.toDouble()
                                //                                            }
                                //                                            .fillMaxWidth()
                                //                                            .padding(8.dp)
                                //                                    ) {
                                //                                        Row(
                                //                                            horizontalArrangement = Arrangement.SpaceBetween,
                                //                                            modifier = Modifier
                                //                                                .fillMaxWidth()
                                //                                        ) {
                                //                                            Text(
                                //                                                text = "Track Point ID ${trackPoint.id}",
                                //                                                style = MaterialTheme.typography.labelSmall
                                //                                            )
                                //                                            Text(
                                //                                                text = toDateTime(
                                //                                                    trackPoint.time,
                                //                                                    "YYYY/MM/dd hh:mm:ss a"
                                //                                                ),
                                //                                                style = MaterialTheme.typography.labelSmall
                                //                                            )
                                //                                        }
                                //                                        Row(
                                //                                            horizontalArrangement = Arrangement.SpaceAround,
                                //                                            verticalAlignment = Alignment.CenterVertically,
                                //                                            modifier = Modifier
                                //                                                .fillMaxWidth()
                                //                                                .height(30.dp)
                                //                                        ) {
                                //                                            Text(
                                //                                                text = latitude,
                                //                                                style = MaterialTheme.typography.titleMedium,
                                //                                                textAlign = TextAlign.Center,
                                //                                                modifier = Modifier
                                //                                                    .weight(1f)
                                //                                            )
                                //                                            VerticalDivider(
                                //                                                modifier = Modifier
                                //                                                    .fillMaxHeight()
                                //                                                    .width(1.dp)
                                //                                            )
                                //                                            Text(
                                //                                                text = longitude,
                                //                                                style = MaterialTheme.typography.titleMedium,
                                //                                                textAlign = TextAlign.Center,
                                //                                                modifier = Modifier
                                //                                                    .weight(1f)
                                //                                            )
                                //                                        }
                                //                                    }
                                //                                }
                                //                            }
                                //                            Spacer(
                                //                                modifier = Modifier
                                //                                    .height(6.dp)
                                //                            )
                                //                        }
                                //                    }
                            }
                            AnimatedVisibility(
                                visible = toolVisible
                            ) {
                                Row {
                                    IconButton(
                                        onClick = { onDeleteTrackClicked(track.track) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TripDetailCompactPreview() {
    HeatMapTheme {
        TripDetailScreen(
            uiState = UiState(
                cars = DataSource.getMockCars(),
                allTripsWithTracks = listOf(DataSource.getMockTripWithTracks()),
                currentPage = yong.jianwen.heatmap.CurrentPage.TRIP_DETAIL,
                carsAndModesForEachTrip = DataSource.getChips()
            ),
            tripWithTracksOrNothing = DataSource.getMockTripWithTracks(),
            onTrackPointClicked = { },
            onPauseTrip = { },
            onContinueTrip = { },
            onEndTrip = { },
            onUpdateTrip = { },
            onUpdateTrack = { },
            onDeleteClicked = { },
            onChooseVehicleClicked = { },
            onChooseModeClicked = { },
            onMoreClicked = { },
            onMoreDismissed = { },
            onMoreItem1Clicked = { },
            onBack = { },
            onDeleteTrackClicked = { },
            onMapClicked = { }
        )
    }
}
