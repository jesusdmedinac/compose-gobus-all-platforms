package com.jesusdmedinac.gobus.domain.model

data class Traveler(
    val email: String = "",
    val currentPath: String = "",
    val isTraveling: Boolean = false,
    val currentPosition: UserLocation? = UserLocation(),
)
