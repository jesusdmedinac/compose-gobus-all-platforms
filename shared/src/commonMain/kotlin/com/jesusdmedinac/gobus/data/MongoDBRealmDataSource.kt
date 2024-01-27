package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.local.model.Driver
import com.jesusdmedinac.gobus.data.local.model.Travel
import com.jesusdmedinac.gobus.data.local.model.Traveler
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class MongoDBRealmDataSource {
    private val config = RealmConfiguration.Builder(
        schema = setOf(
            Traveler::class,
            Driver::class,
            Travel::class,
        ),
    )
        .name("gobus-local.realm")
        .build()
    val realm: Realm = Realm.open(config)
}
