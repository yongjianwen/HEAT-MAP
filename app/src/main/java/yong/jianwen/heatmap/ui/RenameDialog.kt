package yong.jianwen.heatmap.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.data.helper.UpdateTrack
import yong.jianwen.heatmap.data.helper.UpdateTrip
import yong.jianwen.heatmap.ui.component.MyDialog
import yong.jianwen.heatmap.ui.theme.NotoSans
import yong.jianwen.heatmap.ui.utility.Keyboard
import yong.jianwen.heatmap.ui.utility.keyboardAsState

@Composable
fun RenameDialog(
    onDismissRequest: () -> Unit,
    screen: Int,
    tripWithTracks: TripWithTracks,
    tripOrTrackName: String,
    trackId: Long,
    onUpdateTrip: (trip: UpdateTrip) -> Unit,
    onUpdateTrack: (track: UpdateTrack) -> Unit,
    onValueChanged: (String) -> Unit
) {
    val keyboard by keyboardAsState()
    var toClose by remember { mutableStateOf(false) }

    MyDialog(
        title = when (screen) {
            0 -> stringResource(R.string.rename_trip)
            1 -> stringResource(R.string.rename_track)
            2 -> stringResource(R.string.delete_vehicle)
            else -> ""
        },
        onDismissRequest = {
            if (keyboard == Keyboard.Opened) {
                toClose = true
            } else {
                onDismissRequest()
            }
        },
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
                    onDismissRequest()
                }

                1 -> {
                    onUpdateTrack(
                        UpdateTrack(
                            id = trackId,
                            name = tripOrTrackName
                        )
                    )
                    onDismissRequest()
                }
            }
        },
    ) {
        if (screen == 0 || screen == 1) {
            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            val textFieldValue =
                remember {
                    mutableStateOf(
                        TextFieldValue(
                            tripOrTrackName,
                            TextRange(tripOrTrackName.length)
                        )
                    )
                }

            LaunchedEffect(toClose) {
                if (toClose) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    toClose = false
                }
            }

            OutlinedTextField(
                value = textFieldValue.value,
                onValueChange = {
                    textFieldValue.value = it.copy(it.text.trimStart())
                    onValueChanged(textFieldValue.value.text)
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                label = {
                    Text(
                        text = if (screen == 0)
                            stringResource(R.string.trip_name)
                        else
                            stringResource(R.string.track_name),
                        fontFamily = NotoSans
                    )
                },
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                maxLines = 5,
                modifier = Modifier
                    .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                    .focusRequester(focusRequester)
            )
        }
    }
}
