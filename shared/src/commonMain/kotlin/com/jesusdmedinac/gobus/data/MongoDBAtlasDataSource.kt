package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.remote.model.Driver
import com.jesusdmedinac.gobus.data.remote.model.Path
import com.jesusdmedinac.gobus.data.remote.model.Travel
import com.jesusdmedinac.gobus.data.remote.model.Traveler
import com.jesusdmedinac.gobus.data.remote.model.UserCredentials
import com.jesusdmedinac.gobus.data.remote.model.UserLocation
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.exceptions.UserAlreadyExistsException
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlin.native.concurrent.ThreadLocal
import com.jesusdmedinac.gobus.domain.model.UserCredentials as DomainUserCredentials

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
            Driver::class,
            Path::class,
            Travel::class,
            Traveler::class,
            UserCredentials::class,
            UserLocation::class,
        ),
    )
        .initialSubscriptions(rerunOnOpen = true) { realm ->
            add(realm.query<Driver>())
            add(realm.query<Path>())
            add(realm.query<Travel>())
            add(realm.query<Traveler>())
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

    suspend fun login(userCredentials: DomainUserCredentials): Result<Realm> =
        RealmAtlas
            .app
            .login(Credentials.emailPassword(userCredentials.email, userCredentials.password))
            .openRealm()
            .onSuccess { realm ->
                realm.subscriptions.waitForSynchronization()
            }
            .onFailure {
                it.printStackTrace()
            }

    suspend fun signup(userCredentials: DomainUserCredentials): Result<Realm> =
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

    val remoteGobusRemoteDataSource: Result<GobusRemoteDataSource>
        get() = currentUser
            .fold(
                onSuccess = { user ->
                    user
                        .openRealm()
                        .fold(
                            onSuccess = { Result.success(GobusRemoteDataSource(it)) },
                            onFailure = { Result.failure(it) },
                        )
                },
                onFailure = { Result.failure(it) },
            )
}
