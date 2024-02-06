package com.jesusdmedinac.gobus.data.remote.model

import dev.gitlive.firebase.firestore.DocumentReference
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Travel(
    val reference: DocumentReference? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val path: DocumentReference? = null,
    val traveler: DocumentReference? = null,
    val driver: DocumentReference? = null,
    val locationHistory: List<UserLocation> = emptyList(),
)
