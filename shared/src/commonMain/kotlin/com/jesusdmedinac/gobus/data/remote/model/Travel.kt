package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Travel : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var startTime: RealmInstant? = null
    var endTime: RealmInstant? = null
    var path: Path? = null
    var traveler: Traveler? = null
    var driver: Driver? = null
    var locationHistory: RealmList<UserLocation> = realmListOf()
}
