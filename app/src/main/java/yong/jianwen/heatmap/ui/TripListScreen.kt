package yong.jianwen.heatmap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.local.DataSource
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.HeatMapTheme
import yong.jianwen.heatmap.ui.theme.NotoSans

@Composable
fun TripListScreen(
    uiState: UiState,
    windowSize: WindowWidthSizeClass,
    onPauseTrip: () -> Unit,
    onContinueTrip: (tripId: Long) -> Unit,
    onEndTrip: () -> Unit,
    onCardClicked: (tripId: Long) -> Unit,
    onDeleteClicked: (trip: Trip) -> Unit,
    onMoreClicked: () -> Unit,
    onMoreDismissed: () -> Unit,
    onMoreItem1Clicked: () -> Unit,
    onMoreItem2Clicked: () -> Unit,
    onMoreItem3Clicked: () -> Unit,
    tripLazyListState: LazyListState,
    onSpecialClicked: () -> Unit = { }
) {
    /*val lazyListState = rememberLazyListState()*/
    val coroutineScope = rememberCoroutineScope()

    /*LaunchedEffect(uiState.newTripId) {
        coroutineScope.launch {
            if (lazyListState.firstVisibleItemIndex == 0) {
                delay(50)
            }
            lazyListState.animateScrollToItem(index = 0)
        }
    }*/

    /*val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        var savedValue = SaveMap["test"]
        if (savedValue?.params != "") savedValue = null
        val savedIndex = savedValue?.index ?: 0
        val savedOffset = savedValue?.scrollOffset ?: 0
        LazyListState(savedIndex, savedOffset)
    }
    DisposableEffect(Unit) {
        onDispose {
            val lastIndex = scrollState.firstVisibleItemIndex
            val lastOffset = scrollState.firstVisibleItemScrollOffset
            SaveMap["test"] = KeyParams("", lastIndex, lastOffset)
        }
    }
    LaunchedEffect(uiState.newTripId) {
        coroutineScope.launch {
            if (scrollState.firstVisibleItemIndex == 0) {
                delay(50)
            }
            scrollState.animateScrollToItem(index = 0)
        }
    }*/

    Scaffold(
        topBar = {
            HeatMapAppBar(
                title = stringResource(R.string.app_name),
                onAppBarClicked = {
                    coroutineScope.launch {
//                        lazyListState.animateScrollToItem(index = 0)
//                        animateScrollToTop(lazyListState)
//                        scrollState.animateScrollToItem(index = 0)
                        tripLazyListState.animateScrollToItem(index = 0)
                    }
                },
                showMoreButton = true,
                onMoreClicked = onMoreClicked
            ) {
                DropdownMenu(
                    expanded = uiState.moreExpanded,
                    onDismissRequest = onMoreDismissed,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_medium_corner_radius))
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.map),
//                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = stringResource(R.string.more_actions),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = "View Map",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = onMoreItem1Clicked
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.upload),
                                contentDescription = "Export Data",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = "Export Data",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = onMoreItem2Clicked
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.download),
                                contentDescription = "Import Data",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = "Import Data",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = onMoreItem3Clicked
                    )
                    HorizontalDivider()
                    val context = LocalContext.current
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                text = "HEAT MAP v${
                                    context.packageManager.getPackageInfo(
                                        context.packageName,
                                        0
                                    ).longVersionCode
                                }",
                                fontFamily = NotoSans
                            )
                        },
                        onClick = onSpecialClicked
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(
//                    paddingValues
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .background(CustomTheme.backgroundColors.background)
                    .fillMaxHeight()
            ) {
                if (uiState.allTripsWithTracks.isEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.get_started_label),
                            fontFamily = NotoSans,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .alpha(0.5f)
                                .fillMaxWidth()
                        )
                    }
                } else {
                    val focusRequester = remember { FocusRequester() }
                    LazyColumn(
                        /*state = lazyListState,*/
                        state = /*rememberForeverLazyListState(
                            key = "test",
                            uiState = uiState,
                            coroutineScope = coroutineScope
                        )*/
//                        scrollState,
//                        lazyListState,
                        tripLazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    ) {
                        items(
                            items = uiState.allTripsWithTracks.map { trip -> trip.trip }.reversed(),
                            key = { item -> item.id }
                        ) {
                            TripRow(
                                uiState = uiState,
                                windowSize = windowSize,
                                trip = it,
                                onCardClicked = onCardClicked,
                                onPauseTrip = onPauseTrip,
                                onContinueTrip = { tripId ->
                                    onContinueTrip(tripId)
                                },
                                onEndTrip = onEndTrip,
                                onDeleteClicked = onDeleteClicked
                            )
                        }
                        item {
                            Surface(
                                modifier = Modifier
                                    .height(100.dp)  // for bottom bar
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun TripListCompactPreview() {
    HeatMapTheme {
        TripListScreen(
            uiState = UiState(
                cars = DataSource.getMockCars(),
                allTripsWithTracks = listOf(DataSource.getMockTripWithTracks()),
                newTripId = 4,
                isPaused = false
            ),
            windowSize = WindowWidthSizeClass.Expanded,
            onPauseTrip = { },
            onContinueTrip = { },
            onEndTrip = { },
            onCardClicked = { },
            onDeleteClicked = { },
            onMoreClicked = { },
            onMoreDismissed = { },
            onMoreItem1Clicked = { },
            onMoreItem2Clicked = { },
            onMoreItem3Clicked = { },
            tripLazyListState = rememberLazyListState()
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
    initialFirstVisibleItemScrollOffset: Int = 0,
    uiState: UiState,
    coroutineScope: CoroutineScope
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
    LaunchedEffect(uiState.newTripId) {
        coroutineScope.launch {
            if (scrollState.firstVisibleItemIndex == 0) {
                delay(50)
            }
            scrollState.animateScrollToItem(index = 0)
        }
    }
    return scrollState
}
