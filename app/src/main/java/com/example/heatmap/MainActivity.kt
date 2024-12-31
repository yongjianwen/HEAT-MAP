package com.example.heatmap

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.heatmap.data.TripMode
import com.example.heatmap.data.entity.Car
import com.example.heatmap.data.entity.Trip
import com.example.heatmap.data.entity.TripWithTracks
import com.example.heatmap.data.helper.NewTripInfo
import com.example.heatmap.service.LocationService
import com.example.heatmap.ui.AppViewModel
import com.example.heatmap.ui.TripDetailScreen
import com.example.heatmap.ui.TripListScreen
import com.example.heatmap.ui.component.MyDialog
import com.example.heatmap.ui.component.MyDialogSelectableList
import com.example.heatmap.ui.component.MyDialogText
import com.example.heatmap.ui.theme.HeatMapTheme
import com.example.heatmap.ui.theme.NotoSans
import com.mapbox.bindgen.Value
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
import com.mapbox.maps.extension.compose.style.importConfigs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted
                    // Toast.makeText(this, "FINE", Toast.LENGTH_SHORT).show()
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted
                    Toast.makeText(this, "COARSE", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    // No location access granted
                    Toast.makeText(this, "BAD", Toast.LENGTH_SHORT).show()
                }
            }
        }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeatMapTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val appViewModel: AppViewModel = viewModel(factory = AppViewModel.factory)
                    val appUiState by appViewModel.appUiState.collectAsState()

                    val intent = Intent(this, LocationService::class.java)

                    val navController = rememberNavController()
                    val onBack = {
                        appUiState.currentPage = CurrentPage.HOME
                        navController.navigateUp()
                    }

                    var screen by rememberSaveable { mutableIntStateOf(0) }

                    val windowSize = calculateWindowSizeClass(this)

                    if (appUiState.alertExpanded) {
                        MyDialog(
                            title = "Error",
                            onDismissRequest = { appViewModel.hideAlertDialog() },
                            button1Label = "Dismiss",
                            onButton1Clicked = {
                                appViewModel.hideAlertDialog()
                            }
                        ) {

                        }
                    }

                    var mapExpanded by rememberSaveable { mutableStateOf(false) }

                    if (mapExpanded) {
                        MyDialog(
                            title = "Heat Map",
                            onDismissRequest = { mapExpanded = false }
                        ) {
                            val mapViewportState = rememberMapViewportState { }

                            Surface(
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                MapboxMap(
                                    mapViewportState = mapViewportState,
                                    mapState = rememberMapState { },
                                    logo = { },
                                    attribution = { },
                                    style = {
                                        GenericStyle(
                                            style = "", //Style.DARK,
                                            styleImportsContent = {
                                                StyleImport(
                                                    importId = "standard",
                                                    style = Style.DARK,
                                                    configs = importConfigs {
                                                        config("showTransitLabels", Value(false))
                                                        config("showPlaceLabels", Value(false))
                                                        config(
                                                            "showPointOfInterestLabels",
                                                            Value(false)
                                                        )
                                                        config("showRoadLabels", Value(false))
                                                    }
                                                )
                                            }
                                        )
                                    },
                                    /*style = {
                                        MapboxStandardStyle {
                                            lightPreset = LightPresetValue.NIGHT
                                            theme = ThemeValue.DEFAULT
                                            showPlaceLabels = BooleanValue(false)
                                            showRoadLabels = BooleanValue(false)
                                            showPointOfInterestLabels = BooleanValue(false)
                                        }
                                    },*/
                                    /*style = { GenericStyle(
                                        style = "", //Style.DARK,
                                        styleImportsContent = {
                                                StyleImport(
                                                    importId = "standard",
                                                    style = Style.DARK,
                                                    configs = importConfigs {
                                                        config("showTransitLabels", Value(false))
                                                        config("showPlaceLabels", Value(false))
                                                        config("showPointOfInterestLabels", Value(false))
                                                        config("showRoadLabels", Value(false))
                                                    }
                                                )
                                        }
                                    ) },*/
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    // Get reference to the raw MapView using MapEffect
                                    MapEffect(Unit) { mapView ->
                                        val points = appUiState.allTrips.flatMap { tripWithTracks ->
                                            tripWithTracks.tracks.flatMap { track ->
                                                track.trackSegments.flatMap { trackSegment ->
                                                    trackSegment.trackPoints.map { trackPoint ->
                                                        Point.fromLngLat(
                                                            trackPoint.longitude,
                                                            trackPoint.latitude
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        var northernmost2 = -90.0
                                        var southernmost2 = 90.0
                                        var easternmost2 = -180.0
                                        var westernmost2 = 180.0
                                        points.forEach { point ->
                                            northernmost2 =
                                                Math.max(northernmost2, point.latitude())
                                            southernmost2 =
                                                Math.min(southernmost2, point.latitude())
                                            easternmost2 =
                                                Math.max(easternmost2, point.longitude())
                                            westernmost2 =
                                                Math.min(westernmost2, point.longitude())
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

                                        val cameraBoundsOptions =
                                            CameraBoundsOptions.Builder()
                                                .maxZoom(15.0)
                                                .build()
                                        mapView.mapboxMap.setBounds(cameraBoundsOptions)
                                    }
                                    appUiState.allTrips.flatMap { tripWithTracks ->
                                        tripWithTracks.tracks.flatMap { track ->
                                            track.trackSegments
                                        }
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
                                            lineWidth = 5.0
                                            lineOpacity = 0.1
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (appUiState.carExpanded) {
                        var carIdToEdit by rememberSaveable { mutableIntStateOf(-1) }

                        var carIdToDelete by rememberSaveable { mutableIntStateOf(-1) }
                        var carNameToDelete by rememberSaveable { mutableStateOf("") }

                        var manufacturer by rememberSaveable { mutableStateOf("") }
                        var model by rememberSaveable { mutableStateOf("") }
                        var numberPlate by rememberSaveable { mutableStateOf("") }

                        var toolVisible by remember { mutableStateOf(false) }

                        MyDialog(
                            title = when (screen) {
                                -1 -> "Error"
                                0 -> if (!appUiState.isUpdatingCarOrMode) stringResource(R.string.choose_vehicle) else "Update Vehicle"
                                1 -> stringResource(R.string.add_new_vehicle)
                                2 -> stringResource(R.string.delete_vehicle)
                                3 -> stringResource(R.string.edit_vehicle)
                                else -> ""
                            },
                            onDismissRequest = {
                                when (screen) {
                                    0 -> {
                                        appViewModel.hideCarDialog()
                                    }

                                    else -> screen = 0
                                }
                            },
                            backgroundColor = if (screen == -1)
                                MaterialTheme.colorScheme.error
                            else
                                null,
                            button1Label = when (screen) {
                                -1 -> "Dismiss"
                                0 -> stringResource(R.string.add_vehicle)
                                1 -> stringResource(R.string.save)
                                2 -> stringResource(R.string.delete)
                                3 -> stringResource(R.string.save)
                                else -> null
                            },
                            button1Color = when (screen) {
                                -1 -> MaterialTheme.colorScheme.onErrorContainer
                                2 -> MaterialTheme.colorScheme.error
                                else -> null
                            },
                            button1Enabled = when (screen) {
                                1 -> manufacturer.isNotBlank() && model.isNotBlank() && numberPlate.isNotBlank()
                                3 -> manufacturer.isNotBlank() && model.isNotBlank() && numberPlate.isNotBlank()
                                else -> true
                            },
                            onButton1Clicked = {
                                when (screen) {
                                    -1 -> {
                                        screen = 0
                                    }

                                    0 -> {
                                        manufacturer = ""
                                        model = ""
                                        numberPlate = ""
                                        screen = 1
                                    }

                                    1 -> {
                                        appViewModel.addNewCar(
                                            Car(
                                                id = 0,
                                                registrationNumber = numberPlate.trim(),
                                                manufacturer = manufacturer.trim(),
                                                model = model.trim()
                                            )
                                        )
                                        manufacturer = ""
                                        model = ""
                                        numberPlate = ""
                                        screen = 0
                                    }

                                    2 -> {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            if (appViewModel.deleteCar(carIdToDelete)) {
                                                screen = 0
                                            } else {
//                                                appViewModel.showAlertDialog()
                                                screen = -1
                                            }
                                        }
                                        /*appViewModel.deleteCar(carIdToDelete)
                                        screen = 0*/
                                    }

                                    3 -> {
                                        appViewModel.updateCar(
                                            Car(
                                                id = carIdToEdit,
                                                registrationNumber = numberPlate,
                                                manufacturer = manufacturer,
                                                model = model
                                            )
                                        )
                                        screen = 0
                                    }
                                }
                            },
                            button2Label = when (screen) {
                                0 -> stringResource(R.string.manage_vehicle)
                                else -> null
                            },
                            button2Enabled = when (screen) {
                                0 -> appUiState.cars.isNotEmpty()
                                else -> true
                            },
                            onButton2Clicked = {
                                when (screen) {
                                    0 -> toolVisible = !toolVisible
                                }
                            }
                        ) { mod ->
                            if (screen == -1) {
                                MyDialogText(
                                    text = "Cannot delete $carNameToDelete as it is in use.\n\nFind and replace all tracks using this car first.",
                                    modifier = mod
                                )
                            }
                            if (screen == 0) {
                                MyDialogSelectableList(
                                    items = appUiState.cars,
                                    onItemSelected = {
                                        if (!appUiState.isUpdatingCarOrMode) {
                                            appViewModel.saveCar(it as Car)
                                            appViewModel.hideCarDialog()
                                        } else {
                                            appViewModel.updateTrackCarIdById(
                                                appUiState.updatingTrackId,
                                                (it as Car).id
                                            )
                                            appViewModel.hideCarDialog()
                                        }
                                    },
                                    modifier = mod,
                                    selectedItem = if (!appUiState.isUpdatingCarOrMode)
                                        appUiState.carSelected
                                    else
                                        appUiState.updatingCarOrModeSelected as Car
                                ) {
                                    AnimatedVisibility(
                                        visible = toolVisible
                                    ) {
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    carIdToDelete = it.id
                                                    carNameToDelete = it.getDisplayName()
                                                    screen = 2
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = String.format(
                                                        stringResource(R.string.delete_item),
                                                        it.getDisplayName()
                                                    ),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    carIdToEdit = it.id
                                                    manufacturer = (it as Car).manufacturer
                                                    model = it.model
                                                    numberPlate = it.registrationNumber
                                                    screen = 3
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = String.format(
                                                        stringResource(R.string.edit_item),
                                                        it.getDisplayName()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (screen == 1 || screen == 3) {
                                val focusRequester = remember { FocusRequester() }
                                val textFieldValue =
                                    remember {
                                        mutableStateOf(
                                            TextFieldValue(
                                                manufacturer,
                                                TextRange(manufacturer.length)
                                            )
                                        )
                                    }

                                LaunchedEffect(Unit) {
                                    if (screen == 1) {
                                        focusRequester.requestFocus()
                                    }
                                }

                                LazyColumn(
                                    modifier = mod
                                ) {
                                    item {
                                        OutlinedTextField(
                                            value = textFieldValue.value,
                                            onValueChange = {
                                                textFieldValue.value = it.copy(it.text.trimStart())
                                                manufacturer = textFieldValue.value.text
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.Words,
                                                imeAction = ImeAction.Next
                                            ),
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.manufacturer),
                                                    fontFamily = NotoSans
                                                )
                                            },
                                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                                            singleLine = true,
                                            modifier = Modifier
                                                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                                                .focusRequester(focusRequester)
                                        )
                                        OutlinedTextField(
                                            value = model,
                                            onValueChange = { model = it.trimStart() },
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.Words,
                                                imeAction = ImeAction.Next
                                            ),
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.model),
                                                    fontFamily = NotoSans
                                                )
                                            },
                                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                                            singleLine = true,
                                            modifier = Modifier
                                                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                                        )
                                        OutlinedTextField(
                                            value = numberPlate,
                                            onValueChange = { numberPlate = it.trimStart() },
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.Characters,
                                                imeAction = ImeAction.Done
                                            ),
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.number_plate),
                                                    fontFamily = NotoSans
                                                )
                                            },
                                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                                            singleLine = true,
                                            modifier = Modifier
                                                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                                        )
                                    }
                                }
                            } else if (screen == 2) {
                                MyDialogText(
                                    text = "Confirm delete $carNameToDelete?",
                                    modifier = mod
                                )
                            }
                        }
                    }

                    if (appUiState.modeExpanded) {
                        MyDialog(
                            title = if (!appUiState.isUpdatingCarOrMode) stringResource(R.string.choose_mode) else "Update Mode",
                            onDismissRequest = { appViewModel.hideModeDialog() }
                        ) { mod ->
                            MyDialogSelectableList(
                                items = TripMode.entries,
                                onItemSelected = {
                                    if (!appUiState.isUpdatingCarOrMode) {
                                        appViewModel.saveMode(it as TripMode)
                                        appViewModel.hideModeDialog()
                                    } else {
                                        appViewModel.updateTrackTypeById(
                                            appUiState.updatingTrackId,
                                            it as TripMode
                                        )
                                        appViewModel.hideModeDialog()
                                    }
                                },
                                modifier = mod,
                                selectedItem = if (!appUiState.isUpdatingCarOrMode)
                                    appUiState.modeSelected
                                else
                                    appUiState.updatingCarOrModeSelected as TripMode
                            ) {}
                        }
                    }

                    if (appUiState.deleteTripExpanded) {
                        MyDialog(
                            title = "Delete Trip",
                            onDismissRequest = { appViewModel.hideDeleteTripDialog() },
                            button1Label = stringResource(R.string.delete),
                            button1Color = MaterialTheme.colorScheme.error,
                            onButton1Clicked = {
                                appViewModel.deleteTrip(appUiState.tripIdToDelete)
                                appViewModel.hideDeleteTripDialog()

                                if (navController.currentBackStackEntry?.destination?.route == CurrentPage.TRIP_DETAIL.name + "/{tripId}") {
                                    navController.popBackStack()
                                    appUiState.currentPage = CurrentPage.HOME
                                }
                            }
                        ) { mod ->
                            Text(
                                text = "Confirm delete trip ID ${appUiState.tripIdToDelete}?",
                                fontFamily = NotoSans,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = CurrentPage.HOME.name
                    ) {
                        composable(
                            route = CurrentPage.HOME.name,
                            exitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    tween(700)
//                                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                                )
                            },
                            popEnterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    tween(700)
//                                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                                )
                            }
                        ) {
                            appViewModel.editTrack(false)

                            TripListScreen(
                                uiState = appUiState,
                                windowSize = windowSize.widthSizeClass,
                                onCarSelected = { appViewModel.saveCar(it) },
                                onModeSelected = { appViewModel.saveMode(it) },
                                onAddNewCar = { appViewModel.addNewCar(it) },
                                onDeleteCar = { /*appViewModel.deleteCar(it)*/ },
                                onClearCar = { appViewModel.clearCar() },
                                onUpdateCar = { appViewModel.updateCar(it) },
                                onStartNewTrip = {
                                    appViewModel.startNewTrip(
                                        NewTripInfo(
                                            car = appUiState.carSelected!!,
                                            tripMode = appUiState.modeSelected!!,
                                            tripName = getString(R.string.default_trip_name),
                                            trackName = getString(R.string.default_track_name),
                                            tripId = -1
                                        )
                                    )
                                    startService(intent)
                                },
                                onPauseTrip = {
                                    appViewModel.pauseTrip()
                                    stopService(intent)
                                },
                                onContinueTrip = {
                                    appViewModel.continueTrip(
                                        NewTripInfo(
                                            car = appUiState.carSelected!!,
                                            tripMode = appUiState.modeSelected!!,
                                            tripName = getString(R.string.default_trip_name),
                                            trackName = getString(R.string.default_track_name),
                                            tripId = it
                                        )
                                    )
                                    startService(intent)
                                },
                                onEndTrip = {
                                    appViewModel.endTrip()
                                    stopService(intent)
                                },
                                onDeleteTrip = {
                                    appViewModel.deleteTrip(it)
                                    appViewModel.hideDeleteTripDialog()
                                },
                                onCardClicked = {
                                    if (navController.currentBackStackEntry?.destination?.route == CurrentPage.HOME.name) {
                                        navController.navigate(
                                            CurrentPage.TRIP_DETAIL.name + "/$it"
                                        )
                                        appUiState.currentPage = CurrentPage.TRIP_DETAIL
                                    }
                                },
                                onDeleteClicked = { appViewModel.showDeleteTripDialog(it) },
                                onDeleteDismissed = { appViewModel.hideDeleteTripDialog() },
                                onChooseVehicleClicked = { appViewModel.showCarDialog() },
                                onChooseVehicleDismissed = { appViewModel.hideCarDialog() },
                                onChooseModeClicked = { appViewModel.showModeDialog() },
                                onChooseModeDismissed = { appViewModel.hideModeDialog() },
                                onMoreClicked = { appViewModel.showMoreMenu() },
                                onMoreDismissed = { appViewModel.hideMoreMenu() },
                                onMoreItem1Clicked = { mapExpanded = true },
                                onExportData = { },
                                onImportData = { },
                                modifier = Modifier,
                                onGenerateGPX = { appViewModel.generateGPX() }
                            )
                        }

                        val tripIdArgument = "tripId"
                        composable(
                            route = CurrentPage.TRIP_DETAIL.name + "/{$tripIdArgument}",
                            arguments = listOf(
                                navArgument(tripIdArgument) { type = NavType.StringType }
                            ),
                            enterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    tween(700)
//                                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                                )
                            },
                            popExitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    tween(700)
//                                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                                )
                            }
                        ) { it ->
                            val tripId = it.arguments?.getString(tripIdArgument)?.toLong() ?: error(
                                "$tripIdArgument cannot be empty"
                            )
                            val tripWithTracks by appViewModel.getTripWithTracksById(tripId)
                                .collectAsState(
                                    TripWithTracks(
                                        trip = Trip(
                                            id = 0,
                                            name = "",
                                            start = "",
                                            end = ""
                                        ),
                                        tracks = emptyList()
                                    )
                                )

                            TripDetailScreen(
                                uiState = appUiState,
                                _tripWithTracks = tripWithTracks,
                                onTripNameClicked = {

                                },
                                onTrackPointClicked = {
                                    val cm =
                                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                    val data = ClipData.newPlainText("Track Point", it)
                                    cm.setPrimaryClip(data)
                                },
                                onPauseTrip = {
                                    appViewModel.pauseTrip()
                                    stopService(intent)
                                },
                                onContinueTrip = {
                                    appViewModel.continueTrip(
                                        NewTripInfo(
                                            car = appUiState.carSelected!!,
                                            tripMode = appUiState.modeSelected!!,
                                            tripName = getString(R.string.default_trip_name),
                                            trackName = getString(R.string.default_track_name),
                                            tripId = it
                                        )
                                    )
                                    startService(intent)
                                },
                                onEndTrip = {
                                    appViewModel.endTrip()
                                    stopService(intent)
                                },
                                onUpdateTrip = {
                                    appViewModel.updateTripNameById(
                                        it.id,
                                        it.name
                                    )
                                },
                                onUpdateTrack = {
                                    appViewModel.updateTrackNameById(
                                        it.id,
                                        it.name
                                    )
                                },
                                onDeleteClicked = { appViewModel.showDeleteTripDialog(it) },
                                onDeleteDismissed = { appViewModel.hideDeleteTripDialog() },
                                onChooseVehicleClicked = {
                                    appViewModel.editTrack(
                                        true,
                                        appUiState.cars.first { car -> car.id == it.carId },
                                        it.id
                                    )
                                    appViewModel.showCarDialog()
                                },
                                onChooseVehicleDismissed = { appViewModel.hideCarDialog() },
                                onChooseModeClicked = {
                                    appViewModel.editTrack(
                                        true,
                                        TripMode.entries.first { mode -> mode.getDisplayName() == it.type },
                                        it.id
                                    )
                                    appViewModel.showModeDialog()

                                },
                                onChooseModeDismissed = { appViewModel.hideModeDialog() },
                                onBack = { onBack() },
                                onMoreClicked = { appViewModel.showMoreMenu() },
                                onMoreDismissed = { appViewModel.hideMoreMenu() },
                                onMoreItem1Clicked = {
                                    /*appViewModel.generateGPXByTripId(it)*/
                                    GlobalScope.launch(Dispatchers.Main) {
                                        val res = appViewModel.generateGPXByTripId(it)
                                        val cm =
                                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                        val data = ClipData.newPlainText(
                                            "GPX for trip ID $it",
                                            res
                                        )
                                        cm.setPrimaryClip(data)
                                        Toast.makeText(
                                            this@MainActivity,
                                            "GPX Copied - ${res}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
