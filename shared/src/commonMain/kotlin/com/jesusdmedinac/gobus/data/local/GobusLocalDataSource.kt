package com.jesusdmedinac.gobus.data.local

import com.jesusdmedinac.gobus.data.local.model.Driver
import com.jesusdmedinac.gobus.data.local.model.Traveler
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

class GobusLocalDataSource(
    private val realm: Realm,
) {
    fun getCurrentTraveler() = runCatching {
        realm
            .query<Traveler>()
            .find()
            .firstOrNull()
            ?: throw Throwable("Current traveler not available")
    }

    fun getCurrentDriver() = runCatching {
        realm
            .query<Driver>()
            .find()
            .firstOrNull()
            ?: throw Throwable("Current driver not available")
    }

    suspend fun addTraveler(traveler: Traveler): Traveler = realm.write {
        copyToRealm(traveler)
    }

    suspend fun addDriver(driver: Driver): Driver = realm.write {
        copyToRealm(driver)
    }
}
