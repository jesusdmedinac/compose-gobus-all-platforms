package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.data.MongoDBAtlasDataSource
import com.jesusdmedinac.gobus.data.MongoDBRealmDataSource
import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.data.remote.model.Travel
import com.jesusdmedinac.gobus.data.toDomainTravel
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.jesusdmedinac.gobus.domain.model.Driver as DomainDriver
import com.jesusdmedinac.gobus.domain.model.Travel as DomainTravel
import com.jesusdmedinac.gobus.domain.model.Traveler as DomainTraveler

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
            .getTravels()
            .onSuccess { flowOfTravels ->
                flowOfTravels.collect { travels: ResultsChange<Travel> ->
                    _state.update { state ->
                        val domainTravels = travels.list.map { it.toDomainTravel() }
                        state.copy(travels = domainTravels)
                    }
                }
            }
    }
}

data class MapState(
    val drivers: List<DomainDriver> = emptyList(),
    val travelers: List<DomainTraveler> = emptyList(),
    val travels: List<DomainTravel> = emptyList(),
)
