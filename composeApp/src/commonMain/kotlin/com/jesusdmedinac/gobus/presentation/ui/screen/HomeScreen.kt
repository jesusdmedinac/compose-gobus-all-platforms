package com.jesusdmedinac.gobus.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.jesusdmedinac.gobus.domain.model.UserLocation
import com.jesusdmedinac.gobus.presentation.viewmodel.HomeScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.MapState
import com.jesusdmedinac.gobus.presentation.viewmodel.MapViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel,
    mapViewModel: MapViewModel,
    maps: @Composable (
        Modifier,
        UserLocation,
        MapState,
    ) -> Unit = { modifier, userPosition, mapState -> },
) {
    val homeScreenState by homeScreenViewModel.state.collectAsState()
    val mapState by mapViewModel.state.collectAsState()
    if (homeScreenState.isStartTravelingDialogShown) {
        AlertDialog(
            onDismissRequest = {
                homeScreenViewModel.onDismissTravelingClick()
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        homeScreenViewModel.onDismissTravelingClick()
                    },
                ) {
                    Text("No estoy listo")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        homeScreenViewModel.startTraveling()
                    },
                ) {
                    Text("Empezar a viajar", color = Color.Red)
                }
            },
            title = {
                Text("¿Listo para viajar?")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Al empezar a viajar compartirás tu ubicación con otros viajeros.")
                    Text("Por favor escribe el nombre de la ruta en la que empezarás a viajar")
                    val paths = homeScreenState.paths
                    var expanded by remember { mutableStateOf(false) }
                    var selectedPath by remember { mutableStateOf(TextFieldValue("")) }
                    LaunchedEffect(Unit) {
                        homeScreenViewModel.onPathChange(selectedPath.text)
                    }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedPath,
                            onValueChange = {
                                if (it.text != selectedPath.text) {
                                    expanded = true
                                }
                                selectedPath = it
                                homeScreenViewModel.onPathChange(it.text)
                            },
                            label = { Text("Ruta") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded,
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                            ),
                        )
                        val filteringOptions: List<String> =
                            paths.filter {
                                it.contains(
                                    selectedPath.text,
                                    ignoreCase = true,
                                )
                            }
                        if (filteringOptions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    expanded = false
                                },
                            ) {
                                filteringOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded = false
                                            selectedPath = TextFieldValue(selectionOption)
                                            homeScreenViewModel.onPathChange(selectionOption)
                                        },
                                    ) {
                                        Text(text = selectionOption)
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )
    }
    if (homeScreenState.isStopTravelingDialogShown) {
        AlertDialog(
            onDismissRequest = {
                homeScreenViewModel.onDismissTravelingClick()
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        homeScreenViewModel.onDismissTravelingClick()
                    },
                ) {
                    Text("No estoy listo")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        homeScreenViewModel.stopTraveling()
                    },
                ) {
                    Text("Dejar de viajar", color = Color.Red)
                }
            },
            title = {
                Text("Gracias por viajar con nosotros")
            },
            text = {
                Text("Espero hayas disfrutado tu viaje. Al terminar tu viaje tu ubicación dejará de ser visible para otros viajeros")
            },
        )
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        maps(
            Modifier.fillMaxSize(),
            homeScreenState.currentPosition,
            mapState,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
        ) {
            Button(
                onClick = {
                    if (homeScreenState.isTraveling) {
                        homeScreenViewModel.onStopTravelingClick()
                    } else {
                        homeScreenViewModel.onStartTravelingClick()
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
            ) {
                if (homeScreenState.isTraveling) {
                    Icon(
                        painterResource("ic-stop.xml"),
                        contentDescription = null,
                    )
                } else {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}
