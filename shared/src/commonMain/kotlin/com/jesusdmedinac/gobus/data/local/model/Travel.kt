package com.jesusdmedinac.gobus.data.local.model

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Travel : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var remoteTravelPath: String = ""
    var path: String = ""
    var startTime: RealmInstant = RealmInstant.now()
    var endTime: RealmInstant? = null
}
