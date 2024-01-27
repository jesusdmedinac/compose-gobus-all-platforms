package com.jesusdmedinac.gobus.data.remote.model

import io.realm.kotlin.types.EmbeddedRealmObject

class UserCredentials : EmbeddedRealmObject {
    var email: String = ""
    var password: String = ""
}
