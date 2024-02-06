package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusFirebaseBridge
import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.data.mapper.toDomainPath
import com.jesusdmedinac.gobus.domain.model.Path
import com.jesusdmedinac.gobus.domain.model.Traveler
import com.jesusdmedinac.gobus.domain.model.User
import com.jesusdmedinac.gobus.domain.model.UserLocation
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription

class MapViewModel(
    private val gobusRepository: GobusRepository,
    private val gobusFirebaseBridge: GobusFirebaseBridge,
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
                gobusFirebaseBridge
                    .getPaths()
                    .flatMapMerge { querySnapshot ->
                        querySnapshot
                            .documents
                            .map { documentSnapshot ->
                                documentSnapshot
                                    .data<com.jesusdmedinac.gobus.data.remote.model.Path>()
                                    .toDomainPath()
                            }
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
