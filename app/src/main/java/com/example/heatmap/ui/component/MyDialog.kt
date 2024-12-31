package com.example.heatmap.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.heatmap.R
import com.example.heatmap.local.DataSource
import com.example.heatmap.ui.theme.HeatMapTheme
import com.example.heatmap.ui.theme.NotoSans

@Composable
fun MyDialog(
    title: String,
    onDismissRequest: () -> Unit,
    backgroundColor: Color? = null,
    button1Label: String? = null,
    button1Color: Color? = null,
    button1Enabled: Boolean = true,
    onButton1Clicked: (() -> Unit)? = null,
    button2Color: Color? = null,
    button2Enabled: Boolean = true,
    button2Label: String? = null,
    onButton2Clicked: (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            colors = if (backgroundColor != null)
                CardDefaults.cardColors(backgroundColor)
            else
                CardDefaults.cardColors(),
            elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.card_medium_elevation)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
            modifier = Modifier
                .heightIn(0.dp, dimensionResource(R.dimen.dialog_max_height))
        ) {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.card_medium_content_padding))
            ) {
                Text(
                    text = title,
                    fontFamily = NotoSans,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopCenter)
                        .padding(bottom = dimensionResource(R.dimen.card_medium_content_padding))
                )
                content(
                    Modifier.weight(1f, fill = false)
                )
                if (
                    (button1Label != null && onButton1Clicked != null)
                    || (button2Label != null && onButton2Clicked != null)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.big_button_separation)),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = dimensionResource(R.dimen.card_small_content_padding))
                    ) {
                        if (button1Label != null && onButton1Clicked != null) {
                            MyBigButton(
                                label = button1Label,
                                color = button1Color,
                                enabled = button1Enabled,
                                onClick = onButton1Clicked,
                                modifier = Modifier.weight(1f),
                                isActionButton = true
                            )
                        }
                        if (button2Label != null && onButton2Clicked != null) {
                            MyBigButton(
                                label = button2Label,
                                color = button2Color,
                                enabled = button2Enabled,
                                onClick = onButton2Clicked,
                                modifier = Modifier.weight(1f),
                                isActionButton = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun MyDialogPreview() {
    HeatMapTheme {
        MyDialog(
            title = "Test Loooooooooooooooooooooooooooooooong Title",
            onDismissRequest = { },
            button1Label = "Loooooooooooooooooooooooooooooooong Button 1",
            onButton1Clicked = { },
            button2Label = "Button 2",
            onButton2Clicked = { }
        ) {
            MyDialogSelectableList(
                items = DataSource.getMockCars(),
                onItemSelected = { },
                modifier = it,
                selectedItem = DataSource.getMockCars()[2]
            ) {}
        }
    }
}
