package com.jesusdmedinac.gobus.data.remote.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserLocation(
    val email: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val bearing: Double = 0.0,
    val timestamp: Instant = Clock.System.now(),
    val pathName: String = "",
)
