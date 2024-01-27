package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.data.MongoDBAtlasDataSource
import com.jesusdmedinac.gobus.data.MongoDBRealmDataSource
import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.domain.model.Driver
import com.jesusdmedinac.gobus.domain.model.Path
import com.jesusdmedinac.gobus.domain.model.Traveler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val gobusRepository: GobusRepository,
) {
    companion object {
        val INSTANCE = MapViewModel(
            GobusRepository(
                MongoDBAtlasDataSource(),
                GobusLocalDataSource(MongoDBRealmDataSource().realm),
            ),
        )
    }

    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(MapState())
    val state = _state

    fun fetchTravelingDriversAndTravelers() = scope.launch {
        gobusRepository
            .getPaths()
            .onSuccess { flowOfPaths ->
                flowOfPaths.collect { paths ->
                    _state.update {
                        it.copy(paths = paths)
                    }
                }
            }
    }
}

data class MapState(
    val paths: List<Path> = emptyList(),
) {
    fun getActiveTravelers(): List<Traveler> = paths
        .map { it.activeTravelers }
        .flatten()

    fun getActiveDrivers(): List<Driver> = paths
        .map { it.activeDrivers }
        .flatten()

    fun getTravelerBy(email: String): Traveler? = getActiveTravelers()
        .firstOrNull { it.email == email }

    fun getDriverBy(email: String): Driver? = getActiveDrivers()
        .firstOrNull { it.email == email }
}
