package com.jesusdmedinac.gobus.domain.model

import kotlinx.coroutines.flow.Flow

data class Path(
    val name: String = "",
    val activeTravelers: List<Flow<Traveler>> = emptyList(),
    val activeDrivers: List<Flow<Driver>> = emptyList(),
) {
    val hasActiveTravelers: Boolean get() = activeTravelers.isNotEmpty()
    val hasActiveDrivers: Boolean get() = activeDrivers.isNotEmpty()
}
