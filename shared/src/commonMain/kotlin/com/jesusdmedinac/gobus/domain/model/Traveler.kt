package com.jesusdmedinac.gobus.domain.model

data class Traveler(
    val email: String = "",
    val favoritePath: String = "",
    val currentLocation: UserLocation? = UserLocation(),
) {
    val isTraveling: Boolean get() = currentLocation != null
}
