package com.jesusdmedinac.gobus.data.local

import com.jesusdmedinac.gobus.data.local.model.Driver
import com.jesusdmedinac.gobus.data.local.model.Travel
import com.jesusdmedinac.gobus.data.local.model.Traveler
import com.jesusdmedinac.gobus.data.mapper.toRealmInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class GobusLocalDataSource(
    val realm: Realm,
) {
    // region User
    inline fun <reified T : RealmObject> getCurrentUser() = runCatching {
        realm
            .query<T>()
            .find()
            .firstOrNull()
            ?: throw Throwable("Current user not available")
    }

    inline fun <reified T : RealmObject> getCurrentUserAsFlow() = runCatching {
        realm
            .query<T>()
            .find()
            .asFlow()
            .map { resultsChange ->
                when (resultsChange) {
                    is InitialResults -> resultsChange.list.last()
                    is UpdatedResults -> resultsChange.list.last()
                }
            }
    }

    suspend inline fun <reified T : RealmObject> updateIsTraveling(
        isTraveling: Boolean,
    ) = runCatching {
        realm
            .write {
                query<T>()
                    .find()
                    .firstOrNull()
                    ?.apply {
                        when (this) {
                            is Traveler -> findLatest(this)?.isTraveling = isTraveling
                            is Driver -> findLatest(this)?.isTraveling = isTraveling
                        }
                    }
                    ?: throw Throwable("No active user to update isTraveling as $isTraveling")
            }
    }

    suspend inline fun <reified T : RealmObject> addUser(user: T) = runCatching {
        realm
            .write {
                copyToRealm(user)
            }
    }
    // endregion

    // region Travel
    fun getLastTravelByStartTime() = runCatching {
        realm
            .query<Travel>()
            .sort("startTime", Sort.DESCENDING)
            .find()
            .first()
    }

    suspend fun addTravel(travel: Travel) = runCatching {
        realm
            .write {
                copyToRealm(travel)
            }
    }

    suspend fun markTravelAsEnded() = runCatching {
        realm
            .write {
                query<Travel>()
                    .sort("startTime", Sort.DESCENDING)
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
