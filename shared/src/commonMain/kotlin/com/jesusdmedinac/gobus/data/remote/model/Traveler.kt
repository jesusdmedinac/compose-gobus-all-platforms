package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Traveler : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var userCredentials: UserCredentials? = null
    var favoritePath: String = ""
    var currentLocation: UserLocation? = UserLocation()
}
