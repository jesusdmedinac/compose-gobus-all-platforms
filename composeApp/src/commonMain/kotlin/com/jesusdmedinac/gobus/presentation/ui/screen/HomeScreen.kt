package com.jesusdmedinac.gobus.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.jesusdmedinac.gobus.presentation.ui.screen.homescreen.DiscoverScreen
import com.jesusdmedinac.gobus.presentation.ui.screen.homescreen.MyPathsScreen
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
        MapState,
    ) -> Unit = { modifier, mapState -> },
) {
    val homeScreenState by homeScreenViewModel.container.stateFlow.collectAsState()
    val mapState by mapViewModel.container.stateFlow.collectAsState()
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
                    val focusManager = LocalFocusManager.current
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
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    expanded = false
                                    focusManager.clearFocus()
                                    homeScreenViewModel.onPathChange(selectedPath.text)
                                },
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
                                    focusManager.clearFocus()
                                },
                            ) {
                                filteringOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded = false
                                            focusManager.clearFocus()
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
    Scaffold(
        bottomBar = {
            BottomAppBar {
                Spacer(
                    modifier = Modifier.width(16.dp),
                )
                Button(
                    onClick = {},
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp,
                    ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                        )
                        Text("Viajar")
                    }
                }

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                Button(
                    onClick = {},
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp,
                    ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                        )
                        Text("Descubrir")
                    }
                }

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                Button(
                    onClick = {},
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp,
                    ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                        )
                        Text("Mis rutas")
                    }
                }

                Spacer(
                    modifier = Modifier.width(16.dp),
                )
            }
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = homeScreenViewModel::onUserLocationClick,
                    backgroundColor = MaterialTheme.colors.secondary,
                    contentColor = MaterialTheme.colors.onSecondary,
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                    )
                }
                Spacer(
                    modifier = Modifier.height(16.dp),
                )
                FloatingActionButton(
                    onClick = {
                        if (mapState.isTraveling) {
                            homeScreenViewModel.onStopTravelingClick()
                        } else {
                            homeScreenViewModel.onStartTravelingClick()
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primarySurface,
                    contentColor = MaterialTheme.colors.onPrimary,
                ) {
                    if (mapState.isTraveling) {
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
        },
        floatingActionButtonPosition = FabPosition.End,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            var currentScreen by remember { mutableStateOf("viajar") }
            when (currentScreen) {
                "viajar" -> maps(
                    Modifier.fillMaxSize(),
                    mapState,
                )

                "descubrir" -> DiscoverScreen(mapState)
                "mis-rutas" -> MyPathsScreen()
            }
        }
    }
}
