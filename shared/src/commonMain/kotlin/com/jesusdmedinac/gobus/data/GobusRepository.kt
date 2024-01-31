package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.exception.StartStopTravelException
import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.data.mapper.toDataUserCredentials
import com.jesusdmedinac.gobus.data.mapper.toDataUserLocation
import com.jesusdmedinac.gobus.data.mapper.toDomainPath
import com.jesusdmedinac.gobus.data.remote.model.Driver
import com.jesusdmedinac.gobus.data.remote.model.Path
import com.jesusdmedinac.gobus.data.remote.model.Traveler
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import com.jesusdmedinac.gobus.domain.model.UserType
import io.realm.kotlin.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.jesusdmedinac.gobus.data.local.model.Driver as LocalDriver
import com.jesusdmedinac.gobus.data.local.model.Travel as LocalTravel
import com.jesusdmedinac.gobus.data.local.model.Traveler as LocalTraveler
import com.jesusdmedinac.gobus.domain.model.UserLocation as DomainUserLocation

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
    ) = withContext(Dispatchers.IO) {
        mongoDBAtlasDataSource
            .signup(userCredentials)
            .fold(
                onSuccess = {
                    when (userType) {
                        UserType.Unknown -> throw Throwable()
                        UserType.Traveler -> {
                            val traveler = Traveler().apply {
                                this.userCredentials = userCredentials.toDataUserCredentials()
                                favoritePath = path
                            }
                            addTravelToAtlasAndRealm(traveler)
                        }

                        UserType.Driver -> {
                            val driver = Driver().apply {
                                this.userCredentials = userCredentials.toDataUserCredentials()
                                workingPath = path
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
                email = traveler.userCredentials?.email ?: ""
            },
        )
    }

    private suspend fun addDriverToAtlasAndRealm(driver: Driver) {
        gobusRemoteDataSource
            .onSuccess { it.addDriver(driver) }
        gobusLocalDataSource.addDriver(
            LocalDriver().apply {
                email = driver.userCredentials?.email ?: ""
            },
        )
    }

    private val gobusRemoteDataSource: Result<GobusRemoteDataSource>
        get() = mongoDBAtlasDataSource
            .remoteGobusRemoteDataSource

    suspend fun login(userCredentials: UserCredentials): Result<Realm> =
        withContext(Dispatchers.IO) {
            mongoDBAtlasDataSource
                .login(userCredentials)
                .fold(
                    onSuccess = {
                        gobusLocalDataSource.addTraveler(
                            LocalTraveler().apply {
                                email = userCredentials.email
                            },
                        )
                        Result.success(it)
                    },
                    onFailure = { Result.failure(it) },
                )
        }

    suspend fun updateCurrentPosition(
        userLocation: DomainUserLocation,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            gobusLocalDataSource
                .getCurrentTraveler()
                .onSuccess { traveler ->
                    gobusRemoteDataSource
                        .onSuccess {
                            it.updateTravelerCurrentPosition(
                                email = traveler.email,
                                userLocation = userLocation.toDataUserLocation(),
                            )
                        }
                        .onFailure {
                            // it.printStackTrace()
                        }
                }
                .onFailure {
                    // it.printStackTrace()
                }
            gobusLocalDataSource
                .getCurrentDriver()
                .onSuccess { driver ->
                    gobusRemoteDataSource
                        .onSuccess {
                            it.updateDriverCurrentPosition(
                                email = driver.email,
                                userLocation = userLocation.toDataUserLocation(),
                            )
                        }
                        .onFailure {
                            // it.printStackTrace()
                        }
                }
                .onFailure {
                    // it.printStackTrace()
                }
        }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun startTravelOn(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        val addPathAndTravelForTravelerResult: Result<Unit> = gobusLocalDataSource
            .getCurrentTraveler()
            .fold(
                onSuccess = { localTraveler ->
                    gobusRemoteDataSource
                        .fold(
                            onSuccess = { gobusRemoteDataSource ->
                                getTravelerToAddItToPathAndTravel(
                                    gobusRemoteDataSource,
                                    localTraveler,
                                    path,
                                )
                            },
                            onFailure = { Result.failure(it) },
                        )
                },
                onFailure = { Result.failure(it) },
            )
        val updateTravelerIsTravelingResult: Result<LocalTraveler?> = gobusLocalDataSource
            .updateTravelerIsTraveling(isTraveling = true)
        val addPathAndTravelForDriverResult: Result<Unit> = gobusLocalDataSource
            .getCurrentDriver()
            .fold(
                onSuccess = { localDriver ->
                    gobusRemoteDataSource
                        .fold(
                            onSuccess = { gobusRemoteDataSource ->
                                getDriverToAddItToPathAndTravel(
                                    gobusRemoteDataSource,
                                    localDriver,
                                    path,
                                )
                            },
                            onFailure = { Result.failure(it) },
                        )
                },
                onFailure = { Result.failure(it) },
            )
        val updateDriverIsTravelingResult: Result<LocalDriver?> = gobusLocalDataSource
            .updateDriverIsTraveling(isTraveling = true)
        val results = listOf(
            addPathAndTravelForTravelerResult,
            updateTravelerIsTravelingResult,
            addPathAndTravelForDriverResult,
            updateDriverIsTravelingResult,
        )
        if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            Result.failure(
                StartStopTravelException(
                    results
                        .filter { it.isFailure }
                        .mapNotNull { it.exceptionOrNull() },
                ),
            )
        }
    }

    private suspend fun GobusRepository.getTravelerToAddItToPathAndTravel(
        gobusRemoteDataSource: GobusRemoteDataSource,
        localTraveler: LocalTraveler,
        path: String,
    ) = gobusRemoteDataSource
        .getTravelerBy(localTraveler.email)
        .fold(
            onSuccess = { traveler ->
                addPathAndTravelToRemoteDataSource(
                    gobusRemoteDataSource,
                    path,
                    traveler = traveler,
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun GobusRepository.getDriverToAddItToPathAndTravel(
        gobusRemoteDataSource: GobusRemoteDataSource,
        localDriver: LocalDriver,
        path: String,
    ) = gobusRemoteDataSource
        .getDriverBy(localDriver.email)
        .fold(
            onSuccess = { driver ->
                addPathAndTravelToRemoteDataSource(
                    gobusRemoteDataSource,
                    path,
                    driver = driver,
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun GobusRepository.addPathAndTravelToRemoteDataSource(
        gobusRemoteDataSource: GobusRemoteDataSource,
        path: String,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ) = gobusRemoteDataSource
        .addOrUpdatePath(
            path,
            traveler = traveler,
            driver = driver,
        )
        .fold(
            onSuccess = { path ->
                addTravelToRemoteAndLocalDataSource(
                    gobusRemoteDataSource,
                    path,
                    traveler = traveler,
                    driver = driver,
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun addTravelToRemoteAndLocalDataSource(
        gobusRemoteDataSource: GobusRemoteDataSource,
        path: Path,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ) = gobusRemoteDataSource
        .addTravel(
            path.name,
            traveler = traveler,
            driver = driver,
        )
        .fold(
            onSuccess = { remoteTravel ->
                gobusLocalDataSource
                    .addTravel(
                        remoteTravel._id,
                        path.name,
                    )
                    .fold(
                        onSuccess = {
                            Result.success(Unit)
                        },
                        onFailure = {
                            Result.failure(it)
                        },
                    )
            },
            onFailure = {
                Result.failure(it)
            },
        )

    suspend fun stopTravelOn(): Result<Unit> = withContext(Dispatchers.IO) {
        val updateTravelerIsTravelingResult = gobusLocalDataSource
            .updateTravelerIsTraveling(isTraveling = false)
        val updateDriverIsTravelingResult = gobusLocalDataSource
            .updateDriverIsTraveling(isTraveling = false)
        val getLastTravelByStartTimeResult = gobusLocalDataSource
            .getLastTravelByStartTime()
            .fold(
                onSuccess = { travel ->
                    val markRemoteTravelAsEndedResult = gobusRemoteDataSource
                        .fold(
                            onSuccess = { gobusRemoteDataSource ->
                                travel
                                    .remoteTravelId
                                    ?.let { gobusRemoteDataSource.markTravelAsEnded(it) }
                                    ?: Result.failure(Throwable("Remote travel is null on local travel"))
                            },
                            onFailure = { Result.failure(it) },
                        )
                    val markLocalTravelAsEndedResult = gobusLocalDataSource
                        .markTravelAsEnded()
                    val removeTravelerResult = gobusLocalDataSource
                        .getCurrentTraveler()
                        .fold(
                            onSuccess = { localTraveler ->
                                gobusRemoteDataSource
                                    .fold(
                                        onSuccess = { gobusRemoteDataSource ->
                                            getTravelerAndRemoveItFromTravel(
                                                gobusRemoteDataSource,
                                                localTraveler,
                                                travel,
                                            )
                                        },
                                        onFailure = { Result.failure(it) },
                                    )
                            },
                            onFailure = { Result.failure(it) },
                        )
                    val removeDriverResult = gobusLocalDataSource
                        .getCurrentDriver()
                        .fold(
                            onSuccess = { localDriver ->
                                gobusRemoteDataSource
                                    .fold(
                                        onSuccess = { gobusRemoteDataSource ->
                                            getDriverAndRemoveItFromTravel(
                                                gobusRemoteDataSource,
                                                localDriver,
                                                travel,
                                            )
                                        },
                                        onFailure = { Result.failure(it) },
                                    )
                            },
                            onFailure = { Result.failure(it) },
                        )
                    val results = listOf(
                        markRemoteTravelAsEndedResult,
                        markLocalTravelAsEndedResult,
                        removeTravelerResult,
                        removeDriverResult,
                    )
                    if (results.all { it.isSuccess }) {
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            StartStopTravelException(
                                results
                                    .filter { it.isFailure }
                                    .mapNotNull { it.exceptionOrNull() },
                            ),
                        )
                    }
                },
                onFailure = { Result.failure(it) },
            )

        val results = listOf(
            updateTravelerIsTravelingResult,
            updateDriverIsTravelingResult,
            getLastTravelByStartTimeResult,
        )
        if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            Result.failure(
                StartStopTravelException(
                    results
                        .filter { it.isFailure }
                        .mapNotNull { it.exceptionOrNull() },
                ),
            )
        }
    }

    private suspend fun getTravelerAndRemoveItFromTravel(
        gobusRemoteDataSource: GobusRemoteDataSource,
        localTraveler: LocalTraveler,
        travel: LocalTravel,
    ) = gobusRemoteDataSource
        .getTravelerBy(localTraveler.email)
        .fold(
            onSuccess = { traveler ->
                gobusRemoteDataSource.removeActiveUserBy(
                    travel.path,
                    traveler = traveler,
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun getDriverAndRemoveItFromTravel(
        gobusRemoteDataSource: GobusRemoteDataSource,
        localDriver: LocalDriver,
        travel: LocalTravel,
    ) = gobusRemoteDataSource
        .getDriverBy(localDriver.email)
        .fold(
            onSuccess = { driver ->
                gobusRemoteDataSource.removeActiveUserBy(
                    travel.path,
                    driver = driver,
                )
            },
            onFailure = { Result.failure(it) },
        )

    suspend fun getPathsThatContains(pathPrompt: String) = withContext(Dispatchers.IO) {
        gobusRemoteDataSource
            .fold(
                onSuccess = {
                    it
                        .getPathsThatContains(pathPrompt)
                        .fold(
                            onSuccess = { paths -> Result.success(paths.map { path -> path.toDomainPath() }) },
                            onFailure = { exception -> Result.failure(exception) },
                        )
                },
                onFailure = { exception -> Result.failure(exception) },
            )
    }

    suspend fun getPaths() = withContext(Dispatchers.IO) {
        gobusRemoteDataSource
            .fold(
                onSuccess = { gobusRemoteDataSource ->
                    gobusRemoteDataSource
                        .getPaths()
                        .fold(
                            onSuccess = { flowOfPaths ->
                                Result.success(
                                    flowOfPaths
                                        .map { resultsChangeOfPaths ->
                                            resultsChangeOfPaths
                                                .list
                                                .map { path -> path.toDomainPath() }
                                        },
                                )
                            },
                            onFailure = { exception -> Result.failure(exception) },
                        )
                },
                onFailure = { exception -> Result.failure(exception) },
            )
    }
}
