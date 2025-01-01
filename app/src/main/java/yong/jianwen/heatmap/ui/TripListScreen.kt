package yong.jianwen.heatmap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.advancedShadow
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.local.DataSource
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.HeatMapTheme
import yong.jianwen.heatmap.ui.theme.NotoSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TripListScreen(
    uiState: AppUiState,
    windowSize: WindowWidthSizeClass,
    onCarSelected: (car: Car) -> Unit,
    onModeSelected: (mode: TripMode) -> Unit,
    onAddNewCar: (Car) -> Unit,
    onDeleteCar: (Int) -> Unit,
    onClearCar: () -> Unit,
    onUpdateCar: (Car) -> Unit,
    onStartNewTrip: () -> Unit,
    onPauseTrip: () -> Unit,
    onContinueTrip: (tripId: Long) -> Unit,
    onEndTrip: () -> Unit,
    onDeleteTrip: (Long) -> Unit,
    onCardClicked: (tripId: Long) -> Unit,
    onDeleteClicked: (trip: Trip) -> Unit,
    onDeleteDismissed: () -> Unit,
    onChooseVehicleClicked: () -> Unit,
    onChooseVehicleDismissed: () -> Unit,
    onChooseModeClicked: () -> Unit,
    onChooseModeDismissed: () -> Unit,
    onMoreClicked: () -> Unit,
    onMoreDismissed: () -> Unit,
    onMoreItem1Clicked: () -> Unit,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    modifier: Modifier = Modifier,
    onGenerateGPX: () -> Unit = { }
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.newTripId) {
        coroutineScope.launch {
            if (lazyListState.firstVisibleItemIndex == 0) {
                delay(50)
            }
            lazyListState.animateScrollToItem(index = 0)
        }
    }

    Scaffold(
        topBar = {
            HeatMapAppBar(
                title = stringResource(R.string.app_name),
                onAppBarClicked = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(index = 0)
                    }
                },
                showMoreButton = true,
                onMoreClicked = onMoreClicked
            ) {
                DropdownMenu(
                    expanded = uiState.moreExpanded,
                    onDismissRequest = onMoreDismissed
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = stringResource(R.string.more_actions)
                            )
                        },
                        text = {
                            Text(
                                text = "View Heat Map",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = onMoreItem1Clicked
                    )
                    Divider()
                    DropdownMenuItem(
                        leadingIcon = { },
                        text = {
                            Text(
                                text = "Export Data",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = onExportData
                    )
                    DropdownMenuItem(
                        leadingIcon = { },
                        text = {
                            Text(
                                text = "Import Data",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = onImportData
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues
//                    top = paddingValues.calculateTopPadding(),
//                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .background(CustomTheme.backgroundColors.background)
                    .fillMaxHeight()
            ) {
                if (uiState.trips.isEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(2f)
                    ) {
                        Text(
                            text = "Start a new trip",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                } else {
                    LazyColumn(
                        /*state = lazyListState,*/
                        state = rememberForeverLazyListState(key = "test"),
                        modifier = Modifier
                            .weight(2f)
                    ) {
                        items(
                            items = uiState.trips.reversed(),
                            key = { item -> item.id }
                        ) {
                            TripRow(
                                uiState = uiState,
                                windowSize = windowSize,
                                trip = it,
                                onCardClick = onCardClicked,
                                onPauseTrip = onPauseTrip,
                                onContinueTrip = { tripId ->
                                    onContinueTrip(tripId)
                                },
                                onEndTrip = onEndTrip,
                                onDeleteClicked = onDeleteClicked,
                                cardColor = if (uiState.newTripId == it.id)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else null
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .advancedShadow(
                            alpha = 0.1f,
                            shadowBlurRadius = dimensionResource(R.dimen.app_bar_shadow_elevation),
                            offsetY = -dimensionResource(R.dimen.app_bar_shadow_elevation)
                        )
                ) {
                    Column {
                        if (uiState.newTripId != -1L) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        /*coroutineScope.launch {
                                            lazyListState.animateScrollToItem(0)
                                        }*/
                                        onCardClicked(uiState.newTripId)
                                    }
                                    .fillMaxWidth()
                                    .background(
                                        if (uiState.isPaused)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else
                                            MaterialTheme.colorScheme.errorContainer
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = if (uiState.isPaused)
                                        "Trip ID ${uiState.newTripId}: Paused"
                                    else
                                        "Trip ID ${uiState.newTripId}: Ongoing",
                                    fontFamily = NotoSans,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            BottomBarButton(
                                text = uiState.carSelected?.getDisplayName()
                                    ?: stringResource(R.string.choose_vehicle),
                                onClicked = onChooseVehicleClicked,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            )
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight(0.8f)
                                    .width(1.dp)
                            )
                            BottomBarButton(
                                text = uiState.modeSelected?.getDisplayName()
                                    ?: stringResource(R.string.choose_mode),
                                onClicked = onChooseModeClicked,
                                modifier = Modifier.weight(1f)
                            )
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight(0.8f)
                                    .width(1.dp)
                            )
                            if (uiState.newTripId == -1L) {
                                BottomBarButton(
                                    text = stringResource(R.string.start_trip),
                                    onClicked = onStartNewTrip,
                                    modifier = Modifier.weight(1f)
                                )
                                /*Divider(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                )
                                BottomBarButton(
                                    text = "GPX",
                                    onClicked = onGenerateGPX,
                                    modifier = Modifier.weight(1f)
                                )*/
                            } else {
                                if (!uiState.isPaused) {
                                    BottomBarButton(
                                        text = stringResource(R.string.pause_trip),
                                        onClicked = onPauseTrip,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    BottomBarButton(
                                        text = stringResource(R.string.continue_trip),
                                        onClicked = {
                                            onContinueTrip(uiState.newTripId)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Divider(
                                    modifier = Modifier
                                        .fillMaxHeight(0.8f)
                                        .width(1.dp)
                                )
                                BottomBarButton(
                                    text = stringResource(R.string.end_trip),
                                    onClicked = onEndTrip,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarButton(
    text: String,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClicked() }
            .fillMaxSize()
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = NotoSans,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(5.dp)
                .wrapContentHeight()
        )
    }
}

@PreviewLightDark
@Composable
fun TripListCompactPreview() {
    HeatMapTheme {
        TripListScreen(
            uiState = AppUiState(
                cars = DataSource.getMockCars(),
                trips = DataSource.getMockTrips(),
                newTripId = 4,
                isPaused = true
            ),
            windowSize = WindowWidthSizeClass.Expanded,
            onCarSelected = { },
            onModeSelected = { },
            onAddNewCar = { },
            onDeleteCar = { },
            onClearCar = { },
            onUpdateCar = { },
            onStartNewTrip = { },
            onPauseTrip = { },
            onContinueTrip = { },
            onEndTrip = { },
            onDeleteTrip = { },
            onCardClicked = { },
            onDeleteClicked = { },
            onDeleteDismissed = { },
            onChooseVehicleClicked = { },
            onChooseVehicleDismissed = { },
            onChooseModeClicked = { },
            onChooseModeDismissed = { },
            onMoreClicked = { },
            onMoreDismissed = { },
            onMoreItem1Clicked = { },
            onExportData = { },
            onImportData = { }
        )
    }
}

private val SaveMap = mutableMapOf<String, KeyParams>()

private data class KeyParams(
    val params: String = "",
    val index: Int,
    val scrollOffset: Int
)

@Composable
fun rememberForeverLazyListState(
    key: String,
    params: String = "",
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
): LazyListState {
    val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        var savedValue = SaveMap[key]
        if (savedValue?.params != params) savedValue = null
        val savedIndex = savedValue?.index ?: initialFirstVisibleItemIndex
        val savedOffset = savedValue?.scrollOffset ?: initialFirstVisibleItemScrollOffset
        LazyListState(
            savedIndex,
            savedOffset
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            val lastIndex = scrollState.firstVisibleItemIndex
            val lastOffset = scrollState.firstVisibleItemScrollOffset
            SaveMap[key] = KeyParams(params, lastIndex, lastOffset)
        }
    }
    return scrollState
}
