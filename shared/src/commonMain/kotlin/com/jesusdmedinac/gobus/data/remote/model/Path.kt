package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Path : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = ""
    var activeTravelers: RealmList<Traveler> = realmListOf()
    var activeDrivers: RealmList<Driver> = realmListOf()
}
