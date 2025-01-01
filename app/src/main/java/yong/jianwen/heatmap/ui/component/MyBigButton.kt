package yong.jianwen.heatmap.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.NotoSans

@Composable
fun MyBigButton(
    label: String,
    color: Color?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    isActionButton: Boolean = false
) {
    if (isActionButton) {
        ElevatedButton(
            colors = if (color != null)
                ButtonDefaults.buttonColors(color)
            else
                ButtonDefaults.buttonColors(),
            enabled = enabled,
            onClick = onClick,
            modifier = modifier
                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
        ) {
            MyBigButtonText(label)
        }
    } else if (selected) {
        FilledTonalButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = CustomTheme.brandColors.background,
                contentColor = CustomTheme.brandColors.content
            ),
            enabled = enabled,
            onClick = onClick,
            modifier = modifier
                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
        ) {
            MyBigButtonText(label)
        }
    } else {
        ElevatedButton(
            enabled = enabled,
            onClick = onClick,
            modifier = modifier
                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
        ) {
            MyBigButtonText(label)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyBigButtonText(
    label: String
) {
    Text(
        text = label,
        fontFamily = NotoSans,
        maxLines = 1,
        modifier = Modifier
            .basicMarquee()
            .padding(dimensionResource(R.dimen.big_button_inner_padding))
    )
}
