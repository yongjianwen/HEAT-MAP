package com.example.heatmap.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.heatmap.CurrentPage
import com.example.heatmap.R
import com.example.heatmap.data.entity.Track
import com.example.heatmap.data.entity.Trip
import com.example.heatmap.data.entity.TripWithTracks
import com.example.heatmap.data.helper.UpdateTrack
import com.example.heatmap.data.helper.UpdateTrip
import com.example.heatmap.formatDisplayStartEndTimes
import com.example.heatmap.local.DataSource
import com.example.heatmap.toDateTime
import com.example.heatmap.ui.component.FourButtons
import com.example.heatmap.ui.component.MyChip
import com.example.heatmap.ui.component.MyDialog
import com.example.heatmap.ui.theme.HeatMapTheme
import com.example.heatmap.ui.theme.NotoSans
import com.example.heatmap.ui.theme.labelMediumSmall
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.coroutine.awaitCameraForCoordinates
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripDetailScreen(
    uiState: AppUiState,
    _tripWithTracks: TripWithTracks?,
    onTripNameClicked: (tripId: Long) -> Unit,
    onTrackPointClicked: (String) -> Unit,
    onPauseTrip: () -> Unit,
    onContinueTrip: (tripId: Long) -> Unit,
    onEndTrip: () -> Unit,
    onUpdateTrip: (trip: UpdateTrip) -> Unit,
    onUpdateTrack: (track: UpdateTrack) -> Unit,
    onDeleteClicked: (trip: Trip) -> Unit,
    onDeleteDismissed: () -> Unit,
    onChooseVehicleClicked: (track: Track) -> Unit,
    onChooseVehicleDismissed: () -> Unit,
    onChooseModeClicked: (track: Track) -> Unit,
    onChooseModeDismissed: () -> Unit,
    onBack: () -> Unit,
    onMoreClicked: () -> Unit,
    onMoreDismissed: () -> Unit,
    onMoreItem1Clicked: (tripId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val tripWithTracks = _tripWithTracks ?: TripWithTracks(
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
    /*var northernmost = -90.0
    var southernmost = 90.0
    var easternmost = -180.0
    var westernmost = 180.0
    tripWithTracks.tracks.flatMap { track ->
        track.trackSegments.flatMap { trackSegment ->
            trackSegment.trackPoints.map { trackPoint ->
                Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
            }
        }
    }.forEach { point ->
        northernmost = Math.max(northernmost, point.latitude())
        southernmost = Math.min(southernmost, point.latitude())
        easternmost = Math.max(easternmost, point.longitude())
        westernmost = Math.min(westernmost, point.longitude())
    }
    val cornerPoints = listOf(
        Point.fromLngLat(westernmost, southernmost),
        Point.fromLngLat(westernmost, northernmost),
        Point.fromLngLat(easternmost, northernmost),
        Point.fromLngLat(easternmost, southernmost)
    )*/
//    Log.d("TESTTEST100", cornerPoints.toString())
//
//    val cornerP by rememberSaveable { mutableStateOf(cornerPoints) }

//    val northernmost by rememberSaveable {mutableStateOf()}

    /*val northernmost = points.maxByOrNull { it.latitude() }?.latitude() ?: 10.0
    val southernmost = points.minByOrNull { it.latitude() }?.latitude() ?: 10.0
    val easternmost = points.maxByOrNull { it.longitude() }?.longitude() ?: 10.0
    val westernmost = points.minByOrNull { it.longitude() }?.longitude() ?: 10.0*/

    var northernmost1 by rememberSaveable {
        mutableDoubleStateOf(tripWithTracks.tracks.flatMap { track ->
            track.trackSegments.flatMap { trackSegment ->
                trackSegment.trackPoints.map { trackPoint ->
                    Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                }
            }
        }.maxByOrNull { it.latitude() }?.latitude() ?: 0.0)
    }
    var southernmost1 by rememberSaveable {
        mutableDoubleStateOf(tripWithTracks.tracks.flatMap { track ->
            track.trackSegments.flatMap { trackSegment ->
                trackSegment.trackPoints.map { trackPoint ->
                    Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                }
            }
        }.minByOrNull { it.latitude() }?.latitude() ?: 0.0)
    }
    var easternmost1 by rememberSaveable {
        mutableDoubleStateOf(tripWithTracks.tracks.flatMap { track ->
            track.trackSegments.flatMap { trackSegment ->
                trackSegment.trackPoints.map { trackPoint ->
                    Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                }
            }
        }.maxByOrNull { it.longitude() }?.longitude() ?: 0.0)
    }
    var westernmost1 by rememberSaveable {
        mutableDoubleStateOf(tripWithTracks.tracks.flatMap { track ->
            track.trackSegments.flatMap { trackSegment ->
                trackSegment.trackPoints.map { trackPoint ->
                    Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                }
            }
        }.minByOrNull { it.longitude() }?.longitude() ?: 0.0)
    }

    /*LaunchedEffect(_tripWithTracks) {
        coroutineScope.launch {
            if (_tripWithTracks != null) {
                northernmost1 = _tripWithTracks.tracks.flatMap { track ->
                    track.trackSegments.flatMap { trackSegment ->
                        trackSegment.trackPoints.map { trackPoint ->
                            Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                        }
                    }
                }.maxByOrNull { it.latitude() }?.latitude() ?: 0.0

                southernmost1 = _tripWithTracks.tracks.flatMap { track ->
                    track.trackSegments.flatMap { trackSegment ->
                        trackSegment.trackPoints.map { trackPoint ->
                            Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                        }
                    }
                }.minByOrNull { it.latitude() }?.latitude() ?: 0.0

                easternmost1 = _tripWithTracks.tracks.flatMap { track ->
                    track.trackSegments.flatMap { trackSegment ->
                        trackSegment.trackPoints.map { trackPoint ->
                            Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                        }
                    }
                }.maxByOrNull { it.longitude() }?.longitude() ?: 0.0

                westernmost1 = _tripWithTracks.tracks.flatMap { track ->
                    track.trackSegments.flatMap { trackSegment ->
                        trackSegment.trackPoints.map { trackPoint ->
                            Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
                        }
                    }
                }.minByOrNull { it.longitude() }?.longitude() ?: 0.0
            }
        }
    }*/

    Log.d(
        "TESTTEST",
        Point.fromLngLat(westernmost1, southernmost1).toString()
    )
    Log.d(
        "TESTTEST",
        Point.fromLngLat(easternmost1, northernmost1).toString()
    )

    if (renameExpanded) {
        MyDialog(
            title = when (screen) {
                0 -> "Rename Trip"
                1 -> "Rename Track"
                2 -> stringResource(R.string.delete_vehicle)
                else -> ""
            },
            onDismissRequest = { renameExpanded = false },
            button1Label = when (screen) {
                0, 1 -> stringResource(R.string.save)
                2 -> stringResource(R.string.delete)
                else -> null
            },
            button1Color = when (screen) {
                2 -> MaterialTheme.colorScheme.error
                else -> null
            },
            button1Enabled = true,
            onButton1Clicked = {
                when (screen) {
                    0 -> {
                        onUpdateTrip(
                            UpdateTrip(
                                id = tripWithTracks.trip.id,
                                name = tripOrTrackName
                            )
                        )
                        renameExpanded = false
                    }

                    1 -> {
                        onUpdateTrack(
                            UpdateTrack(
                                id = trackId,
                                name = tripOrTrackName
                            )
                        )
                        renameExpanded = false
                    }
                }
            },
            /*button2Label = when (screen) {
                0 -> stringResource(R.string.manage_vehicle)
                else -> null
            },
            button2Enabled = when (screen) {
                0 -> uiState.cars.isNotEmpty()
                else -> true
            },
            onClickedButton2 = {
                when (screen) {
                    0 -> toolVisible = !toolVisible
                }
            }*/
        ) { mod ->
            if (screen == 0 || screen == 1) {
                val focusRequester = remember { FocusRequester() }
                val textFieldValue =
                    remember {
                        mutableStateOf(
                            TextFieldValue(
                                tripOrTrackName,
                                TextRange(tripOrTrackName.length)
                            )
                        )
                    }

                LaunchedEffect(Unit) {
//                focusRequester.requestFocus()
                }

                OutlinedTextField(
                    value = textFieldValue.value,
                    onValueChange = {
                        textFieldValue.value = it.copy(it.text.trimStart())
                        tripOrTrackName = textFieldValue.value.text
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    label = {
                        Text(
                            text = if (screen == 0) "Trip Name" else "Track Name",
                            fontFamily = NotoSans
                        )
                    },
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                    /*singleLine = true,*/
                    maxLines = 5,
                    modifier = Modifier
                        .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                        .focusRequester(focusRequester)
                )
            }
        }
    }

    Scaffold(
        topBar = {
            HeatMapAppBar(
                title = "Trip Details",
                onAppBarClicked = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(tripWithTracks.tracks.size - 1)
                    }
                    /*mapExpanded = true*/
                },
                showBackButton = true,
                showMoreButton = true,
                onBackClicked = { onBack() },
                onMoreClicked = onMoreClicked
            ) {
                DropdownMenu(
                    expanded = uiState.moreExpanded,
                    onDismissRequest = onMoreDismissed
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Copy GPX...",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = { onMoreItem1Clicked(tripWithTracks.trip.id) }
                    )
                }
            }
        }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(it)
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
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
//                            .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
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
                                //.fillMaxWidth()
                                //.padding(vertical = dimensionResource(R.dimen.card_inner_vertical_padding))
                                .padding(10.dp)
                        )
                    }
                    Text(
                        text = formatDisplayStartEndTimes(
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
                            uiState.chips.filter { chip -> tripWithTracks.trip.id == chip.tripId }
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
                    val mapViewportState = rememberMapViewportState() {
                        /*setCameraOptions {
//                                zoom(12.0)
//                                center(Point.fromLngLat(102.23142684950145, 2.2432788267098336))
                            pitch(0.0)
                            bearing(0.0)
                        }*/
                    }
                    var markerResourceId by remember {
                        mutableStateOf(R.drawable.ic_launcher_background)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        MapboxMap(
                            mapViewportState = mapViewportState,
                            mapState = rememberMapState {
                                gesturesSettings = GesturesSettings {
                                    scrollEnabled = false
//                                    pinchToZoomEnabled = false
                                    rotateEnabled = false
                                    pitchEnabled = false
//                                    doubleTapToZoomInEnabled = false
//                                    doubleTouchToZoomOutEnabled = false
                                    quickZoomEnabled = false
//                                    pinchToZoomDecelerationEnabled = false
                                    rotateDecelerationEnabled = false
                                    scrollDecelerationEnabled = false
                                    increaseRotateThresholdWhenPinchingToZoom = false
                                    increasePinchToZoomThresholdWhenRotating = false
//                                    pinchScrollEnabled = false
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
                                .height((250 * 1.8).dp)
                        ) {
                            // Get reference to the raw MapView using MapEffect
                            if (_tripWithTracks != null && _tripWithTracks.tracks.isNotEmpty())
                                MapEffect(Unit) { mapView ->
                                    // Use mapView to access the Mapbox Maps APIs not in the Compose extension.
                                    // Changes inside `MapEffect` may conflict with Compose states.

//                                    coroutineScope.launch {
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
                                            EdgeInsets(100.0, 100.0, 100.0, 100.0)
                                        )
                                    mapViewportState.setCameraOptions(cameraPosition)

                                    val cameraBoundsOptions = CameraBoundsOptions.Builder()
                                        .maxZoom(15.0)
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
                            tripWithTracks.tracks.flatMap { track ->
                                track.trackSegments
                            }.forEach {
                                PolylineAnnotation(
                                    points = it.trackPoints.map { trackPoint ->
                                        Point.fromLngLat(
                                            trackPoint.longitude,
                                            trackPoint.latitude
                                        )
                                    }
                                ) {
                                    lineColor = Color.Red
                                    lineWidth = 2.0
                                }
                            }
                            /*PolylineAnnotation(
                                points = tripWithTracks.tracks.flatMap { track ->
                                    track.trackSegments.flatMap { trackSegment ->
                                        trackSegment.trackPoints.map {
                                            Point.fromLngLat(
                                                it.longitude,
                                                it.latitude
                                            )
                                        }
                                    }
                                }
                            ) {
                                lineColor = Color.Red
                                lineWidth = 2.0
//                                lineOpacity = 0.2
                            }*/
                        }
                    }
                    Spacer(
                        modifier = Modifier.height(dimensionResource(R.dimen.card_medium_content_padding))
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
                    Spacer(
                        modifier = Modifier.height(dimensionResource(R.dimen.card_medium_content_padding))
                    )
                    Divider()
                }
            }
            items(
                items = tripWithTracks.tracks.reversed(),
                key = { eachTrack -> eachTrack.track.id }
            ) { track ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(R.dimen.card_medium_content_padding))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(dimensionResource(R.dimen.chip_corner_radius))
                            ) {
                                Text(
                                    text = "Track ${track.track.number}: ${track.track.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .clickable {
                                            tripOrTrackName = track.track.name
                                            trackId = track.track.id
                                            renameExpanded = true
                                            screen = 1
                                        }
                                        //.padding(dimensionResource(R.dimen.card_inner_vertical_padding))
                                        .padding(10.dp)
                                )
                            }
                            /*Text(
                                text = "Track ${track.track.number}: ${track.track.name}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .clickable {
                                        tripOrTrackName = track.track.name
                                        trackId = track.track.id
                                        renameExpanded = true
                                        screen = 1
                                    }
                                    .padding(vertical = dimensionResource(R.dimen.card_inner_vertical_padding))
                                    //.fillMaxWidth()
                            )*/
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_separation)),
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_separation)),
                                modifier = Modifier
                                    .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
                            ) {
                                MyChip(
                                    label1 = track.track.type,
                                    /*modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondaryContainer),*/
                                    onLabel1Clicked = { onChooseModeClicked(track.track) },
                                    label2 = uiState.cars.first { car -> car.id == track.track.carId }
                                        .getDisplayName(),
                                    onLabel2Clicked = { onChooseVehicleClicked(track.track) }
                                )
                            }
                        }
                        /*Spacer(
                            modifier = Modifier.weight(1f)
                        )*/
                        IconButton(
                            onClick = { sectionExpanded = !sectionExpanded }
                        ) {
                            Icon(
                                imageVector = if (sectionExpanded)
                                    Icons.Filled.KeyboardArrowUp
                                else
                                    Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Expand track"
                            )
                        }
                    }

                    if (sectionExpanded) {
//                        LazyColumn(
//                            modifier = Modifier.heightIn(0.dp, 1000.dp)
//                        ) {
//                            items(
//                                items = track.trackSegments,
//                                key = { eachTrackSegment -> eachTrackSegment.trackSegment.id }
//                            ) { trackSegment ->
                        track.trackSegments.reversed().forEach { trackSegment ->
                            Text(
                                text = "Track Segment ${trackSegment.trackSegment.number}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier
                                    .heightIn(0.dp, 100.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                //                            items(
                                //                                items = trackSegment.trackPoints,
                                //                                key = { eachTrackPoint -> eachTrackPoint.id }
                                //                            ) { trackPoint ->
                                trackSegment.trackPoints.reversed().forEach { trackPoint ->
                                    val latitude = String.format("%.5f", trackPoint.latitude)
                                    val longitude = String.format("%.5f", trackPoint.longitude)

                                    Column(
                                        modifier = Modifier
                                            .border(1.dp, Color.LightGray, RectangleShape)
                                            .clickable {
                                                onTrackPointClicked(
                                                    String.format(
                                                        "%s, %s",
                                                        latitude,
                                                        longitude
                                                    )
                                                )
                                            }
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Track Point ID ${trackPoint.id}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Text(
                                                text = toDateTime(
                                                    trackPoint.time,
                                                    "YYYY/MM/dd hh:mm:ss a"
                                                ),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceAround,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(30.dp)
                                        ) {
                                            Text(
                                                text = latitude,
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                            )
                                            Divider(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(1.dp)
                                            )
                                            Text(
                                                text = longitude,
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(
                                modifier = Modifier
                                    .height(6.dp)
                            )
//                            }
                        }
                    }
                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
                    Divider()
                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
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
            uiState = AppUiState(
                cars = DataSource.getMockCars(),
                trips = DataSource.getMockTrips(),
                currentPage = CurrentPage.TRIP_DETAIL,
                chips = DataSource.getChips()
            ),
            _tripWithTracks = DataSource.getMockTripWithTracks(),
            onTripNameClicked = { },
            onTrackPointClicked = { },
            onPauseTrip = { },
            onContinueTrip = { },
            onEndTrip = { },
            onUpdateTrip = { },
            onUpdateTrack = { },
            onDeleteClicked = { },
            onDeleteDismissed = { },
            onChooseVehicleClicked = { },
            onChooseVehicleDismissed = { },
            onChooseModeClicked = { },
            onChooseModeDismissed = { },
            onMoreClicked = { },
            onMoreDismissed = { },
            onMoreItem1Clicked = { },
            onBack = { }
        )
    }
}
