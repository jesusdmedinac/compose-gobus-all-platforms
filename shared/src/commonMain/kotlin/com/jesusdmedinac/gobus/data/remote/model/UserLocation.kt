package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant

class UserLocation : EmbeddedRealmObject {
    var lat: Double = 0.0
    var long: Double = 0.0
    var bearing: Float = 0f
    var timestamp: RealmInstant = RealmInstant.now()
}
