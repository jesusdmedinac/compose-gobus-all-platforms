package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Traveler : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var email: String = ""
    var password: String = ""
    var traveling: Boolean = false
    var currentLocation: UserLocation? = UserLocation()
    var path: String = ""
}
