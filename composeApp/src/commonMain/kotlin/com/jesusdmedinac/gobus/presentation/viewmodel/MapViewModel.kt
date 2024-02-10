package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.domain.model.Path
import com.jesusdmedinac.gobus.domain.model.Traveler
import com.jesusdmedinac.gobus.domain.model.User
import com.jesusdmedinac.gobus.domain.model.UserLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.mapNotNull
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val gobusRepository: GobusRepository,
) : ViewModel(), ContainerHost<MapState, MapSideEffect> {

    override val container: Container<MapState, MapSideEffect> =
        viewModelScope.container(MapState())

    fun fetchMapState() {
        intent {
            gobusRepository
                .getCurrentUserAsFlow<Traveler>()
                .onSuccess { currentTravelerFlow ->
                    repeatOnSubscription {
                        currentTravelerFlow
                            .collect { currentTraveler ->
                                reduce {
                                    state.copy(
                                        currentUser = currentTraveler,
                                    )
                                }
                            }
                    }
                }
                .onFailure { throwable ->
                    reduce {
                        state.copy(
                            currentUser = null,
                            throwable = throwable,
                        )
                    }
                }
        }
        intent {
            repeatOnSubscription {
                gobusRepository
                    .getPaths()
                    .onSuccess { flowOfListOfPath ->
                        flowOfListOfPath
                            .flatMapMerge { paths ->
                                reduce {
                                    state.copy(
                                        paths = paths,
                                        userLocations = emptyList(),
                                    )
                                }
                                paths
                                    .flatMap { path ->
                                        val travelersLocation = path
                                            .activeTravelers
                                            .map { travelerFlow ->
                                                travelerFlow
                                                    .mapNotNull { traveler -> traveler.currentLocation }
                                            }
                                        val driversLocation = path
                                            .activeDrivers
                                            .map { driverFlow ->
                                                driverFlow
                                                    .mapNotNull { driver -> driver.currentLocation }
                                            }
                                        travelersLocation + driversLocation
                                    }
                                    .asFlow()
                                    .flattenMerge()
                            }
                            .collect { userLocation ->
                                val indexOfFirst = state.userLocations
                                    .indexOfFirst { it.email == userLocation.email }
                                if (indexOfFirst == -1) {
                                    val userLocations =
                                        state.userLocations + userLocation
                                    reduce {
                                        state.copy(userLocations = userLocations)
                                    }
                                } else {
                                    val userLocations =
                                        state.userLocations.toMutableList().apply {
                                            set(indexOfFirst, userLocation)
                                        }
                                    reduce {
                                        state.copy(userLocations = userLocations)
                                    }
                                }
                            }
                    }
            }
        }
    }

    fun onLocationChange(
        latitude: Double,
        longitude: Double,
        bearing: Double,
    ) = intent {
        reduce {
            state.copy(
                latitude = latitude,
                longitude = longitude,
                bearing = bearing,
            )
        }
        if (state.currentUser?.isTraveling == true) {
            gobusRepository.updateCurrentPosition(
                latitude,
                longitude,
                bearing,
            )
        }
    }
}

data class MapState(
    val paths: List<Path> = emptyList(),
    val userLocations: List<UserLocation> = emptyList(),
    val currentUser: User? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bearing: Double = 0.0,
    val throwable: Throwable? = null,
) {
    val isTraveling: Boolean get() = currentUser?.isTraveling == true

    fun initialLocation(): UserLocation = UserLocation()
        .copy(
            latitude = latitude,
            longitude = longitude,
            bearing = bearing,
        )
}

sealed class MapSideEffect
