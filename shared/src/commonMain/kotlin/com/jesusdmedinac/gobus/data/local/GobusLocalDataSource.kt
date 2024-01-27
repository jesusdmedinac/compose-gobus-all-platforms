package com.jesusdmedinac.gobus.data.local

import com.jesusdmedinac.gobus.data.local.model.Driver
import com.jesusdmedinac.gobus.data.local.model.Travel
import com.jesusdmedinac.gobus.data.local.model.Traveler
import com.jesusdmedinac.gobus.data.mapper.toRealmInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class GobusLocalDataSource(
    private val realm: Realm,
) {
    // region Traveler
    fun getCurrentTraveler() = runCatching {
        realm
            .query<Traveler>()
            .find()
            .firstOrNull()
            ?: throw Throwable("Current traveler not available")
    }

    suspend fun addTraveler(traveler: Traveler) = runCatching {
        realm.write {
            copyToRealm(traveler)
        }
    }

    suspend fun updateTravelerIsTraveling(
        isTraveling: Boolean,
    ) = runCatching {
        realm.write {
            query<Traveler>()
                .find()
                .firstOrNull()
                ?.let { traveler -> findLatest(traveler) }
                ?.also {
                    it.isTraveling = isTraveling
                }
        }
    }
    // endregion

    // region Driver
    fun getCurrentDriver() = runCatching {
        realm
            .query<Driver>()
            .find()
            .firstOrNull()
            ?: throw Throwable("Current driver not available")
    }

    suspend fun addDriver(driver: Driver) = runCatching {
        realm.write {
            copyToRealm(driver)
        }
    }

    suspend fun updateDriverIsTraveling(
        isTraveling: Boolean,
    ) = runCatching {
        realm.write {
            query<Driver>()
                .find()
                .firstOrNull()
                ?.let { traveler -> findLatest(traveler) }
                ?.also {
                    it.isTraveling = isTraveling
                }
        }
    }
    // endregion

    // region Travel
    fun getLastTravelByStartTime() = runCatching {
        realm.query<Travel>()
            .sort("startTime", Sort.DESCENDING)
            .limit(1)
            .find()
            .first()
    }

    suspend fun addTravel(remoteTravelId: ObjectId, pathName: String) = runCatching {
        realm.write {
            copyToRealm(
                Travel().also {
                    it.remoteTravelId = remoteTravelId
                    it.path = pathName
                },
            )
        }
    }

    suspend fun markTravelAsEnded() = runCatching {
        realm.write {
            query<Travel>()
                .sort("startTime", Sort.DESCENDING)
                .limit(1)
                .find()
                .firstOrNull()
                ?.let { traveler -> findLatest(traveler) }
                ?.also {
                    it.endTime = Clock.System.now().toRealmInstant()
                }
        }
    }
    // endregion
}
