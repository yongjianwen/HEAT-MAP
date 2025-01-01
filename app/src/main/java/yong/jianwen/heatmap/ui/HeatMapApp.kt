package yong.jianwen.heatmap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import yong.jianwen.heatmap.data.Selectable
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.ui.theme.HeatMapTheme

@Composable
fun HeatMapApp(
    getCurrentLocation: (onSuccess: (Pair<Double, Double>) -> Unit) -> Unit,
    startService: () -> Unit,
    stopService: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appViewModel: AppViewModel = viewModel(factory = AppViewModel.factory)
    val appUiState by appViewModel.appUiState.collectAsState()

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var modeExpanded by remember { mutableStateOf(false) }
    var carExpanded by remember { mutableStateOf(false) }
    var tripModeSelected by remember { mutableStateOf(TripMode.DRIVING) }
    var carSelected: Car? by remember { mutableStateOf(null) }
    var gpxString by remember { mutableStateOf("") }

    val modes = TripMode.entries
    val cars = appUiState.cars

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            MyDropdownMenu(
                buttonLabel = "Select Mode",
                onButtonClick = { modeExpanded = true },
                items = modes,
                expanded = modeExpanded,
                onDismissRequest = { modeExpanded = false },
                onItemSelected = {
                    tripModeSelected = it as TripMode
                    modeExpanded = false
                }
            )
            MyDropdownMenu(
                buttonLabel = "Select Car",
                onButtonClick = { carExpanded = true },
                items = cars,
                expanded = carExpanded,
                onDismissRequest = { carExpanded = false },
                onItemSelected = {
                    carSelected = it as Car
                    carExpanded = false
                }
            )
        }
        Spacer(modifier = Modifier)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (modes.isNotEmpty()) {
                Text(
                    text = "Mode:\n${tripModeSelected.getDisplayName()}",
                    textAlign = TextAlign.Center
                )
            }
            if (cars.isNotEmpty() && carSelected != null) {
                Text(
                    text = "Car:\n${carSelected!!.getDisplayName()}",
                    textAlign = TextAlign.Center
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = latitude
            )
            Text(
                text = longitude
            )
            Text(
                text = gpxString
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    getCurrentLocation {
                        latitude = it.first.toString()
                        longitude = it.second.toString()
                    }
                },
                modifier = Modifier
//                    .padding(bottom = 50.dp)
            ) {
                Text(
                    text = "Get Current Location"
                )
            }
            Button(
                onClick = {
//                    appViewModel.insertCar()
                },
            ) {
                Text(
                    text = "Save Changes"
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
//                    appViewModel.startTrip(tripModeSelected, carSelected!!)
                },
                modifier = Modifier
//                    .padding(bottom = 50.dp)
            ) {
                Text(
                    text = "Start Trip"
                )
            }
            Button(
                onClick = {
//                    Log.d("Test1", appViewModel.generateGPX(1))
                },
                modifier = Modifier
//                    .padding(bottom = 50.dp)
            ) {
                Text(
                    text = "Generate GPX"
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    startService()
                },
                modifier = Modifier
                    .padding(bottom = 50.dp)
            ) {
                Text(
                    text = "Start Service"
                )
            }
            Button(
                onClick = {
                    stopService()
                },
                modifier = Modifier
                    .padding(bottom = 50.dp)
            ) {
                Text(
                    text = "Stop Service"
                )
            }
        }
    }
}

@Composable
fun MyDropdownMenu(
    buttonLabel: String,
    onButtonClick: () -> Unit,
    items: List<Selectable>,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onItemSelected: (selectable: Selectable) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Button(
            onClick = onButtonClick
        ) {
            Text(
                text = buttonLabel
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = it.getDisplayName()
                        )
                    },
                    onClick = { onItemSelected(it) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LocationCompactPreview() {
    HeatMapTheme {
        HeatMapApp({ }, { }, { })
    }
}

//@Preview(showBackground = true, showSystemUi = true, widthDp = 700)
//@Composable
//fun LocationMediumPreview() {
//    HeatMapTheme {
//        AccessLocation({})
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 1000)
//@Composable
//fun LocationExpandedPreview() {
//    HeatMapTheme {
//        AccessLocation({})
//    }
//}
