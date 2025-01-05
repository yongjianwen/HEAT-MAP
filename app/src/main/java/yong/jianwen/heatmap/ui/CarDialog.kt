package yong.jianwen.heatmap.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.ui.component.MyDialog
import yong.jianwen.heatmap.ui.component.MyDialogSelectableList
import yong.jianwen.heatmap.ui.component.MyDialogText
import yong.jianwen.heatmap.ui.theme.NotoSans
import yong.jianwen.heatmap.ui.utility.Keyboard
import yong.jianwen.heatmap.ui.utility.keyboardAsState

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun CarDialog(
    uiState: UiState,
    onDismissRequestAtScreen0: () -> Unit,
    onAddNewCar: (Car) -> Unit,
    onDeleteCar: suspend (Int) -> Boolean,
    onUpdateCar: (Car) -> Unit,
    onItemSelected: (Car) -> Unit
) {
    var screen by rememberSaveable { mutableIntStateOf(0) }

    var carIdToEdit by rememberSaveable { mutableIntStateOf(-1) }

    var carIdToDelete by rememberSaveable { mutableIntStateOf(-1) }
    var carNameToDelete by rememberSaveable { mutableStateOf("") }

    var manufacturer by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var numberPlate by rememberSaveable { mutableStateOf("") }

    var toolVisible by remember { mutableStateOf(false) }

    val keyboard by keyboardAsState()
    var toClose by remember { mutableStateOf(false) }

    MyDialog(
        title = when (screen) {
            -1 -> "Error"
            0 -> if (!uiState.isUpdatingCarOrMode)
                stringResource(R.string.choose_vehicle)
            else
                stringResource(R.string.update_vehicle)

            1 -> stringResource(R.string.add_new_vehicle)
            2 -> stringResource(R.string.delete_vehicle)
            3 -> stringResource(R.string.edit_vehicle)
            else -> ""
        },
        onDismissRequest = {
            when (screen) {
                0 -> {
                    onDismissRequestAtScreen0()
                }

                1, 3 -> {
                    if (keyboard == Keyboard.Opened) {
                        toClose = true
                    } else {
                        screen = 0
                    }
                }

                else -> screen = 0
            }
        },
        backgroundColor = if (screen == -1)
            MaterialTheme.colorScheme.error
        else
            null,
        button1Label = when (screen) {
            -1 -> stringResource(R.string.dismiss)
            0 -> stringResource(R.string.add_vehicle)
            1 -> stringResource(R.string.save)
            2 -> stringResource(R.string.delete)
            3 -> stringResource(R.string.save)
            else -> null
        },
        button1Color = when (screen) {
            -1 -> MaterialTheme.colorScheme.onErrorContainer
            2 -> MaterialTheme.colorScheme.error
            else -> null
        },
        button1Enabled = when (screen) {
            1 -> manufacturer.isNotBlank() && model.isNotBlank() && numberPlate.isNotBlank()
            3 -> manufacturer.isNotBlank() && model.isNotBlank() && numberPlate.isNotBlank()
            else -> true
        },
        onButton1Clicked = {
            when (screen) {
                -1 -> {
                    screen = 0
                }

                0 -> {
                    manufacturer = ""
                    model = ""
                    numberPlate = ""
                    screen = 1
                }

                1 -> {
                    onAddNewCar(
                        Car(
                            id = 0,
                            registrationNumber = numberPlate.trim(),
                            manufacturer = manufacturer.trim(),
                            model = model.trim()
                        )
                    )
                    manufacturer = ""
                    model = ""
                    numberPlate = ""
                    screen = 0
                }

                2 -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        screen = if (onDeleteCar(carIdToDelete)) {
                            0
                        } else {
                            -1
                        }
                    }
                }

                3 -> {
                    onUpdateCar(
                        Car(
                            id = carIdToEdit,
                            registrationNumber = numberPlate,
                            manufacturer = manufacturer,
                            model = model
                        )
                    )
                    screen = 0
                }
            }
        },
        button2Label = when (screen) {
            0 -> stringResource(R.string.manage_vehicle)
            else -> null
        },
        button2Enabled = when (screen) {
            0 -> uiState.cars.isNotEmpty()
            else -> true
        },
        onButton2Clicked = {
            when (screen) {
                0 -> toolVisible = !toolVisible
            }
        }
    ) { mod ->
        when (screen) {
            -1 -> {
                MyDialogText(
                    text = stringResource(R.string.car_in_use_error, carNameToDelete),
                    modifier = mod
                )
            }

            0 -> {
                MyDialogSelectableList(
                    items = uiState.cars,
                    onItemSelected = { onItemSelected(it as Car) },
                    modifier = mod,
                    selectedItem = if (!uiState.isUpdatingCarOrMode)
                        uiState.carSelected
                    else
                        uiState.updatingCarOrModeSelected as Car
                ) {
                    AnimatedVisibility(
                        visible = toolVisible
                    ) {
                        Row {
                            IconButton(
                                onClick = {
                                    carIdToDelete = it.id
                                    carNameToDelete = it.getDisplayName()
                                    screen = 2
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = String.format(
                                        stringResource(R.string.delete_item),
                                        it.getDisplayName()
                                    ),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(
                                onClick = {
                                    carIdToEdit = it.id
                                    manufacturer = (it as Car).manufacturer
                                    model = it.model
                                    numberPlate = it.registrationNumber
                                    screen = 3
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = String.format(
                                        stringResource(R.string.edit_item),
                                        it.getDisplayName()
                                    )
                                )
                            }
                        }
                    }
                }
            }

            1, 3 -> {
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                val textFieldValue =
                    remember {
                        mutableStateOf(
                            TextFieldValue(
                                manufacturer,
                                TextRange(manufacturer.length)
                            )
                        )
                    }

                LaunchedEffect(Unit) {
                    if (screen == 1) {
                        focusRequester.requestFocus()
                    }
                }

                LaunchedEffect(toClose) {
                    if (toClose) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        toClose = false
                    }
                }

                LazyColumn(
                    modifier = mod
                ) {
                    item {
                        OutlinedTextField(
                            value = textFieldValue.value,
                            onValueChange = {
                                textFieldValue.value = it.copy(it.text.trimStart())
                                manufacturer = textFieldValue.value.text
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            label = {
                                Text(
                                    text = stringResource(R.string.manufacturer),
                                    fontFamily = NotoSans
                                )
                            },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                            singleLine = true,
                            modifier = Modifier
                                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                                .focusRequester(focusRequester)
                        )
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it.trimStart() },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            label = {
                                Text(
                                    text = stringResource(R.string.model),
                                    fontFamily = NotoSans
                                )
                            },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                            singleLine = true,
                            modifier = Modifier
                                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                        )
                        OutlinedTextField(
                            value = numberPlate,
                            onValueChange = { numberPlate = it.trimStart() },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            label = {
                                Text(
                                    text = stringResource(R.string.number_plate),
                                    fontFamily = NotoSans
                                )
                            },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_high_corner_radius)),
                            singleLine = true,
                            modifier = Modifier
                                .padding(vertical = dimensionResource(R.dimen.big_button_separation))
                        )
                    }
                }
            }

            2 -> {
                MyDialogText(
                    text = "Confirm delete $carNameToDelete?",
                    modifier = mod
                )
            }
        }
    }
}
