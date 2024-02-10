package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.exception.StartStopTravelException
import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.data.mapper.toDataUserCredentials
import com.jesusdmedinac.gobus.data.mapper.toDomainDriver
import com.jesusdmedinac.gobus.data.mapper.toDomainPath
import com.jesusdmedinac.gobus.data.mapper.toDomainTraveler
import com.jesusdmedinac.gobus.data.remote.model.Path
import com.jesusdmedinac.gobus.domain.model.Driver
import com.jesusdmedinac.gobus.domain.model.Traveler
import com.jesusdmedinac.gobus.domain.model.User
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import com.jesusdmedinac.gobus.domain.model.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import com.jesusdmedinac.gobus.data.local.model.Driver as LocalDriver
import com.jesusdmedinac.gobus.data.local.model.Travel as LocalTravel
import com.jesusdmedinac.gobus.data.local.model.Traveler as LocalTraveler
import com.jesusdmedinac.gobus.data.remote.model.Driver as DataDriver
import com.jesusdmedinac.gobus.data.remote.model.Traveler as DataTraveler

class GobusRepository(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    val gobusLocalDataSource: GobusLocalDataSource,
    val gobusRemoteDataSource: GobusRemoteDataSource,
) {
    fun isUserLoggedIn(): Boolean = firebaseAuthDataSource
        .isUserLoggedIn

    suspend fun signup(
        userCredentials: UserCredentials,
        userType: UserType,
        path: String,
    ) = withContext(Dispatchers.IO) {
        firebaseAuthDataSource
            .signup(userCredentials)
            .fold(
                onSuccess = {
                    when (userType) {
                        UserType.Unknown -> throw Throwable()
                        UserType.Traveler -> {
                            gobusRemoteDataSource
                                .addOrUpdatePath(
                                    path,
                                )
                                .fold(
                                    onSuccess = { path ->
                                        val traveler = DataTraveler(
                                            userCredentials = userCredentials.toDataUserCredentials(),
                                            favoritePath = path.reference,
                                        )
                                        addTravelerToLocalAndRemote(traveler)
                                    },
                                    onFailure = { Result.failure(it) },
                                )
                        }

                        UserType.Driver -> {
                            gobusRemoteDataSource
                                .addOrUpdatePath(
                                    path,
                                )
                                .fold(
                                    onSuccess = { path ->
                                        val driver = DataDriver(
                                            userCredentials = userCredentials.toDataUserCredentials(),
                                            workingPath = path.reference,
                                        )
                                        addDriverToLocalAndRemote(driver)
                                    },
                                    onFailure = { Result.failure(it) },
                                )
                        }
                    }
                },
                onFailure = {
                    Result.failure(it)
                },
            )
    }

    private suspend fun addTravelerToLocalAndRemote(traveler: DataTraveler) =
        gobusRemoteDataSource
            .addTraveler(traveler)
            .onSuccess {
                gobusLocalDataSource.addUser(
                    LocalTraveler().apply {
                        email = traveler.userCredentials?.email ?: ""
                    },
                )
            }
            .onFailure {
                it.printStackTrace()
            }

    private suspend fun addDriverToLocalAndRemote(driver: DataDriver) =
        gobusRemoteDataSource
            .addDriver(driver)
            .onSuccess {
                gobusLocalDataSource.addUser(
                    LocalDriver().apply {
                        email = driver.userCredentials?.email ?: ""
                    },
                )
            }
            .onFailure {
                it.printStackTrace()
            }

    suspend fun login(userCredentials: UserCredentials): Result<Unit> =
        withContext(Dispatchers.IO) {
            firebaseAuthDataSource
                .login(userCredentials)
                .fold(
                    onSuccess = {
                        gobusLocalDataSource.addUser(
                            LocalTraveler().apply {
                                email = userCredentials.email
                            },
                        )
                        Result.success(Unit)
                    },
                    onFailure = { Result.failure(it) },
                )
        }

    suspend fun updateCurrentPosition(
        latitude: Double,
        longitude: Double,
        bearing: Double,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            gobusLocalDataSource
                .getCurrentUser<LocalTraveler>()
                .onSuccess { traveler ->
                    if (traveler.isTraveling) {
                        gobusLocalDataSource
                            .getLastTravelByStartTime()
                            .onSuccess { travel ->
                                gobusRemoteDataSource
                                    .updateTravelerCurrentPosition(
                                        email = traveler.email,
                                        latitude,
                                        longitude,
                                        bearing,
                                        path = travel.path,
                                    )
                            }
                            .onFailure { it.printStackTrace() }
                    } else {
                        gobusRemoteDataSource
                            .markTravelerLocationAsNull(traveler.email)
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
            gobusLocalDataSource
                .getCurrentUser<LocalDriver>()
                .onSuccess { driver ->
                    if (driver.isTraveling) {
                        gobusLocalDataSource
                            .getLastTravelByStartTime()
                            .onSuccess { travel ->
                                gobusRemoteDataSource
                                    .updateDriverCurrentPosition(
                                        email = driver.email,
                                        latitude,
                                        longitude,
                                        bearing,
                                        path = travel.path,
                                    )
                            }
                            .onFailure { it.printStackTrace() }
                    } else {
                        gobusRemoteDataSource
                            .markTravelerLocationAsNull(driver.email)
                    }
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun startTravelOn(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        val addPathAndTravelForTravelerResult: Result<Unit> = gobusLocalDataSource
            .getCurrentUser<LocalTraveler>()
            .fold(
                onSuccess = { localTraveler ->
                    getTravelerToAddItToPathAndTravel(
                        localTraveler,
                        path,
                    )
                },
                onFailure = { Result.failure(it) },
            )
        val updateTravelerIsTravelingResult: Result<LocalTraveler> = gobusLocalDataSource
            .updateIsTraveling(isTraveling = true)
        val addPathAndTravelForDriverResult: Result<Unit> = gobusLocalDataSource
            .getCurrentUser<LocalDriver>()
            .fold(
                onSuccess = { localDriver ->
                    getDriverToAddItToPathAndTravel(
                        localDriver,
                        path,
                    )
                },
                onFailure = { Result.failure(it) },
            )
        val updateDriverIsTravelingResult: Result<LocalDriver> = gobusLocalDataSource
            .updateIsTraveling(isTraveling = true)
        val results = listOf(
            addPathAndTravelForTravelerResult,
            updateTravelerIsTravelingResult,
            addPathAndTravelForDriverResult,
            updateDriverIsTravelingResult,
        )
        if (
            (
                addPathAndTravelForTravelerResult.isSuccess &&
                    updateTravelerIsTravelingResult.isSuccess
                ) ||
            (
                addPathAndTravelForDriverResult.isSuccess &&
                    updateDriverIsTravelingResult.isSuccess
                )
        ) {
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
        localTraveler: LocalTraveler,
        path: String,
    ) = gobusRemoteDataSource
        .getTravelerBy(localTraveler.email)
        .fold(
            onSuccess = { traveler ->
                addPathAndTravel(
                    path,
                    traveler = traveler,
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun GobusRepository.getDriverToAddItToPathAndTravel(
        localDriver: LocalDriver,
        path: String,
    ) = gobusRemoteDataSource
        .getDriverBy(localDriver.email)
        .fold(
            onSuccess = { driver ->
                addPathAndTravel(
                    path,
                    driver = driver,
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun GobusRepository.addPathAndTravel(
        path: String,
        traveler: DataTraveler? = null,
        driver: DataDriver? = null,
    ) = gobusRemoteDataSource
        .addOrUpdatePath(
            path,
            traveler = traveler,
            driver = driver,
        )
        .fold(
            onSuccess = { path ->
                addTravelToRemoteAndLocalDataSource(
                    path,
                    traveler = traveler,
                    driver = driver,
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun addTravelToRemoteAndLocalDataSource(
        path: Path,
        traveler: DataTraveler? = null,
        driver: DataDriver? = null,
    ) = gobusRemoteDataSource
        .addTravel(
            path.name,
            traveler = traveler,
            driver = driver,
        )
        .fold(
            onSuccess = { remoteTravel ->
                remoteTravel
                    .reference
                    ?.path
                    ?.let { remoteTravelId ->
                        gobusLocalDataSource
                            .addTravel(
                                LocalTravel().apply {
                                    remoteTravelPath = remoteTravelId
                                    this.path = path.name
                                },
                            )
                            .fold(
                                onSuccess = {
                                    Result.success(Unit)
                                },
                                onFailure = {
                                    Result.failure(it)
                                },
                            )
                    }
                    ?: Result.failure(Throwable("Remote travel reference must not be null"))
            },
            onFailure = {
                Result.failure(it)
            },
        )

    suspend fun stopTravelOn(): Result<Unit> = withContext(Dispatchers.IO) {
        val updateTravelerIsTravelingResult: Result<LocalTraveler> = gobusLocalDataSource
            .updateIsTraveling(isTraveling = false)
        val updateDriverIsTravelingResult: Result<LocalDriver> = gobusLocalDataSource
            .updateIsTraveling(isTraveling = false)
        val markTravelAsEndedAndRemoveUserFromPath = gobusLocalDataSource
            .getLastTravelByStartTime()
            .fold(
                onSuccess = { travel ->
                    val markRemoteTravelAsEndedResult = travel
                        .remoteTravelPath
                        .let { gobusRemoteDataSource.markTravelAsEnded(it) }
                    val markLocalTravelAsEndedResult = gobusLocalDataSource
                        .markTravelAsEnded()
                    val removeTravelerResult = gobusLocalDataSource
                        .getCurrentUser<LocalTraveler>()
                        .fold(
                            onSuccess = { localTraveler ->
                                getTravelerAndRemoveItFromTravel(
                                    localTraveler,
                                    travel,
                                )
                            },
                            onFailure = { Result.failure(it) },
                        )
                    val removeDriverResult = gobusLocalDataSource
                        .getCurrentUser<LocalDriver>()
                        .fold(
                            onSuccess = { localDriver ->
                                getDriverAndRemoveItFromTravel(
                                    localDriver,
                                    travel,
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
                    if (
                        markRemoteTravelAsEndedResult.isSuccess &&
                        markLocalTravelAsEndedResult.isSuccess && (
                            removeTravelerResult.isSuccess ||
                                removeDriverResult.isSuccess
                            )
                    ) {
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
            markTravelAsEndedAndRemoveUserFromPath,
        )
        if (
            (
                updateTravelerIsTravelingResult.isSuccess ||
                    updateDriverIsTravelingResult.isSuccess
                ) &&
            markTravelAsEndedAndRemoveUserFromPath.isSuccess
        ) {
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
            .getPathsThatContains(pathPrompt)
            .fold(
                onSuccess = { paths -> Result.success(paths.map { path -> path.toDomainPath() }) },
                onFailure = { exception -> Result.failure(exception) },
            )
    }

    suspend fun getPaths() = withContext(Dispatchers.IO) {
        gobusRemoteDataSource
            .getPaths()
            .fold(
                onSuccess = { flowOfListOfPaths ->
                    Result.success(
                        flowOfListOfPaths
                            .map { listOfPaths ->
                                listOfPaths
                                    .map { path ->
                                        path.toDomainPath()
                                    }
                            },
                    )
                },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun getCurrentTraveler() = withContext(Dispatchers.IO) {
        gobusLocalDataSource
            .getCurrentUser<LocalTraveler>()
            .fold(
                onSuccess = { currentLocalTraveler ->
                    val travelerEmail = currentLocalTraveler.email
                    gobusRemoteDataSource
                        .getTravelerBy(travelerEmail)
                        .fold(
                            onSuccess = { traveler ->
                                Result.success(traveler.toDomainTraveler(currentLocalTraveler.isTraveling))
                            },
                            onFailure = { Result.failure(it) },
                        )
                },
                onFailure = { Result.failure(it) },
            )
    }

    suspend inline fun <reified T : User> getCurrentUserAsFlow() = withContext(Dispatchers.IO) {
        gobusLocalDataSource
            .let {
                when (T::class) {
                    Traveler::class -> it.getCurrentUserAsFlow<LocalTraveler>()
                    Driver::class -> it.getCurrentUserAsFlow<LocalDriver>()
                    else -> Result.failure(Throwable("T reified class on getCurrentUserAsFlow must be a User: Traveler or Driver but was ${T::class.simpleName}"))
                }
            }
            .fold(
                onSuccess = { currentLocalTravelerFlow ->
                    runCatching {
                        currentLocalTravelerFlow
                            .mapNotNull { localUser ->
                                when (localUser) {
                                    is LocalTraveler -> localUser.toDomainTraveler()
                                    is LocalDriver -> localUser.toDomainDriver()
                                    else -> null
                                }
                            }
                    }
                },
                onFailure = { Result.failure(it) },
            )
    }

    suspend fun getCurrentDriver() = withContext(Dispatchers.IO) {
        gobusLocalDataSource
            .getCurrentUser<LocalDriver>()
            .fold(
                onSuccess = { currentLocalDriver ->
                    val driverEmail = currentLocalDriver.email
                    gobusRemoteDataSource
                        .getDriverBy(driverEmail)
                        .fold(
                            onSuccess = { driver ->
                                Result.success(driver.toDomainDriver(currentLocalDriver.isTraveling))
                            },
                            onFailure = { Result.failure(it) },
                        )
                },
                onFailure = { Result.failure(it) },
            )
    }
}
