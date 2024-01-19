package com.jesusdmedinac.gobus.domain.model

data class UserLocation(
    var lat: Double = 0.0,
    var long: Double = 0.0,
    var bearing: Float = 0f,
)
