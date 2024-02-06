package com.jesusdmedinac.gobus.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class UserLocation(
    var email: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var bearing: Double = 0.0,
    var timestamp: Instant = Clock.System.now(),
    var pathName: String = "",
)
