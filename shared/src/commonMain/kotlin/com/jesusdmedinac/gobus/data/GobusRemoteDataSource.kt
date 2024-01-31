package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.mapper.toRealmInstant
import com.jesusdmedinac.gobus.data.remote.model.Driver
import com.jesusdmedinac.gobus.data.remote.model.Path
import com.jesusdmedinac.gobus.data.remote.model.Travel
import com.jesusdmedinac.gobus.data.remote.model.Traveler
import com.jesusdmedinac.gobus.data.remote.model.UserLocation
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class GobusRemoteDataSource(
    private val realm: Realm,
) {
    // region Traveler
    fun getTravelers() = runCatching {
        realm
            .query<Traveler>()
            .find()
    }

    fun getTravelerBy(email: String) = runCatching {
        realm
            .query<Traveler>("userCredentials.email == $0", email)
            .find()
            .first()
    }

    suspend fun addTraveler(traveler: Traveler) = runCatching {
        realm
            .write {
                copyToRealm(traveler)
            }
    }

    suspend fun updateTravelerCurrentPosition(
        email: String,
        userLocation: UserLocation,
    ) =
        runCatching {
            realm
                .write {
                    query<Traveler>("userCredentials.email == $0", email)
                        .find()
                        .firstOrNull()
                        ?.let { traveler -> findLatest(traveler) }
                        ?.currentLocation = userLocation
                }
        }
    // endregion

    // region Driver
    fun getDrivers() = runCatching {
        realm
            .query<Driver>()
            .find()
    }

    fun getDriverBy(email: String) = runCatching {
        realm
            .query<Driver>("userCredentials.email == $0", email)
            .find()
            .first()
    }

    suspend fun addDriver(driver: Driver) = runCatching {
        realm
            .write {
                copyToRealm(driver)
            }
    }

    suspend fun updateDriverCurrentPosition(
        email: String,
        userLocation: UserLocation,
    ) =
        runCatching {
            realm
                .write {
                    query<Driver>("userCredentials.email == $0", email)
                        .find()
                        .firstOrNull()
                        ?.let { traveler -> findLatest(traveler) }
                        ?.currentLocation = userLocation
                }
        }
    // endregion

    // region Travel
    fun getPathsThatContains(pathPrompt: String) =
        runCatching {
            if (pathPrompt.isEmpty()) {
                realm
                    .query<Path>()
                    .limit(10)
                    .find()
                    .toList()
            } else {
                realm
                    .query<Path>("name LIKE[c] \"*$pathPrompt*\"")
                    .limit(10)
                    .find()
                    .toList()
            }
        }

    fun getPaths() = runCatching {
        realm
            .query<Path>()
            .limit(10)
            .asFlow()
    }

    suspend fun addTravel(
        pathName: String,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ) = runCatching {
        realm
            .write {
                val travel = Travel().apply {
                    query<Path>("name == $0", pathName)
                        .find()
                        .firstOrNull()
                        ?.let { findLatest(it) }
                        ?.let { path = it }
                    startTime = Clock.System.now().toRealmInstant()
                    traveler
                        ?.let { findLatest(it) }
                        ?.let { this.traveler = it }
                    driver
                        ?.let { findLatest(it) }
                        ?.let { this.driver = it }
                }
                copyToRealm(travel)
            }
    }
    // endregion

    // region path
    suspend fun addOrUpdatePath(
        path: String,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ) = runCatching {
        realm
            .write {
                query<Path>("name == $0", path)
                    .find()
                    .firstOrNull()
                    ?.let { path -> findLatest(path) }
                    ?.apply {
                        traveler
                            ?.let { findLatest(it) }
                            ?.takeIf { traveler -> !activeTravelers.any { it._id == traveler._id } }
                            ?.let { activeTravelers.add(it) }
                        driver
                            ?.let { findLatest(it) }
                            ?.takeIf { driver -> !activeDrivers.any { it._id == driver._id } }
                            ?.let { activeDrivers.add(it) }
                    }
                    ?: run {
                        copyToRealm(
                            Path().apply {
                                name = path
                                traveler
                                    ?.let { findLatest(it) }
                                    ?.let { activeTravelers = realmListOf(it) }
                                driver
                                    ?.let { findLatest(it) }
                                    ?.let { activeDrivers = realmListOf(it) }
                            },
                        )
                    }
            }
    }

    suspend fun removeActiveUserBy(
        path: String,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ) = runCatching {
        realm.write {
            query<Path>("name == $0", path)
                .find()
                .firstOrNull()
                ?.let { traveler -> findLatest(traveler) }
                ?.apply {
                    try {
                        traveler
                            ?.let { findLatest(it) }
                            ?.takeIf { traveler -> !activeTravelers.any { it._id == traveler._id } }
                            ?.let { activeTravelers.remove(it) }
                        driver
                            ?.let { findLatest(it) }
                            ?.takeIf { driver -> !activeDrivers.any { it._id == driver._id } }
                            ?.let { activeDrivers.remove(it) }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
        }
    }

    suspend fun markTravelAsEnded(travelId: ObjectId) = runCatching {
        realm.write {
            query<Travel>("_id == $0", travelId)
                .find()
                .firstOrNull()
                ?.let { findLatest(it) }
                ?.apply {
                    endTime = Clock.System.now().toRealmInstant()
                }
        }
    }

    // endregion
}
