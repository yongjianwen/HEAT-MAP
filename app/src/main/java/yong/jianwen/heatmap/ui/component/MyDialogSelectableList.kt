package yong.jianwen.heatmap.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import yong.jianwen.heatmap.data.Selectable

@Composable
fun MyDialogSelectableList(
    items: List<Selectable>,
    onItemSelected: (selectable: Selectable) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: Selectable? = null,
    content: @Composable (Selectable) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = items
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                MyBigButton(
                    label = it.getDisplayName(),
                    color = null,
                    enabled = true,
                    onClick = { onItemSelected(it) },
                    modifier = Modifier.weight(1f),
                    selected = selectedItem == it
                )
                content(it)
            }
        }
    }
}
