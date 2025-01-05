package yong.jianwen.heatmap.ui

import android.view.Gravity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.coroutine.awaitCameraForCoordinates
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.ui.theme.CustomTheme
import yong.jianwen.heatmap.ui.theme.HeatMapTheme

@Composable
fun MapDialog(
    uiState: UiState,
    onDismissRequest: () -> Unit,
    windowSize: WindowWidthSizeClass
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        when (windowSize) {
            WindowWidthSizeClass.Compact -> dialogWindowProvider.window.setGravity(Gravity.TOP)
            else -> dialogWindowProvider.window.setGravity(Gravity.START)
        }

        Card(
            colors = CardDefaults.cardColors(CustomTheme.backgroundColors.background),
            elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.card_medium_elevation)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.card_medium_corner_radius)),
            modifier = when (windowSize) {
                WindowWidthSizeClass.Compact -> Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth(0.9f)
                    .padding(top = 20.dp)

                else -> Modifier
                    .fillMaxHeight(1f)
                    .fillMaxWidth(0.8f)
                    .padding(top = 20.dp, start = 20.dp)
            }
        ) {
            val mapViewportState = rememberMapViewportState { }

            Surface(
                shape = RoundedCornerShape(13.5.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                MapboxMap(
                    mapViewportState = mapViewportState,
                    mapState = rememberMapState {
                        gesturesSettings = GesturesSettings {
                            rotateEnabled = false
                        }
                    },
                    compass = { },
                    scaleBar = { },
                    logo = { },
                    attribution = { },
                    style = {
                        if (isSystemInDarkTheme()) {
                            GenericStyle(style = Style.DARK)
                        } else {
                            GenericStyle(style = Style.LIGHT)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    MapEffect(Unit) { mapView ->
                        // TODO: store the 4 corner points in database for faster retrieval
                        val points = uiState.allTripsWithTracks.flatMap { tripWithTracks ->
                            tripWithTracks.tracks.flatMap { track ->
                                track.trackSegments.flatMap { trackSegment ->
                                    trackSegment.trackPoints.map { trackPoint ->
                                        Point.fromLngLat(
                                            trackPoint.longitude,
                                            trackPoint.latitude
                                        )
                                    }
                                }
                            }
                        }
                        var northernmost2 = -90.0
                        var southernmost2 = 90.0
                        var easternmost2 = -180.0
                        var westernmost2 = 180.0

                        points.forEach { point ->
                            northernmost2 =
                                Math.max(northernmost2, point.latitude())
                            southernmost2 =
                                Math.min(southernmost2, point.latitude())
                            easternmost2 =
                                Math.max(easternmost2, point.longitude())
                            westernmost2 =
                                Math.min(westernmost2, point.longitude())
                        }

                        val cornerPoints = listOf(
                            Point.fromLngLat(westernmost2, southernmost2),
                            Point.fromLngLat(westernmost2, northernmost2),
                            Point.fromLngLat(easternmost2, northernmost2),
                            Point.fromLngLat(easternmost2, southernmost2)
                        )

                        val cameraPosition =
                            mapView.mapboxMap.awaitCameraForCoordinates(
                                cornerPoints,
                                cameraOptions { },
                                EdgeInsets(100.0, 100.0, 100.0, 100.0)
                            )
                        mapViewportState.setCameraOptions(cameraPosition)

                        /*val cameraBoundsOptions =
                            CameraBoundsOptions.Builder()
                                .maxZoom(15.0)
                                .build()
                        mapView.mapboxMap.setBounds(cameraBoundsOptions)*/
                    }

                    uiState.allTripsWithTracks.flatMap { tripWithTracks ->
                        tripWithTracks.tracks.flatMap { track ->
                            track.trackSegments
                        }
                    }.forEach {
                        PolylineAnnotation(
                            points = it.trackPoints.map { trackPoint ->
                                Point.fromLngLat(
                                    trackPoint.longitude,
                                    trackPoint.latitude
                                )
                            }
                        ) {
                            lineColor = Color.Red
                            lineWidth = 4.0
                            lineOpacity = 0.05
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun MapDialogCompactPreview() {
    HeatMapTheme {
        MapDialog(
            uiState = UiState(),
            onDismissRequest = { },
            windowSize = WindowWidthSizeClass.Compact
        )
    }
}

@Preview(widthDp = 900, heightDp = 400)
@Composable
fun MapDialogMediumPreview() {
    HeatMapTheme {
        MapDialog(
            uiState = UiState(),
            onDismissRequest = { },
            windowSize = WindowWidthSizeClass.Medium
        )
    }
}
