package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.data.remote.model.Driver
import com.jesusdmedinac.gobus.data.remote.model.Travel
import com.jesusdmedinac.gobus.data.remote.model.Traveler
import com.jesusdmedinac.gobus.data.remote.model.UserLocation
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import com.jesusdmedinac.gobus.domain.model.UserType
import io.realm.kotlin.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import com.jesusdmedinac.gobus.data.local.model.Driver as LocalDriver
import com.jesusdmedinac.gobus.data.local.model.Traveler as LocalTraveler
import com.jesusdmedinac.gobus.data.remote.model.UserLocation as DataUserPosition
import com.jesusdmedinac.gobus.data.remote.server.model.UserCredentials as DataUserCredentials
import com.jesusdmedinac.gobus.domain.model.Driver as DomainDriver
import com.jesusdmedinac.gobus.domain.model.Travel as DomainTravel
import com.jesusdmedinac.gobus.domain.model.Traveler as DomainTraveler
import com.jesusdmedinac.gobus.domain.model.UserLocation as DomainUserPosition

class GobusRepository(
    private val mongoDBAtlasDataSource: MongoDBAtlasDataSource,
    private val gobusLocalDataSource: GobusLocalDataSource,
) {
    fun isUserLoggedIn(): Boolean = mongoDBAtlasDataSource
        .currentUser
        .isSuccess

    suspend fun signup(
        userCredentials: UserCredentials,
        userType: UserType,
        path: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val dataUserCredentials = userCredentials.toDataUserCredentials()
        mongoDBAtlasDataSource
            .signup(dataUserCredentials)
            .fold(
                onSuccess = {
                    when (userType) {
                        UserType.Unknown -> throw Throwable()
                        UserType.Traveler -> {
                            val traveler = Traveler().apply {
                                email = dataUserCredentials.email
                                password = dataUserCredentials.password
                                this.path = path
                            }
                            addTravelToAtlasAndRealm(traveler)
                        }

                        UserType.Driver -> {
                            val driver = Driver().apply {
                                email = dataUserCredentials.email
                                password = dataUserCredentials.password
                                this.path = path
                            }
                            addDriverToAtlasAndRealm(driver)
                        }
                    }
                    Result.success(Unit)
                },
                onFailure = {
                    Result.failure(it)
                },
            )
    }

    private suspend fun addTravelToAtlasAndRealm(traveler: Traveler) {
        gobusRemoteDataSource
            .onSuccess { it.addTraveler(traveler) }
        gobusLocalDataSource.addTraveler(
            LocalTraveler().apply {
                email = traveler.email
            },
        )
    }

    private suspend fun addDriverToAtlasAndRealm(driver: Driver) {
        gobusRemoteDataSource
            .onSuccess { it.addDriver(driver) }
        gobusLocalDataSource.addDriver(
            LocalDriver().apply {
                email = driver.email
            },
        )
    }

    private val gobusRemoteDataSource: Result<GobusDataSource>
        get() = mongoDBAtlasDataSource
            .remoteGobusDataSource

    suspend fun login(userCredentials: UserCredentials): Result<Realm> =
        withContext(Dispatchers.IO) {
            val dataUserCredentials = userCredentials.toDataUserCredentials()
            mongoDBAtlasDataSource
                .login(dataUserCredentials)
                .fold(
                    onSuccess = {
                        gobusLocalDataSource.addTraveler(
                            LocalTraveler().apply {
                                email = dataUserCredentials.email
                            },
                        )
                        Result.success(it)
                    },
                    onFailure = { Result.failure(it) },
                )
        }

    private fun UserCredentials.toDataUserCredentials(): DataUserCredentials = DataUserCredentials(
        email,
        password,
    )

    suspend fun updateCurrentPosition(
        userPosition: DomainUserPosition,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            gobusLocalDataSource
                .getCurrentTraveler()
                .onSuccess { traveler ->
                    gobusRemoteDataSource
                        .onSuccess {
                            it.updateTravelerCurrentPosition(
                                email = traveler.email,
                                userLocation = userPosition.toDataUserPosition(),
                            )
                        }
                }
            gobusLocalDataSource
                .getCurrentDriver()
                .onSuccess { driver ->
                    gobusRemoteDataSource
                        .onSuccess {
                            it.updateDriverCurrentPosition(
                                email = driver.email,
                                userLocation = userPosition.toDataUserPosition(),
                            )
                        }
                }
        }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun updateIsTraveling(
        isTraveling: Boolean,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            gobusLocalDataSource
                .getCurrentTraveler()
                .onSuccess { traveler ->
                    gobusRemoteDataSource
                        .onSuccess {
                            it.updateTravelerIsTraveling(
                                email = traveler.email,
                                isTraveling = isTraveling,
                            )
                        }
                }
            gobusLocalDataSource
                .getCurrentDriver()
                .onSuccess { driver ->
                    gobusRemoteDataSource
                        .onSuccess {
                            it.updateDriverIsTraveling(
                                email = driver.email,
                                isTraveling = isTraveling,
                            )
                        }
                }
        }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) },
            )
    }

    private fun DomainUserPosition.toDataUserPosition(): DataUserPosition =
        DataUserPosition().apply {
            lat = this@toDataUserPosition.lat
            long = this@toDataUserPosition.long
        }

    suspend fun startTravelOn(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            gobusLocalDataSource
                .getCurrentTraveler()
                .onSuccess { localTraveler ->
                    gobusRemoteDataSource.onSuccess {
                        val traveler = it.getTravelerBy(localTraveler.email)
                        it.addOrUpdateTravelBy(
                            path,
                            traveler = traveler,
                        )
                    }
                }
            gobusLocalDataSource
                .getCurrentDriver()
                .onSuccess { localDriver ->
                    gobusRemoteDataSource.onSuccess {
                        val driver = it.getDriverBy(localDriver.email)
                        it.addOrUpdateTravelBy(
                            path,
                            driver = driver,
                        )
                    }
                }
        }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun stopTravelOn(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            gobusLocalDataSource
                .getCurrentTraveler()
                .onSuccess { localTraveler ->
                    gobusRemoteDataSource.onSuccess {
                        val traveler = it.getTravelerBy(localTraveler.email)
                        it.removeActiveTraveler(path, traveler)
                    }
                }
            gobusLocalDataSource
                .getCurrentDriver()
                .onSuccess { localDriver ->
                    gobusRemoteDataSource.onSuccess {
                        val driver = it.getDriverBy(localDriver.email)
                        it.removeActiveDriver(path, driver)
                    }
                }
        }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun getTravelThatContains(pathPrompt: String) = withContext(Dispatchers.IO) {
        gobusRemoteDataSource
            .fold(
                onSuccess = {
                    Result.success(
                        it
                            .getTravelThatContains(pathPrompt)
                            .map { travel: Travel -> travel.toDomainTravel() },
                    )
                },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun getTravels() = withContext(Dispatchers.IO) {
        gobusRemoteDataSource
            .fold(
                onSuccess = { Result.success(it.getTravels()) },
                onFailure = { Result.failure(it) },
            )
    }
}

fun Travel.toDomainTravel(): DomainTravel = DomainTravel(
    path,
    locationHistory.map { it.toDomainUserPosition() },
    activeTravelers.map { it.toDomainTraveler() },
    activeDrivers.map { it.toDomainDriver() },
)

fun UserLocation.toDomainUserPosition(): DomainUserPosition = DomainUserPosition(
    lat,
    long,
)

fun Traveler.toDomainTraveler(): DomainTraveler = DomainTraveler(
    email,
    path,
    traveling,
    currentLocation?.toDomainUserPosition(),
)

fun Driver.toDomainDriver(): DomainDriver = DomainDriver(
    email,
    path,
    traveling,
    currentPosition?.toDomainUserPosition(),
)
