package yong.jianwen.heatmap.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.ui.component.MyDialog
import yong.jianwen.heatmap.ui.theme.NotoSans

@Composable
fun DeleteTripDialog(
    uiState: UiState,
    onDismissRequest: () -> Unit,
    onButton1Clicked: () -> Unit
) {
    MyDialog(
        title = stringResource(R.string.delete_trip),
        onDismissRequest = onDismissRequest,
        button1Label = stringResource(R.string.delete),
        button1Color = MaterialTheme.colorScheme.error,
        onButton1Clicked = onButton1Clicked
    ) {
        Text(
            text = stringResource(R.string.confirm_delete_trip, uiState.tripIdToDelete),
            fontFamily = NotoSans,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
