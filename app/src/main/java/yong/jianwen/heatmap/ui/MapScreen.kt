package yong.jianwen.heatmap.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.coroutine.awaitCameraForCoordinates
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.GenericStyle
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.entity.Track
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.local.DataSource
import yong.jianwen.heatmap.ui.theme.HeatMapTheme

@Composable
fun MapScreen(
    tripWithTracksOrNothing: TripWithTracks?,
    onBack: () -> Unit
) {
    val tripWithTracks = tripWithTracksOrNothing ?: TripWithTracks(
        trip = Trip(
            id = 0,
            name = "",
            start = "",
            end = ""
        ),
        tracks = emptyList()
    )

    val mapViewportState = rememberMapViewportState()

    BackHandler {
        onBack()
    }

    val points = tripWithTracks.tracks.flatMap { track ->
        track.trackSegments.flatMap { trackSegment ->
            trackSegment.trackPoints.map { trackPoint ->
                Point.fromLngLat(trackPoint.longitude, trackPoint.latitude)
            }
        }
    }

    Scaffold {
        Box {
            MapboxMap(
                mapViewportState = mapViewportState,
                mapState = rememberMapState { },
//                compass = { },
                scaleBar = { },
                logo = { },
                attribution = { },
                style = {
                    if (isSystemInDarkTheme()) {
                        GenericStyle(style = Style.DARK)
                    } else {
                        GenericStyle(style = Style.OUTDOORS)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .height(200.dp)
            ) {
                if (tripWithTracksOrNothing != null && tripWithTracksOrNothing.tracks.isNotEmpty()) {
                    MapEffect(Unit) { mapView ->
                        // Use mapView to access the Mapbox Maps APIs not in the Compose extension.
                        // Changes inside `MapEffect` may conflict with Compose states.
                        var northernmost2 = -90.0
                        var southernmost2 = 90.0
                        var easternmost2 = -180.0
                        var westernmost2 = 180.0
                        points.forEach { point ->
                            northernmost2 =
                                Math.max(northernmost2, point.latitude())
                            southernmost2 =
                                Math.min(southernmost2, point.latitude())
                            easternmost2 = Math.max(easternmost2, point.longitude())
                            westernmost2 = Math.min(westernmost2, point.longitude())
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
                                EdgeInsets(150.0, 150.0, 150.0, 150.0)
                            )
                        mapViewportState.setCameraOptions(cameraPosition)
                    }
                }
                val colors =
                    arrayOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Blue,
                        Color.Magenta
                    )
                tripWithTracks.tracks.forEachIndexed { index, track ->
                    PolylineAnnotation(
                        points = track.trackSegments
                            .flatMap { trackSegment -> trackSegment.trackPoints }
                            .map { trackPoint ->
                                Point.fromLngLat(
                                    trackPoint.longitude,
                                    trackPoint.latitude
                                )
                            }
                    ) {
                        lineColor = colors[index % 5]
                        lineWidth = 2.0
                    }
                }

                val marker = rememberIconImage(R.drawable.map)
                //                            points.forEach { point ->
                ////                                PointAnnotation(
                ////                                    point = point
                ////                                ) {
                ////                                    iconImage = marker
                ////                                }
                //                                CircleAnnotation(point = point) {
                //                                    circleColor = Color.Blue
                //                                    circleRadius = 1.0
                //                                }
                //                            }

                /*PointAnnotation(
                    point = Point.fromLngLat(
                        longitudeSelected,
                        latitudeSelected
                    )
                ) {
                    iconImage = marker
                }*/

                /*ViewAnnotation(
                    options = viewAnnotationOptions {
                        geometry(Point.fromLngLat(longitudeSelected, latitudeSelected))
                        annotationAnchor {
                            anchor(ViewAnnotationAnchor.BOTTOM)
                        }
                        allowOverlap(false)
                    }
                ) {
                    Button(
                        onClick = {
                        },
                        colors = ButtonDefaults.buttonColors(
                            Color.Red
                        ),
                    ) {
                        Text(
                            "Click me"
                        )
                    }
                }*/
            }
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal))
                    .align(Alignment.TopStart)
                    .padding(it)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    }
}

@Preview(widthDp = 300)
@Preview(widthDp = 600)
@Preview(widthDp = 840)
@Composable
fun MapScreenCompactPreview() {
    HeatMapTheme {
        MapScreen(
            tripWithTracksOrNothing = DataSource.getMockTripWithTracks(),
            onBack = { }
        )
    }
}
