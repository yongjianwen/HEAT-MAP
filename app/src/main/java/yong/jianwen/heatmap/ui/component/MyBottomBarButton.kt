package yong.jianwen.heatmap.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.NotoSans

@Composable
fun MyBottomBarButton(
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
                .background(CustomTheme.brandColors.background)
                .padding(5.dp)
                .wrapContentHeight()
        )
    }
}