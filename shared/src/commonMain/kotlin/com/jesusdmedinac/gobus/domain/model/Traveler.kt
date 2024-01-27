package com.jesusdmedinac.gobus.domain.model

data class Traveler(
    val email: String = "",
    val favoritePath: String = "",
    val isTraveling: Boolean = false,
    val currentLocation: UserLocation? = UserLocation(),
)
