package yong.jianwen.heatmap.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import yong.jianwen.heatmap.CurrentPage
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.local.DataSource
import yong.jianwen.heatmap.ui.AppUiState
import yong.jianwen.heatmap.ui.TripDetailScreen
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.HeatMapTheme
import yong.jianwen.heatmap.ui.theme.chipLabel

@Composable
fun MyChip(
    label1: String,
    modifier: Modifier = Modifier,
    onLabel1Clicked: (() -> Unit)? = null,
    label2: String? = null,
    onLabel2Clicked: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.chip_corner_radius)),
        modifier = Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (onLabel1Clicked == null)
                Modifier
                    .height(IntrinsicSize.Max)
                    .background(CustomTheme.backgroundColors.background)
            else
                Modifier
                    .height(IntrinsicSize.Max)
                    .background(CustomTheme.brandColors.background)
        ) {
            if (onLabel1Clicked == null) {
                Text(
                    text = label1,
                    style = chipLabel,
                    modifier = modifier
                        .fillMaxHeight()
                        .padding(
                            top = dimensionResource(R.dimen.chip_vertical_padding),
                            bottom = dimensionResource(R.dimen.chip_vertical_padding),
                            start = dimensionResource(R.dimen.chip_horizontal_padding),
                            end = dimensionResource(R.dimen.chip_horizontal_padding)
                        )
                        .wrapContentHeight()
                )
            } else {
                Text(
                    text = label1,
                    style = chipLabel,
                    modifier = modifier
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.chip_corner_radius)))    // clip() should be above clickable()
                        .clickable { onLabel1Clicked() }
                        .fillMaxHeight()
                        .padding(
                            top = dimensionResource(R.dimen.chip_vertical_padding),
                            bottom = dimensionResource(R.dimen.chip_vertical_padding),
                            start = dimensionResource(R.dimen.chip_horizontal_padding),
                            end = dimensionResource(R.dimen.chip_horizontal_padding)
                        )
                        .wrapContentHeight()
                )
            }
            if (label2 != null) {
                Text(
                    text = "/",
                    style = chipLabel,
                    modifier = modifier
                        .fillMaxHeight()
                        .padding(
                            top = dimensionResource(R.dimen.chip_vertical_padding),
                            bottom = dimensionResource(R.dimen.chip_vertical_padding)
                        )
                        .wrapContentHeight()
                )
                if (onLabel2Clicked == null) {
                    Text(
                        text = label2,
                        style = chipLabel,
                        modifier = modifier
                            .fillMaxHeight()
                            .padding(
                                top = dimensionResource(R.dimen.chip_vertical_padding),
                                bottom = dimensionResource(R.dimen.chip_vertical_padding),
                                start = dimensionResource(R.dimen.chip_horizontal_padding),
                                end = dimensionResource(R.dimen.chip_horizontal_padding)
                            )
                            .wrapContentHeight()
                    )
                } else {
                    Text(
                        text = label2,
                        style = chipLabel,
                        modifier = modifier
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.chip_corner_radius)))
                            .clickable { onLabel2Clicked() }
                            .fillMaxHeight()
                            .padding(
                                top = dimensionResource(R.dimen.chip_vertical_padding),
                                bottom = dimensionResource(R.dimen.chip_vertical_padding),
                                start = dimensionResource(R.dimen.chip_horizontal_padding),
                                end = dimensionResource(R.dimen.chip_horizontal_padding)
                            )
                            .wrapContentHeight()
                    )
                }
            }
        }
    }
}

/*@PreviewLightDark
@Composable
fun MyChipPreview() {
    HeatMapTheme {
        MyChip(
            label1 = "Driving",
            label2 = "Proton Saga"
        )
    }
}*/

@PreviewLightDark
@Composable
fun TripDetailCompactPreview() {
    HeatMapTheme {
        TripDetailScreen(
            uiState = AppUiState(
                cars = DataSource.getMockCars(),
                trips = DataSource.getMockTrips(),
                currentPage = yong.jianwen.heatmap.CurrentPage.TRIP_DETAIL,
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
            /*northernmost = 0.0,
            southernmost = 0.0,
            easternmost = 0.0,
            westernmost = 0.0,*/
            onBack = { }
        )
    }
}