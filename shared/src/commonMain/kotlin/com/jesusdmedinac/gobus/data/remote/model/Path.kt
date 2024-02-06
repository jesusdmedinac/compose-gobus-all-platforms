package com.jesusdmedinac.gobus.data.remote.model

import dev.gitlive.firebase.firestore.DocumentReference
import kotlinx.serialization.Serializable

@Serializable
data class Path(
    val reference: DocumentReference? = null,
    val name: String = "",
    val activeTravelers: List<DocumentReference> = emptyList(),
    val activeDrivers: List<DocumentReference> = emptyList(),
)
