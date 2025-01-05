package yong.jianwen.heatmap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
    isDone: Boolean = true,
    onDoneClicked: () -> Unit = { },
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
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal))
            )
        },
        navigationIcon = if (showBackButton) {
            {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        onClick = {
                            if (isDone) {
                                onMoreClicked()
                            } else {
                                onDoneClicked()
                            }
                        },
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal))
                    ) {
                        Icon(
                            imageVector = if (isDone)
                                Icons.Filled.MoreVert
                            else
                                Icons.Filled.Check,
                            contentDescription = if (isDone)
                                stringResource(R.string.more_actions)
                            else
                                stringResource(R.string.done)
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
