package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.remote.model.Driver
import com.jesusdmedinac.gobus.data.remote.model.Path
import com.jesusdmedinac.gobus.data.remote.model.Travel
import com.jesusdmedinac.gobus.data.remote.model.Traveler
import com.jesusdmedinac.gobus.data.remote.model.UserLocation
import dev.gitlive.firebase.firestore.ChangeType
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

interface GobusRemoteDataSource {
    suspend fun getTravelers(): Result<List<Traveler>>
    suspend fun getTravelerBy(email: String): Result<Traveler>
    suspend fun getTravelerFlowBy(email: String): Result<Flow<Traveler>>
    suspend fun addTraveler(traveler: Traveler): Result<Traveler>
    suspend fun updateTravelerCurrentPosition(
        email: String,
        latitude: Double,
        longitude: Double,
        bearing: Double,
        path: String,
    ): Result<Traveler>

    suspend fun markTravelerLocationAsNull(email: String): Result<Traveler>

    suspend fun getDrivers(): Result<List<Driver>>
    suspend fun getDriverBy(email: String): Result<Driver>
    suspend fun getDriverFlowBy(email: String): Result<Flow<Driver>>
    suspend fun addDriver(driver: Driver): Result<Driver>
    suspend fun updateDriverCurrentPosition(
        email: String,
        latitude: Double,
        longitude: Double,
        bearing: Double,
        path: String,
    ): Result<Driver>

    suspend fun markDriverLocationAsNull(email: String): Result<Traveler>

    suspend fun addTravel(
        pathName: String,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ): Result<Travel>

    suspend fun markTravelAsEnded(travelRemotePath: String): Result<Travel>

    suspend fun getPathsThatContains(pathPrompt: String): Result<List<Path>>
    fun getPaths(): Result<Flow<List<Path>>>
    suspend fun getPathBy(name: String): Result<Path>
    suspend fun addOrUpdatePath(
        pathName: String,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ): Result<Path>

    suspend fun removeActiveUserBy(
        pathName: String,
        traveler: Traveler? = null,
        driver: Driver? = null,
    ): Result<Path>
}

class GobusRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : GobusRemoteDataSource {
    // region Traveler
    override suspend fun getTravelers() = runCatching {
        firestore
            .collection("travelers")
            .get()
            .documents
            .map { it.data<Traveler>() }
    }

    override suspend fun getTravelerBy(email: String) = runCatching {
        firestore
            .collection("travelers")
            .document(email)
            .get()
            .data<Traveler>()
    }

    override suspend fun getTravelerFlowBy(email: String) = runCatching {
        firestore
            .collection("travelers")
            .document(email)
            .get()
            .reference
            .snapshots
            .map { it.data<Traveler>() }
    }

    override suspend fun addTraveler(traveler: Traveler) = runCatching {
        val email = traveler.userCredentials?.email
            ?: throw Throwable("Traveler $traveler must not have null email")
        firestore
            .collection("travelers")
            .document(email)
            .apply {
                set(traveler.copy(reference = this))
            }
            .get()
            .data<Traveler>()
    }

    override suspend fun updateTravelerCurrentPosition(
        email: String,
        latitude: Double,
        longitude: Double,
        bearing: Double,
        path: String,
    ) =
        runCatching {
            firestore
                .collection("travelers")
                .document(email)
                .get()
                .reference
                .takeIf {
                    it.get().data<Traveler>().currentLocation?.run {
                        lat != latitude ||
                            long != longitude ||
                            bearing != bearing
                    } ?: true
                }
                ?.apply {
                    update(
                        "currentLocation" to UserLocation(
                            email,
                            latitude,
                            longitude,
                            bearing,
                            Clock.System.now(),
                            path,
                        ),
                    )
                }
                ?.get()
                ?.data<Traveler>()
                ?: throw Throwable("Traveler was not updated")
        }

    override suspend fun markTravelerLocationAsNull(email: String) = runCatching {
        firestore
            .collection("travelers")
            .document(email)
            .get()
            .reference
            .apply {
                update(
                    "currentLocation" to null,
                )
            }
            .get()
            .data<Traveler>()
    }
    // endregion

    // region Driver
    override suspend fun getDrivers() = runCatching {
        firestore
            .collection("drivers")
            .get()
            .documents
            .map { it.data<Driver>() }
    }

    override suspend fun getDriverBy(email: String) = runCatching {
        firestore
            .collection("drivers")
            .document(email)
            .get()
            .data<Driver>()
    }

    override suspend fun getDriverFlowBy(email: String) = runCatching {
        firestore
            .collection("drivers")
            .document(email)
            .get()
            .reference
            .snapshots
            .map { it.data<Driver>() }
    }

    override suspend fun addDriver(driver: Driver) = runCatching {
        val email = driver.userCredentials?.email
            ?: throw Throwable("Driver $driver must not have null email")
        firestore
            .collection("drivers")
            .document(email)
            .apply {
                set(driver.copy(reference = this))
            }
            .get()
            .data<Driver>()
    }

    override suspend fun updateDriverCurrentPosition(
        email: String,
        latitude: Double,
        longitude: Double,
        bearing: Double,
        path: String,
    ) =
        runCatching {
            firestore
                .collection("drivers")
                .document(email)
                .get()
                .reference
                .takeIf {
                    it.get().data<Driver>().currentLocation?.run {
                        lat != latitude ||
                            long != longitude ||
                            bearing != bearing
                    } ?: true
                }
                ?.apply {
                    update(
                        "currentLocation" to UserLocation(
                            email,
                            latitude,
                            longitude,
                            bearing,
                            Clock.System.now(),
                            path,
                        ),
                    )
                }
                ?.get()
                ?.data<Driver>()
                ?: throw Throwable("Traveler was not updated")
        }

    override suspend fun markDriverLocationAsNull(email: String) = runCatching {
        firestore
            .collection("drivers")
            .document(email)
            .get()
            .reference
            .apply {
                update(
                    "currentLocation" to null,
                )
            }
            .get()
            .data<Traveler>()
    }
    // endregion

    // region Travel
    override suspend fun addTravel(
        pathName: String,
        traveler: Traveler?,
        driver: Driver?,
    ) = runCatching {
        firestore
            .collection("travels")
            .add(
                Travel(
                    startTime = Clock.System.now(),
                    endTime = null,
                    path = getPathBy(pathName)
                        .fold(
                            onSuccess = { it.reference },
                            onFailure = { null },
                        ),
                    traveler = traveler?.reference,
                    driver = driver?.reference,
                ),
            )
            .get()
            .reference
            .apply {
                update("reference" to this)
            }
            .get()
            .data<Travel>()
    }

    override suspend fun markTravelAsEnded(travelRemotePath: String) = runCatching {
        firestore
            .document(travelRemotePath)
            .apply {
                update("endTime" to Clock.System.now())
            }
            .get()
            .data<Travel>()
            .also {
                it.traveler?.update("currentLocation" to null)
                it.driver?.update("currentLocation" to null)
            }
    }
    // endregion

    // region path
    override suspend fun getPathsThatContains(pathPrompt: String) = runCatching {
        firestore
            .collection("paths")
            .let {
                if (pathPrompt.isNotEmpty()) {
                    it.where { "path" equalTo pathPrompt }
                } else {
                    it
                }
            }
            .get()
            .documents
            .map { it.data<Path>() }
    }

    override fun getPaths() = runCatching {
        firestore
            .collection("paths")
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documentChanges.mapNotNull { dc ->
                    when (dc.type) {
                        ChangeType.ADDED -> {
                            println("dct ADDED $dc")
                            dc.document.data<Path>()
                        }

                        ChangeType.MODIFIED -> {
                            println("dct MODIFIED $dc")
                            dc.document.data<Path>()
                        }

                        ChangeType.REMOVED -> {
                            println("dct REMOVED $dc")
                            null
                        }

                        else -> {
                            println("dct else $dc")
                            null
                        }
                    }
                }
            }
    }

    override suspend fun getPathBy(name: String) = runCatching {
        firestore
            .collection("paths")
            .where { "name" equalTo name }
            .get()
            .documents
            .first()
            .data<Path>()
    }

    override suspend fun addOrUpdatePath(
        pathName: String,
        traveler: Traveler?,
        driver: Driver?,
    ) =
        getPathBy(pathName)
            .fold(
                onSuccess = { path ->
                    runCatching {
                        val activeTravelers = path
                            .activeTravelers
                            .toMutableList()
                        traveler
                            ?.takeIf { traveler ->
                                !activeTravelers
                                    .any {
                                        it
                                            .get()
                                            .data<Traveler>()
                                            .userCredentials
                                            ?.email == traveler.userCredentials?.email
                                    }
                            }
                            ?.reference
                            ?.let { activeTravelers.add(it) }
                        val activeDrivers = path
                            .activeDrivers
                            .toMutableList()
                        driver
                            ?.takeIf { driver ->
                                !activeDrivers
                                    .any {
                                        it
                                            .get()
                                            .data<Driver>()
                                            .userCredentials
                                            ?.email == driver.userCredentials?.email
                                    }
                            }
                            ?.reference
                            ?.let { activeDrivers.add(it) }
                        val updatedPath = path.copy(
                            activeTravelers = activeTravelers,
                            activeDrivers = activeDrivers,
                        )
                        firestore
                            .collection("paths")
                            .where { "name" equalTo pathName }
                            .get()
                            .documents
                            .first()
                            .let {
                                it.reference.set(updatedPath)
                                it.data<Path>()
                            }
                    }
                },
                onFailure = {
                    runCatching {
                        val path = Path(
                            name = pathName,
                            activeTravelers = traveler?.reference?.let { listOf(it) }
                                ?: emptyList(),
                            activeDrivers = driver?.reference?.let { listOf(it) } ?: emptyList(),
                        )
                        firestore
                            .collection("paths")
                            .add(path)
                            .get()
                            .reference
                            .apply {
                                update("reference" to this)
                            }
                            .get()
                            .data<Path>()
                    }
                },
            )

    override suspend fun removeActiveUserBy(
        pathName: String,
        traveler: Traveler?,
        driver: Driver?,
    ) =
        getPathBy(pathName)
            .fold(
                onSuccess = { path ->
                    runCatching {
                        val activeTravelers = path
                            .activeTravelers
                            .toMutableList()
                        traveler
                            ?.reference
                            ?.let { activeTravelers.remove(it) }
                        val activeDrivers = path
                            .activeDrivers
                            .toMutableList()
                        driver
                            ?.reference
                            ?.let { activeDrivers.remove(it) }
                        val updatedPath = path.copy(
                            activeTravelers = activeTravelers,
                            activeDrivers = activeDrivers,
                        )
                        firestore
                            .collection("paths")
                            .where { "name" equalTo pathName }
                            .get()
                            .documents
                            .first()
                            .let {
                                it.reference.set(updatedPath)
                                it.data<Path>()
                            }
                    }
                },
                onFailure = { Result.failure(it) },
            )
    // endregion
}
