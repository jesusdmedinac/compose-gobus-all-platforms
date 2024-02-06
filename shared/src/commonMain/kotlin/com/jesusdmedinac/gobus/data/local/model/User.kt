package com.jesusdmedinac.gobus.data.local.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

sealed interface User {
    var _id: ObjectId
    var email: String
    var isTraveling: Boolean
}

class Traveler : RealmObject, User {
    @PrimaryKey
    override var _id: ObjectId = ObjectId()
    override var email: String = ""
    override var isTraveling: Boolean = false
}

class Driver : RealmObject, User {
    @PrimaryKey
    override var _id: ObjectId = ObjectId()
    override var email: String = ""
    override var isTraveling: Boolean = false
}
