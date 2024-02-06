package com.jesusdmedinac.gobus.data.mapper

import com.jesusdmedinac.gobus.domain.model.Driver
import com.jesusdmedinac.gobus.domain.model.Path
import com.jesusdmedinac.gobus.domain.model.Travel
import com.jesusdmedinac.gobus.domain.model.Traveler
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import com.jesusdmedinac.gobus.domain.model.UserLocation
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.map
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
fun UserCredentials.toDataUserCredentials(): DataUserCredentials = DataUserCredentials(
    email,
    password,
)

fun UserLocation.toDataUserLocation(): DataUserLocation = DataUserLocation(
    email,
    latitude,
    longitude,
    bearing,
    timestamp,
    pathName,
)

fun Instant.toRealmInstant(): RealmInstant = RealmInstant.from(
    epochSeconds,
    nanosecondAdjustment = nanosecondsOfSecond,
)
// endregion

// region remote to domain
fun DataUserLocation.toDomainUserLocation(): UserLocation = UserLocation(
    email,
    lat,
    long,
    bearing,
    timestamp,
    pathName,
)

suspend fun DataTraveler.toDomainTraveler(isTraveling: Boolean): Traveler = Traveler(
    userCredentials?.email ?: "",
    favoritePath
        ?.get()
        ?.data<DataPath>()
        ?.name
        ?: "",
    currentLocation?.toDomainUserLocation(),
    isTraveling = isTraveling,
)

suspend fun DataDriver.toDomainDriver(isTraveling: Boolean): Driver = Driver(
    userCredentials?.email ?: "",
    workingPath
        ?.get()
        ?.data<DataPath>()
        ?.name
        ?: "",
    currentLocation?.toDomainUserLocation(),
    isTraveling = isTraveling,
)

suspend fun DataPath.toDomainPath(): Path = Path(
    name,
    activeTravelers
        .map { documentReference ->
            documentReference
                .snapshots
                .map { documentSnapshot ->
                    documentSnapshot
                        .data<DataTraveler>()
                        .toDomainTraveler(isTraveling = true)
                }
        },
    activeDrivers
        .map { documentReference ->
            documentReference
                .snapshots
                .map { documentSnapshot ->
                    documentSnapshot
                        .data<DataDriver>()
                        .toDomainDriver(isTraveling = true)
                }
        },
)

suspend fun DataTravel.toDomainTravel(): Travel = Travel(
    startTime,
    endTime,
    path
        ?.get()
        ?.data<DataPath>()
        ?.toDomainPath(),
    traveler
        ?.get()
        ?.data<DataTraveler>()
        ?.toDomainTraveler(isTraveling = true),
    driver
        ?.get()
        ?.data<DataDriver>()
        ?.toDomainDriver(isTraveling = true),
)
// endregion

// region local to domain
fun LocalTraveler.toDomainTraveler(): Traveler = Traveler(
    email = email,
    isTraveling = isTraveling,
)

fun LocalDriver.toDomainDriver(): Driver = Driver(
    email = email,
    isTraveling = isTraveling,
)
// endregion
