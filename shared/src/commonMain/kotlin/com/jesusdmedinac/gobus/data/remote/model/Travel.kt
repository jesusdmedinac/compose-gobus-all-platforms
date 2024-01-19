package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Travel : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var path: String = ""
    var locationHistory: RealmList<UserLocation> = realmListOf()
    var activeTravelers: RealmList<Traveler> = realmListOf()
    var activeDrivers: RealmList<Driver> = realmListOf()
}
