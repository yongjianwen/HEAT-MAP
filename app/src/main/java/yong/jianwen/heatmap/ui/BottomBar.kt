package yong.jianwen.heatmap.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import yong.jianwen.heatmap.CurrentPage
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.advancedShadow
import yong.jianwen.heatmap.data.helper.NewTripInfo
import yong.jianwen.heatmap.ui.component.MyBottomBarButton
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.NotoSans

@SuppressLint("NewApi")
@Composable
fun BottomBar(
    appViewModel: AppViewModel,
    uiState: UiState,
    navController: NavController,
    context: Context,
    intent: Intent,
    tripLazyListState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val vibratorManager =
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    val vibrator = vibratorManager.defaultVibrator

    Surface(
        modifier = Modifier
            .advancedShadow(
                alpha = 0.1f,
                shadowBlurRadius = dimensionResource(R.dimen.app_bar_shadow_elevation),
                offsetY = -dimensionResource(R.dimen.app_bar_shadow_elevation)
            )
    ) {
        Column {
            if (uiState.newTripId != -1L) {
                Row(
                    modifier = Modifier
                        .clickable {
                            if (navController.currentBackStackEntry?.destination?.route == CurrentPage.HOME.name) {
                                navController.navigate(
                                    CurrentPage.TRIP_DETAIL.name + "/${uiState.newTripId}"
                                )
                                uiState.currentPage = CurrentPage.TRIP_DETAIL
                            }
                        }
                        .fillMaxWidth()
                        .background(
                            if (uiState.isPaused)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (uiState.isPaused)
                            "Trip ID ${uiState.newTripId}: Paused"
                        else
                            "Trip ID ${uiState.newTripId}: Ongoing",
                        fontFamily = NotoSans,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(CustomTheme.brandColors.background)
                    .fillMaxWidth()
                    .height(80.dp)
                    .windowInsetsPadding(
                        WindowInsets.safeGestures.only(
                            WindowInsetsSides.Horizontal
                        )
                    )
            ) {
                MyBottomBarButton(
                    text = uiState.carSelected?.getDisplayName()
                        ?: stringResource(R.string.choose_vehicle),
                    onClicked = { appViewModel.showCarDialog() },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
                VerticalDivider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                )
                MyBottomBarButton(
                    text = uiState.modeSelected?.getDisplayName()
                        ?: stringResource(R.string.choose_mode),
                    onClicked = { appViewModel.showModeDialog() },
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                )
                if (uiState.newTripId == -1L) {
                    MyBottomBarButton(
                        text = stringResource(R.string.start_trip),
                        onClicked = {
                            vibrator.vibrate(
                                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                            )
                            appViewModel.startTrip(
                                NewTripInfo(
                                    car = uiState.carSelected!!,
                                    tripMode = uiState.modeSelected!!,
                                    tripName = context.getString(R.string.default_trip_name),
                                    trackName = context.getString(R.string.default_track_name),
                                    tripId = -1
                                )
                            )
                            context.startService(intent)
                            coroutineScope.launch {
                                if (tripLazyListState.firstVisibleItemIndex == 0) {
                                    delay(50)
                                }
                                tripLazyListState.animateScrollToItem(index = 0)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.carSelected != null && uiState.modeSelected != null
                    )
                } else {
                    if (!uiState.isPaused) {
                        MyBottomBarButton(
                            text = stringResource(R.string.pause_trip),
                            onClicked = {
                                appViewModel.pauseTrip()
                                context.stopService(intent)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        MyBottomBarButton(
                            text = stringResource(R.string.continue_trip),
                            onClicked = {
                                appViewModel.continueTrip(
                                    NewTripInfo(
                                        car = uiState.carSelected!!,
                                        tripMode = uiState.modeSelected!!,
                                        tripName = context.getString(R.string.default_trip_name),
                                        trackName = context.getString(R.string.default_track_name),
                                        tripId = uiState.newTripId
                                    )
                                )
                                context.startService(intent)
                                coroutineScope.launch {
                                    if (tripLazyListState.firstVisibleItemIndex == 0) {
                                        delay(50)
                                    }
                                    tripLazyListState.animateScrollToItem(index = 0)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.primary,
                        thickness = 2.dp,
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                    )
                    MyBottomBarButton(
                        text = stringResource(R.string.end_trip),
                        onClicked = {
                            appViewModel.endTrip()
                            context.stopService(intent)
                            coroutineScope.launch {
                                if (tripLazyListState.firstVisibleItemIndex == 0) {
                                    delay(50)
                                }
                                tripLazyListState.animateScrollToItem(index = 0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
