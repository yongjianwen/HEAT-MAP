package yong.jianwen.heatmap.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.Selectable
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.ui.component.MyDialog
import yong.jianwen.heatmap.ui.component.MyDialogSelectableList

@Composable
fun ModeDialog(
    uiState: UiState,
    onDismissRequest: () -> Unit,
    onItemSelected: (Selectable) -> Unit
) {
    MyDialog(
        title = if (!uiState.isUpdatingCarOrMode)
            stringResource(R.string.choose_mode)
        else
            stringResource(R.string.update_mode),
        onDismissRequest = onDismissRequest
    ) { mod ->
        MyDialogSelectableList(
            items = TripMode.entries,
            onItemSelected = { onItemSelected(it) },
            modifier = mod,
            selectedItem = if (!uiState.isUpdatingCarOrMode)
                uiState.modeSelected
            else
                uiState.updatingCarOrModeSelected as TripMode
        ) {}
    }
}
