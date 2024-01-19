package com.jesusdmedinac.gobus.domain.model

data class Driver(
    val email: String = "",
    val currentPath: String = "",
    val isTraveling: Boolean = false,
    val currentPosition: UserLocation? = null,
)
