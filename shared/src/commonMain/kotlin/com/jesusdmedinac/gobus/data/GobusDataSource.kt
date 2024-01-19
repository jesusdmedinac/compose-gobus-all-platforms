package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.remote.model.Driver
import com.jesusdmedinac.gobus.data.remote.model.Travel
import com.jesusdmedinac.gobus.data.remote.model.Traveler
import com.jesusdmedinac.gobus.data.remote.model.UserLocation
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query

class GobusDataSource(
    private val realm: Realm,
) {
    fun getTravelers(): List<Traveler> = realm
        .query<Traveler>()
        .find()

    fun getTravelerBy(email: String) = realm
        .query<Traveler>("email == $0", email)
        .find()
        .first()

    suspend fun addTraveler(traveler: Traveler): Traveler = realm
        .write {
            copyToRealm(traveler)
        }

    suspend fun updateTravelerCurrentPosition(
        email: String,
        userLocation: UserLocation,
    ) =
        realm
            .write {
                findLatest(
                    query<Traveler>("email == $0", email)
                        .find()
                        .first(),
                )
                    ?.currentLocation = userLocation
            }

    suspend fun updateTravelerIsTraveling(email: String, isTraveling: Boolean) =
        realm
            .write {
                findLatest(
                    query<Traveler>("email == $0", email)
                        .find()
                        .first(),
                )
                    ?.traveling = isTraveling
            }

    fun getDriverBy(email: String) = realm
        .query<Driver>("email == $0", email)
        .find()
        .first()

    suspend fun updateDriverCurrentPosition(
        email: String,
        userLocation: UserLocation,
    ) =
        realm
            .write {
                findLatest(
                    query<Traveler>("email == $0", email)
                        .find()
                        .first(),
                )
                    ?.currentLocation = userLocation
            }

    suspend fun updateDriverIsTraveling(email: String, isTraveling: Boolean) =
        realm
            .write {
                findLatest(
                    query<Traveler>("email == $0", email)
                        .find()
                        .first(),
                )
                    ?.traveling = isTraveling
            }

    suspend fun addDriver(driver: Driver) = realm
        .write {
            copyToRealm(driver)
        }

    suspend fun addOrUpdateTravelBy(
        path: String,
        userLocation: UserLocation? = null,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ) = realm
        .write {
            query<Travel>("path == $0", path)
                .find()
                .firstOrNull()
                ?.apply {
                    try {
                        userLocation
                            ?.let { findLatest(it) }
                            ?.let { locationHistory.add(it) }
                        traveler
                            ?.let { findLatest(it) }
                            ?.takeIf { traveler -> !activeTravelers.any { it.email == traveler.email } }
                            ?.let { activeTravelers.add(it) }
                        driver
                            ?.let { findLatest(it) }
                            ?.takeIf { driver -> !activeDrivers.any { it.email == driver.email } }
                            ?.let { activeDrivers.add(it) }
                    } catch (t: Throwable) {
                        println(t.message.toString())
                    }
                }
                ?.let { copyToRealm(it, UpdatePolicy.ALL) }
                ?: run {
                    val travel = Travel()
                        .apply {
                            this.path = path
                            try {
                                userLocation
                                    ?.let { findLatest(it) }
                                    ?.let { locationHistory.add(it) }
                                traveler
                                    ?.let { findLatest(it) }
                                    ?.takeIf { traveler -> !activeTravelers.any { it.email == traveler.email } }
                                    ?.let { activeTravelers.add(it) }
                                driver
                                    ?.let { findLatest(it) }
                                    ?.takeIf { driver -> !activeDrivers.any { it.email == driver.email } }
                                    ?.let { activeDrivers.add(it) }
                            } catch (t: Throwable) {
                                println(t.message.toString())
                            }
                        }
                    copyToRealm(travel)
                }
        }

    suspend fun removeActiveTraveler(path: String, traveler: Traveler) = realm
        .write {
            query<Travel>("path == $0", path)
                .find()
                .first()
                .apply {
                    activeTravelers.removeAll { traveler.email == it.email }
                }
        }

    suspend fun removeActiveDriver(path: String, driver: Driver) = realm
        .write {
            query<Travel>("path == $0", path)
                .find()
                .first()
                .apply {
                    activeTravelers.removeAll { driver.email == it.email }
                }
        }

    fun getTravelThatContains(pathPrompt: String) =
        if (pathPrompt.isEmpty()) {
            realm
                .query<Travel>()
                .limit(10)
                .find()
                .toList()
        } else {
            realm
                .query<Travel>("path LIKE[c] \"*$pathPrompt*\"")
                .limit(10)
                .find()
                .toList()
        }

    fun getTravels() = realm
        .query<Travel>()
        .limit(10)
        .asFlow()
}
