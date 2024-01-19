package com.jesusdmedinac.gobus.domain.model

data class Travel(
    val path: String = "",
    val locationHistory: List<UserLocation> = emptyList(),
    val activeTravelers: List<Traveler> = emptyList(),
    val activeDrivers: List<Driver> = emptyList(),
)
