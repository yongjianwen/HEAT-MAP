package com.example.heatmap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heatmap.MainActivity
import com.example.heatmap.R
import com.example.heatmap.data.entity.Trip
import com.example.heatmap.formatDisplayStartEndTimes
import com.example.heatmap.local.DataSource
import com.example.heatmap.ui.component.FourButtons
import com.example.heatmap.ui.component.MyChip
import com.example.heatmap.ui.theme.HeatMapTheme
import com.example.heatmap.ui.theme.labelMediumSmall

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripRow(
    uiState: AppUiState,
    windowSize: WindowWidthSizeClass,
    trip: Trip,
    onCardClick: (tripId: Long) -> Unit,
    onPauseTrip: () -> Unit,
    onContinueTrip: (tripId: Long) -> Unit,
    onEndTrip: () -> Unit,
    onDeleteClicked: (trip: Trip) -> Unit,
    modifier: Modifier = Modifier,
    cardColor: Color? = null
) {
    /*val animatedColor by animateColorAsState(
        if (uiState.isPaused) Color.Red else Color.Blue,
        label = "color"
    )*/
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
//            .clickable { onCardClick(trip.id) }
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.card_outer_padding))
        /*.drawBehind {
            drawRect(animatedColor)
        }*/
        /*.background(brush = Brush.horizontalGradient(CustomTheme.colors.background))*/
    ) {
        when (windowSize) {
            WindowWidthSizeClass.Compact -> Column(
                modifier = Modifier
                    .clickable { onCardClick(trip.id) }
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.card_medium_content_padding))
            ) {
                Text(
                    text = String.format(stringResource(R.string.trip_id_which), trip.id),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                    // .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
                )
                Text(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    text = trip.name,
                    // style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
                )
                Text(
                    text = formatDisplayStartEndTimes(
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
                    val filteredChips = uiState.chips.filter { chip -> trip.id == chip.tripId }
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
                    .clickable { onCardClick(trip.id) }
                    .padding(dimensionResource(R.dimen.card_medium_content_padding))
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = String.format(stringResource(R.string.trip_id_which), trip.id),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                        // .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
                    )
                    Text(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        text = trip.name,
                        // style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(bottom = dimensionResource(R.dimen.card_inner_vertical_padding))
                    )
                    Text(
                        text = formatDisplayStartEndTimes(
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
                        val filteredChips = uiState.chips.filter { chip -> trip.id == chip.tripId }
                        filteredChips.forEach { chip ->
                            MyChip(
                                label1 = chip.mode,
                                label2 = chip.car.getDisplayName()
                            )
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.card_medium_content_padding))
                )
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
//                    modifier = Modifier.fillMaxHeight()
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
fun TripRowPreview(
    windowSize: WindowWidthSizeClass
) {
    HeatMapTheme {
        TripRow(
            uiState = AppUiState(
                // Ongoing me -> Pause, End
                newTripId = 1,
                isPaused = false,
                // Paused me -> Continue, End
                /*newTripId = 1,
                isPaused = true,*/
                // Not me -> Continue, Delete
                /*newTripId = 2,
                isPaused = true,*/
                chips = DataSource.getChips()
            ),
            windowSize = windowSize,
            trip = DataSource.getMockTrip(),
            onCardClick = { },
            onPauseTrip = { },
            onContinueTrip = { },
            onEndTrip = { },
            onDeleteClicked = { },
//            cardColor = Color.Red
        )
    }
}
