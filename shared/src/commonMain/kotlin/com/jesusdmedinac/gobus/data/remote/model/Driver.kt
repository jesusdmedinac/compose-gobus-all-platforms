package com.jesusdmedinac.gobus.data.remote.model

import dev.gitlive.firebase.firestore.DocumentReference
import kotlinx.serialization.Serializable

@Serializable
data class Driver(
    val reference: DocumentReference? = null,
    val userCredentials: UserCredentials? = null,
    val workingPath: DocumentReference? = null,
    val currentLocation: UserLocation? = null,
)
