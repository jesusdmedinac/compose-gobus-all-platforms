package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.remote.model.Driver
import com.jesusdmedinac.gobus.data.remote.model.Travel
import com.jesusdmedinac.gobus.data.remote.model.Traveler
import com.jesusdmedinac.gobus.data.remote.model.UserLocation
import com.jesusdmedinac.gobus.data.remote.server.model.UserCredentials
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.exceptions.UserAlreadyExistsException
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object RealmAtlas {
    var app = App.create("gobus-ydaca")
    var atlas: Realm? = null
}

private fun User.openRealm(): Result<Realm> = runCatching {
    if (RealmAtlas.atlas != null) return@runCatching RealmAtlas.atlas!!
    val realm = SyncConfiguration.Builder(
        user = this,
        schema = setOf(
            Traveler::class,
            Driver::class,
            UserLocation::class,
            Travel::class,
        ),
    )
        .initialSubscriptions(rerunOnOpen = true) { realm ->
            add(realm.query<Traveler>())
            add(realm.query<Driver>())
            add(realm.query<Travel>())
        }
        .errorHandler { session, error ->
            println("Error message: " + error.message.toString())
        }
        .build()
        .let { Realm.open(it) }
    RealmAtlas.atlas = realm
    realm
}

class MongoDBAtlasDataSource {

    suspend fun login(userCredentials: UserCredentials): Result<Realm> =
        RealmAtlas
            .app
            .login(Credentials.emailPassword(userCredentials.email, userCredentials.password))
            .openRealm()
            .onSuccess { realm ->
                realm.subscriptions.waitForSynchronization()
            }

    suspend fun signup(userCredentials: UserCredentials): Result<Realm> =
        runCatching {
            RealmAtlas
                .app
                .emailPasswordAuth
                .registerUser(userCredentials.email, userCredentials.password)
        }
            .fold(
                onFailure = {
                    when (it) {
                        is UserAlreadyExistsException -> login(userCredentials)
                        else -> Result.failure(it)
                    }
                },
                onSuccess = {
                    login(userCredentials)
                },
            )

    val currentUser: Result<User>
        get() = runCatching {
            RealmAtlas
                .app
                .currentUser
                ?: throw Throwable("User is not logged in")
        }

    val remoteGobusDataSource: Result<GobusDataSource>
        get() = currentUser
            .fold(
                onSuccess = { user ->
                    user
                        .openRealm()
                        .fold(
                            onSuccess = { Result.success(GobusDataSource(it)) },
                            onFailure = { Result.failure(it) },
                        )
                },
                onFailure = { Result.failure(it) },
            )
}
