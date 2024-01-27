package com.jesusdmedinac.gobus.domain.model

data class Path(
    val name: String = "",
    val activeTravelers: List<Traveler> = emptyList(),
    val activeDrivers: List<Driver> = emptyList(),
)
