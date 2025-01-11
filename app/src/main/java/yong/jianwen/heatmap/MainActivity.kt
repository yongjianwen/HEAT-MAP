package yong.jianwen.heatmap

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.data.helper.NewTripInfo
import yong.jianwen.heatmap.service.LocationService
import yong.jianwen.heatmap.ui.AppViewModel
import yong.jianwen.heatmap.ui.BottomBar
import yong.jianwen.heatmap.ui.CarDialog
import yong.jianwen.heatmap.ui.MapDialog
import yong.jianwen.heatmap.ui.MapScreen
import yong.jianwen.heatmap.ui.ModeDialog
import yong.jianwen.heatmap.ui.TripDetailScreen
import yong.jianwen.heatmap.ui.TripListScreen
import yong.jianwen.heatmap.ui.component.MyDialog
import yong.jianwen.heatmap.ui.theme.HeatMapTheme
import yong.jianwen.heatmap.ui.theme.NotoSans
import java.util.UUID

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeatMapTheme {
                HeatMapApp(
                    context = this
                )
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HeatMapApp(context: Context) {
    Box(
//        modifier = Modifier.padding(0.dp)
    ) {
//    Column(
//        verticalArrangement = Arrangement.spacedBy(0.dp),
//        modifier = Modifier.fillMaxHeight()
//    ) {
        val appViewModel: AppViewModel = viewModel(factory = AppViewModel.factory)
        val uiState by appViewModel.uiState.collectAsState()

        val navController = rememberNavController()

        val intent = Intent(context, LocationService::class.java)
        /*var bottomBarVisible by remember { mutableStateOf(true) }*/

        val tripLazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        var showDialog by rememberSaveable { mutableStateOf(false) }
        var showAnimatedDialog by remember { mutableStateOf(false) }

        LaunchedEffect(showDialog) {
            if (showDialog) {
                showAnimatedDialog = true
            }
        }

        if (showAnimatedDialog) {
            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                val dialogWindow = getDialogWindow()

                SideEffect {
                    dialogWindow.let { window ->
                        window?.setDimAmount(0f)
                        window?.setWindowAnimations(-1)
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    var animateIn by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { animateIn = true }
                    AnimatedVisibility(
                        visible = animateIn && showDialog,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier
                                .pointerInput(Unit) { detectTapGestures { showDialog = false } }
                                .background(Color.Black.copy(alpha = .56f))
                                .fillMaxSize()
                        )
                    }
                    AnimatedVisibility(
                        visible = animateIn && showDialog,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + scaleIn(
                            initialScale = .8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                        exit = slideOutVertically { it / 8 } + fadeOut() + scaleOut(targetScale = .95f)
                        /*enter = slideInVertically(
                            initialOffsetY = { 4000 }
                        ) + expandVertically(
                            expandFrom = Alignment.Bottom
                        ) + scaleIn(
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        ) + fadeIn(
                            initialAlpha = 0f
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { 40 }
                        ) + shrinkVertically(
                            shrinkTowards = Alignment.CenterVertically
                        ) + scaleOut(
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        ) + fadeOut()*/
                    ) {
                        Box(
                            Modifier
                                .pointerInput(Unit) { detectTapGestures { } }
                                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                                .width(300.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(32.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Hello",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "World",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        DisposableEffect(Unit) {
                            onDispose {
                                showAnimatedDialog = false
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
//                .weight(1f)
        ) {

            val onBack = {
                uiState.currentPage = CurrentPage.HOME
                navController.navigateUp()
                appViewModel.showBottomBar()
            }

            val windowSize = calculateWindowSizeClass(context as MainActivity)

            if (uiState.alertExpanded) {
                MyDialog(
                    title = "Error",
                    onDismissRequest = { appViewModel.hideAlertDialog() },
                    button1Label = "Dismiss",
                    onButton1Clicked = {
                        appViewModel.hideAlertDialog()
                    }
                ) {}
            }

            var mapExpanded by rememberSaveable { mutableStateOf(false) }

            if (mapExpanded) {
                MapDialog(
                    uiState = uiState,
                    onDismissRequest = { mapExpanded = false },
                    windowSize = windowSize.widthSizeClass
                )
            }

            if (uiState.carExpanded) {
                CarDialog(
                    uiState = uiState,
                    onDismissRequestAtScreen0 = { appViewModel.hideCarDialog() },
                    onAddNewCar = { appViewModel.createCar(it) },
                    onDeleteCar = { appViewModel.deleteCar(it) },
                    onUpdateCar = { appViewModel.updateCar(it) },
                    onItemSelected = {
                        if (!uiState.isUpdatingCarOrMode) {
                            appViewModel.saveCarSelected(it)
                            appViewModel.hideCarDialog()
                        } else {
                            appViewModel.updateTrackCarIdById(
                                uiState.updatingTrackId,
                                it.id
                            )
                            appViewModel.hideCarDialog()
                        }
                    }
                )
            }

            if (uiState.modeExpanded) {
                ModeDialog(
                    uiState = uiState,
                    onDismissRequest = { appViewModel.hideModeDialog() },
                    onItemSelected = {
                        if (!uiState.isUpdatingCarOrMode) {
                            appViewModel.saveMode(it as TripMode)
                            appViewModel.hideModeDialog()
                        } else {
                            appViewModel.updateTrackTypeById(
                                uiState.updatingTrackId,
                                it as TripMode
                            )
                            appViewModel.hideModeDialog()
                        }
                    }
                )
            }

            if (uiState.deleteTripExpanded) {
                /*DeleteDialog(
                    uiState = uiState,
                    onDismissRequest = { appViewModel.hideDeleteTripDialog() },
                    onButton1Clicked = {
                        appViewModel.deleteTrip(uiState.tripIdToDelete)
                        appViewModel.hideDeleteTripDialog()

                        if (navController.currentBackStackEntry?.destination?.route == CurrentPage.TRIP_DETAIL.name + "/{tripId}") {
                            navController.popBackStack()
                            uiState.currentPage = CurrentPage.HOME
                        }
                    }
                )*/
                MyDialog(
                    title = stringResource(R.string.delete_trip),
                    onDismissRequest = { appViewModel.hideDeleteTripDialog() },
                    button1Label = stringResource(R.string.delete),
                    button1Color = MaterialTheme.colorScheme.error,
                    onButton1Clicked = {
                        if (uiState.tripToDelete != null) {
                            appViewModel.deleteTrip(uiState.tripToDelete!!.id)
                            appViewModel.hideDeleteTripDialog()

                            if (navController.currentBackStackEntry?.destination?.route == CurrentPage.TRIP_DETAIL.name + "/{tripId}") {
                                navController.popBackStack()
                                uiState.currentPage = CurrentPage.HOME
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(
                            R.string.confirm_delete_trip,
                            uiState.tripToDelete?.id ?: "-1"
                        ),
                        fontFamily = NotoSans,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.deleteTrackExpanded) {
                MyDialog(
                    title = stringResource(R.string.delete_track),
                    onDismissRequest = { appViewModel.hideDeleteTrackDialog() },
                    button1Label = stringResource(R.string.delete),
                    button1Color = MaterialTheme.colorScheme.error,
                    onButton1Clicked = {
                        if (uiState.trackToDelete != null) {
                            appViewModel.deleteTrack(uiState.trackToDelete!!)
                            appViewModel.hideDeleteTrackDialog()
                        }
                    }
                ) {
                    Text(
                        text = stringResource(
                            R.string.confirm_delete_track,
                            uiState.trackToDelete?.id ?: "-1"
                        ),
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
                            // spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                        )
                    },
                    popEnterTransition = {
                        return@composable slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(700)
                            // spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                        )
                    }
                ) {
                    appViewModel.editTrack(false)

                    TripListScreen(
                        uiState = uiState,
                        windowSize = windowSize.widthSizeClass,
                        onPauseTrip = {
                            appViewModel.pauseTrip()
                            context.stopService(intent)
                        },
                        onContinueTrip = {
                            appViewModel.continueTrip(
                                NewTripInfo(
                                    car = uiState.carSelected!!,
                                    tripMode = uiState.modeSelected!!,
                                    tripName = context.getString(R.string.default_trip_name),
                                    trackName = context.getString(R.string.default_track_name),
                                    tripId = it
                                )
                            )
                            context.startService(intent)
                            coroutineScope.launch {
                                if (tripLazyListState.firstVisibleItemIndex == 0) {
                                    delay(50)
                                }
                                tripLazyListState.animateScrollToItem(index = 0)
                            }
                        },
                        onEndTrip = {
                            appViewModel.endTrip()
                            context.stopService(intent)
                            coroutineScope.launch {
                                if (tripLazyListState.firstVisibleItemIndex == 0) {
                                    delay(50)
                                }
                                tripLazyListState.animateScrollToItem(index = 0)
                            }
                        },
                        onCardClicked = {
                            if (navController.currentBackStackEntry?.destination?.route == CurrentPage.HOME.name) {
                                navController.navigate(
                                    CurrentPage.TRIP_DETAIL.name + "/$it"
                                )
                                uiState.currentPage = CurrentPage.TRIP_DETAIL
                                appViewModel.hideBottomBar()
                            }
                        },
                        onDeleteClicked = { appViewModel.showDeleteTripDialog(it) },
                        onMoreClicked = { appViewModel.showMoreMenu() },
                        onMoreDismissed = { appViewModel.hideMoreMenu() },
                        onMoreItem1Clicked = {
                            appViewModel.hideMoreMenu()
//                            mapExpanded = true
                            showDialog = true
                        },
                        onMoreItem2Clicked = {
                            appViewModel.exportData()
                        },
                        onMoreItem3Clicked = {
                            appViewModel.importData()
                        },
                        tripLazyListState = tripLazyListState,
                        /*onSpecialClicked = { appViewModel.resetAllUUIDs() }*/
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
                            // spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                        )
                    },
                    exitTransition = {
                        return@composable slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(700)
                            // spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                        )
                    },
                    popEnterTransition = {
                        return@composable slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(700)
                            // spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
                        )
                    },
                    popExitTransition = {
                        return@composable slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(700)
                            // spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
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
                        uiState = uiState,
                        tripWithTracksOrNothing = tripWithTracks,
                        onTrackPointClicked = {
                            val cm =
                                context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
                            val data = ClipData.newPlainText("Track Point", it)
                            cm.setPrimaryClip(data)
                        },
                        onPauseTrip = {
                            appViewModel.pauseTrip()
                            context.stopService(intent)
                        },
                        onContinueTrip = {
                            appViewModel.continueTrip(
                                NewTripInfo(
                                    car = uiState.carSelected!!,
                                    tripMode = uiState.modeSelected!!,
                                    tripName = context.getString(R.string.default_trip_name),
                                    trackName = context.getString(R.string.default_track_name),
                                    tripId = it
                                )
                            )
                            context.startService(intent)
                        },
                        onEndTrip = {
                            appViewModel.endTrip()
                            context.stopService(intent)
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
                        onChooseVehicleClicked = {
                            appViewModel.editTrack(
                                true,
                                uiState.cars.first { car -> car.id == it.carId },
                                it.id
                            )
                            appViewModel.showCarDialog()
                        },
                        onChooseModeClicked = {
                            appViewModel.editTrack(
                                true,
                                TripMode.entries.first { mode -> mode.getDisplayName() == it.type },
                                it.id
                            )
                            appViewModel.showModeDialog()
                        },
                        onBack = { onBack() },
                        onMoreClicked = { appViewModel.showMoreMenu() },
                        onMoreDismissed = { appViewModel.hideMoreMenu() },
                        onMoreItem1Clicked = {
                            GlobalScope.launch(Dispatchers.Main) {
                                val res = appViewModel.generateGPXByTripId(it)
                                val cm =
                                    context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
                                val data = ClipData.newPlainText(
                                    "GPX for trip ID $it",
                                    res
                                )
                                cm.setPrimaryClip(data)
                                Toast.makeText(
                                    context,
                                    "GPX Copied - ${res}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onDeleteTrackClicked = { appViewModel.showDeleteTrackDialog(it) },
//                        onDeleteTrack = { appViewModel.deleteTrack(it) }
                        onMapClicked = {
                            if (navController.currentBackStackEntry?.destination?.route?.startsWith(
                                    CurrentPage.TRIP_DETAIL.name
                                ) == true
                            ) {
                                navController.navigate(CurrentPage.MAP.name + "/$it")
                                uiState.currentPage = CurrentPage.MAP
                            }
                        }
                    )
                }

                composable(
                    route = CurrentPage.MAP.name + "/{$tripIdArgument}",
                    arguments = listOf(
                        navArgument(tripIdArgument) { type = NavType.StringType }
                    ),
                    enterTransition = {
                        return@composable slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start, tween(700)
                        )
                    },
                    popExitTransition = {
                        return@composable slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End, tween(700)
                        )
                    }
                ) {
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

                    MapScreen(
                        tripWithTracksOrNothing = tripWithTracks,
                        onBack = {
                            uiState.currentPage = CurrentPage.TRIP_DETAIL
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = uiState.bottomBarVisible,
            enter = slideInVertically(
                initialOffsetY = { -40 }
            ) + expandVertically(
                expandFrom = Alignment.Top
            )/* + scaleIn(
                transformOrigin = TransformOrigin(0.5f, 0f)
            )*//* + fadeIn(
                initialAlpha = 0.3f
            )*/,
            exit = slideOutVertically(
                targetOffsetY = { 40 },
                animationSpec = tween(
                    durationMillis = 500,
                    delayMillis = 500
                )//, easing = FastOutLinearInEasing)
            ) + shrinkVertically(
                shrinkTowards = Alignment.CenterVertically,
                animationSpec = tween(
                    durationMillis = 500,
                    delayMillis = 500
                )//, easing = FastOutLinearInEasing)
            )/* + scaleOut(
                transformOrigin = TransformOrigin(0.5f, 1f),
                animationSpec = tween(durationMillis = 500, delayMillis = 500)//, easing = FastOutLinearInEasing)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 450, delayMillis = 500)//, easing = LinearOutSlowInEasing)
            )*/,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            BottomBar(
                appViewModel = appViewModel,
                uiState = uiState,
                navController = navController,
                context = context,
                intent = intent,
                tripLazyListState = tripLazyListState
            )
        }
    }
}

@Composable
fun AnimatedTransitionDialog(
    onDismissRequest: () -> Unit,
    contentAlignment: Alignment = Alignment.Center,
    animateTrigger: Boolean,
    content: @Composable () -> Unit,
) {
    /*val animateTrigger = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        launch {
            delay(500)
            animateTrigger.value = true
        }
    }*/
    LocalContext.current.resources.updateConfiguration(
        LocalConfiguration.current,
        LocalContext.current.resources.displayMetrics
    )
    if (animateTrigger)
        Dialog(onDismissRequest = onDismissRequest) {
            val dialogWindow = getDialogWindow()

            SideEffect {
                dialogWindow.let { window ->
                    window?.setDimAmount(0f)
                    window?.setWindowAnimations(-1)
                }
            }

            Box(
                contentAlignment = contentAlignment,
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedScaleInTransition(visible = animateTrigger) {
                    content()
                }
            }
        }
}

@ReadOnlyComposable
@Composable
fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window


@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun AnimatedScaleInTransition(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(500.toInt())
        ),
        exit = scaleOut(
            animationSpec = tween(500.toInt())
        ),
        content = content
    )
}
