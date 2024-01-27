package com.jesusdmedinac.gobus.domain.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import kotlinx.datetime.Instant

data class Travel(
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val path: Path? = null,
    val traveler: Traveler? = null,
    val driver: Driver? = null,
    val locationHistory: RealmList<UserLocation> = realmListOf(),
)
