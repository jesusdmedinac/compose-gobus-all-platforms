package com.jesusdmedinac.gobus.presentation.ui.screen.homescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jesusdmedinac.gobus.domain.model.Driver
import com.jesusdmedinac.gobus.domain.model.Traveler
import com.jesusdmedinac.gobus.presentation.viewmodel.MapState
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterialApi::class, ExperimentalResourceApi::class)
@Composable
fun DiscoverScreen(
    mapState: MapState,
) {
    val paths = mapState.paths
    val currentUser = mapState.currentUser
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            "Rutas en tu ciudad",
            style = MaterialTheme.typography.subtitle1,
        )
        Text(
            "Estas rutas han sido sugeridas en tu ciudad",
            style = MaterialTheme.typography.subtitle2,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(paths) { path ->
                ListItem(
                    icon = {
                        Icon(
                            painterResource("ic-path.xml"),
                            contentDescription = null,
                        )
                    },
                    trailing = {
                        when (currentUser) {
                            is Traveler -> {
                                val isTheFavoritePathOfTheCurrentTraveler =
                                    currentUser.favoritePathName == path.name
                                if (isTheFavoritePathOfTheCurrentTraveler) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                    )
                                }
                            }

                            is Driver -> {
                                val isThePathWhereTheDriverWorksAt =
                                    currentUser.workingPathName == path.name
                                if (isThePathWhereTheDriverWorksAt) {
                                    Icon(
                                        painterResource("ic-working-bus.xml"),
                                        contentDescription = null,
                                    )
                                }
                            }

                            else -> Unit
                        }
                    },
                    secondaryText = {
                        val hasActiveTravelers = path.hasActiveTravelers
                        val hasActiveDrivers = path.hasActiveDrivers
                        val hasActiveDriversAndTravelers = hasActiveTravelers && hasActiveDrivers
                        val hasActiveDriversOrTravelers = hasActiveTravelers || hasActiveDrivers
                        Text(
                            when {
                                hasActiveDriversAndTravelers -> "Hay conductores y viajeros activos"
                                hasActiveTravelers -> "Hay viajeros activos"
                                hasActiveDrivers -> "Hay conductores activos"
                                else -> ""
                            },
                            color = when {
                                hasActiveDriversOrTravelers -> MaterialTheme.colors.primary
                                else -> Color.Unspecified
                            },
                        )
                    },
                ) {
                    Text(path.name)
                }
            }
        }
    }
}
