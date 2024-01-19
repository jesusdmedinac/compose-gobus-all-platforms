package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Driver : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var email: String = ""
    var password: String = ""
    var traveling: Boolean = false
    var currentPosition: UserLocation? = UserLocation()
    var path: String = ""
}
