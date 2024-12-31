package com.example.heatmap.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.heatmap.R
import com.example.heatmap.data.entity.Trip
import com.example.heatmap.ui.AppUiState
import com.example.heatmap.ui.theme.NotoSans

@Composable
fun FourButtons(
    uiState: AppUiState,
    trip: Trip,
    onContinueTrip: (tripId: Long) -> Unit,
    onPauseTrip: () -> Unit,
    onEndTrip: () -> Unit,
    onDeleteClicked: (trip: Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    val tripId = trip.id

    if (uiState.newTripId == tripId && !uiState.isPaused) {
        Button(
            onClick = onPauseTrip,
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.pause_trip),
                fontFamily = NotoSans
            )
        }
    } else {
        Button(
            onClick = { onContinueTrip(tripId) },
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.continue_trip),
                fontFamily = NotoSans
            )
        }
    }
    if (uiState.newTripId != tripId) {
        Button(
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            onClick = { onDeleteClicked(trip) },
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.delete),
                fontFamily = NotoSans
            )
        }
    } else {
        Button(
            onClick = onEndTrip,
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.end_trip),
                fontFamily = NotoSans
            )
        }
    }
}
