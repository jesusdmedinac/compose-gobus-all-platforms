package com.jesusdmedinac.gobus.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class UserLocation(
    var lat: Double = 0.0,
    var long: Double = 0.0,
    var bearing: Double = 0.0,
    var timestamp: Instant = Clock.System.now(),
)
