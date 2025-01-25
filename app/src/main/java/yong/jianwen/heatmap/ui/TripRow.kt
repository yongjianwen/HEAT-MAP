package yong.jianwen.heatmap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.formatTripStartEndTimes
import yong.jianwen.heatmap.local.DataSource
import yong.jianwen.heatmap.ui.component.FourButtons
import yong.jianwen.heatmap.ui.component.MyChip
import yong.jianwen.heatmap.ui.theme.HeatMapTheme
import yong.jianwen.heatmap.ui.theme.labelMediumSmall

@Composable
fun TripRow(
    uiState: UiState,
    windowSize: WindowWidthSizeClass,
    trip: Trip,
    onCardClicked: (tripId: Long) -> Unit,
    onPauseTrip: () -> Unit,
    onContinueTrip: (tripId: Long) -> Unit,
    onEndTrip: () -> Unit,
    onDeleteClicked: (trip: Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (uiState.newTripId == trip.id && uiState.isPaused == false)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.card_outer_padding))
    ) {
        when (windowSize) {
            WindowWidthSizeClass.Compact -> Column(
                modifier = Modifier
                    .clickable { onCardClicked(trip.id) }
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.card_medium_content_padding))
                    .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal))
            ) {
                Content(uiState, trip)
                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.card_medium_content_padding))
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FourButtons(
                        uiState = uiState,
                        trip = trip,
                        onContinueTrip = onContinueTrip,
                        onPauseTrip = onPauseTrip,
                        onEndTrip = onEndTrip,
                        onDeleteClicked = onDeleteClicked,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }

            else -> Row(
                modifier = Modifier
                    .clickable { onCardClicked(trip.id) }
                    .padding(dimensionResource(R.dimen.card_medium_content_padding))
                    .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal))
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Content(uiState, trip)
                }
                Spacer(
                    modifier = Modifier.width(dimensionResource(R.dimen.card_medium_content_padding))
                )
                Column(
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    FourButtons(
                        uiState = uiState,
                        trip = trip,
                        onContinueTrip = onContinueTrip,
                        onPauseTrip = onPauseTrip,
                        onEndTrip = onEndTrip,
                        onDeleteClicked = onDeleteClicked,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Content(
    uiState: UiState,
    trip: Trip
) {
    Text(
        text = String.format(stringResource(R.string.trip_id_which), trip.id),
        style = MaterialTheme.typography.labelSmall
    )
    Text(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        text = trip.name,
        modifier = Modifier
            .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
    )
    Text(
        text = formatTripStartEndTimes(
            trip.start,
            trip.end,
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
            uiState.carsAndModesForEachTrip.filter { chip -> trip.id == chip.tripId }
        filteredChips.forEach { chip ->
            MyChip(
                label1 = chip.mode,
                label2 = chip.car.getDisplayName()
            )
        }
    }
}

@Preview(device = Devices.PHONE)
@Composable
fun TripRowPreviewCompact() {
    TripRowPreview(WindowWidthSizeClass.Compact)
}

@Preview(widthDp = 700)
@Composable
fun TripRowPreviewMedium() {
    TripRowPreview(WindowWidthSizeClass.Medium)
}

@Composable
fun TripRowPreview(windowSize: WindowWidthSizeClass) {
    HeatMapTheme {
        TripRow(
            uiState = UiState(
                // Ongoing me -> Pause, End
                newTripId = 1,
                isPaused = false,
                // Paused me -> Continue, End
                /*newTripId = 1,
                isPaused = true,*/
                // Not me -> Continue, Delete
                /*newTripId = 2,
                isPaused = true,*/
                carsAndModesForEachTrip = DataSource.getChips()
            ),
            windowSize = windowSize,
            trip = DataSource.getMockTrip(),
            onCardClicked = { },
            onPauseTrip = { },
            onContinueTrip = { },
            onEndTrip = { },
            onDeleteClicked = { }
        )
    }
}
