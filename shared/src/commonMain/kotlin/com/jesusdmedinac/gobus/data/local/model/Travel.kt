package com.jesusdmedinac.gobus.data.local.model

import com.jesusdmedinac.gobus.data.mapper.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class Travel : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var remoteTravelId: ObjectId? = null
    var path: String = ""
    var startTime: RealmInstant = Clock.System.now().toRealmInstant()
    var endTime: RealmInstant? = null
}
