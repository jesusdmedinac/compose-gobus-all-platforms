package com.jesusdmedinac.gobus.domain.model

data class Driver(
    val email: String = "",
    val currentPath: String = "",
    val currentLocation: UserLocation? = null,
) {
    val isTraveling: Boolean get() = currentLocation != null
}
