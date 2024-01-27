package com.jesusdmedinac.gobus.data.mapper

import com.jesusdmedinac.gobus.domain.model.Driver
import com.jesusdmedinac.gobus.domain.model.Path
import com.jesusdmedinac.gobus.domain.model.Travel
import com.jesusdmedinac.gobus.domain.model.Traveler
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import com.jesusdmedinac.gobus.domain.model.UserLocation
import io.realm.kotlin.types.RealmInstant
import kotlinx.datetime.Instant
import com.jesusdmedinac.gobus.data.local.model.Driver as LocalDriver
import com.jesusdmedinac.gobus.data.local.model.Traveler as LocalTraveler
import com.jesusdmedinac.gobus.data.remote.model.Driver as DataDriver
import com.jesusdmedinac.gobus.data.remote.model.Path as DataPath
import com.jesusdmedinac.gobus.data.remote.model.Travel as DataTravel
import com.jesusdmedinac.gobus.data.remote.model.Traveler as DataTraveler
import com.jesusdmedinac.gobus.data.remote.model.UserCredentials as DataUserCredentials
import com.jesusdmedinac.gobus.data.remote.model.UserLocation as DataUserLocation

// region domain to remote
fun UserCredentials.toDataUserCredentials(): DataUserCredentials = DataUserCredentials().also {
    it.email = email
    it.password = password
}

fun UserLocation.toDataUserLocation(): DataUserLocation = DataUserLocation().also {
    it.lat = lat
    it.long = long
    it.bearing = bearing
    it.timestamp = timestamp.toRealmInstant()
}

fun Instant.toRealmInstant(): RealmInstant = RealmInstant.from(
    epochSeconds,
    nanosecondAdjustment = nanosecondsOfSecond,
)
// endregion

// region remote to domain
fun RealmInstant.toInstant(): Instant = Instant.fromEpochSeconds(
    epochSeconds,
    nanosecondAdjustment = nanosecondsOfSecond,
)

fun DataUserLocation.toDomainUserLocation(): UserLocation = UserLocation(
    lat,
    long,
    bearing,
    timestamp.toInstant(),
)

fun DataTraveler.toDomainTraveler(traveler: LocalTraveler? = null): Traveler = Traveler(
    userCredentials?.email ?: "",
    favoritePath,
    isTraveling = traveler?.isTraveling ?: false,
    currentLocation?.toDomainUserLocation(),
)

fun DataDriver.toDomainDriver(driver: LocalDriver? = null): Driver = Driver(
    userCredentials?.email ?: "",
    workingPath,
    isTraveling = driver?.isTraveling ?: false,
    currentLocation?.toDomainUserLocation(),
)

fun DataPath.toDomainPath(): Path = Path(
    name,
    activeTravelers.map { it.toDomainTraveler() },
    activeDrivers.map { it.toDomainDriver() },
)

fun DataTravel.toDomainTravel(): Travel = Travel(
    startTime?.toInstant(),
    endTime?.toInstant(),
    path?.toDomainPath(),
    traveler?.toDomainTraveler(),
)
// endregion
