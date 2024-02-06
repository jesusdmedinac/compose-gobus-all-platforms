package com.jesusdmedinac.gobus.data.remote.model

import dev.gitlive.firebase.firestore.DocumentReference
import kotlinx.serialization.Serializable

@Serializable
data class Traveler(
    val reference: DocumentReference? = null,
    val userCredentials: UserCredentials? = null,
    val favoritePath: DocumentReference? = null,
    val currentLocation: UserLocation? = null,
)
