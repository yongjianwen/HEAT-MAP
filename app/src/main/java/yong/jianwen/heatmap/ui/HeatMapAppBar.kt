package yong.jianwen.heatmap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.NotoSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatMapAppBar(
    title: String,
    onAppBarClicked: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    showMoreButton: Boolean = false,
    onBackClicked: () -> Unit = { },
    onMoreClicked: () -> Unit = { },
    dropdownMenuContent: @Composable () -> Unit = { }
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CustomTheme.brandColors.background,
            navigationIconContentColor = CustomTheme.brandColors.content,
            titleContentColor = CustomTheme.brandColors.content,
            actionIconContentColor = CustomTheme.brandColors.content
        ),
        title = {
            Text(
                text = title,
                fontFamily = NotoSans,
                fontWeight = FontWeight.Black
            )
        },
        navigationIcon = if (showBackButton) {
            {
                IconButton(
                    onClick = onBackClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        } else {
            {}
        },
        actions = if (showMoreButton) {
            {
                Box {
                    IconButton(
                        onClick = onMoreClicked
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.more_actions)
                        )
                    }
                    dropdownMenuContent()
                }
            }
        } else {
            {}
        },
        modifier = modifier
            .shadow(dimensionResource(R.dimen.app_bar_shadow_elevation))
            .clickable { onAppBarClicked() }
    )
}
