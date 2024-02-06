package com.jesusdmedinac.gobus.domain.model

sealed interface User {
    val email: String
    val currentLocation: UserLocation?
    val isTraveling: Boolean
}

data class Traveler(
    override val email: String = "",
    val favoritePathName: String = "",
    override val currentLocation: UserLocation? = UserLocation(),
    override val isTraveling: Boolean,
) : User

data class Driver(
    override val email: String = "",
    val workingPathName: String = "",
    override val currentLocation: UserLocation? = null,
    override val isTraveling: Boolean,
) : User
