package com.jesusdmedinac.gobus.data.remote.server.model

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentials(
    val email: String,
    val password: String,
)
