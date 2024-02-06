package com.jesusdmedinac.gobus.data

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import com.jesusdmedinac.gobus.domain.model.UserCredentials as DomainUserCredentials

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth,
) {
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun login(userCredentials: DomainUserCredentials): Result<FirebaseUser> = runCatching {
        with(userCredentials) {
            auth
                .signInWithEmailAndPassword(
                    email,
                    password,
                )
                .user
                ?: throw Throwable("Unable to login with user credentials: $userCredentials")
        }
    }

    suspend fun signup(userCredentials: DomainUserCredentials): Result<FirebaseUser> = runCatching {
        with(userCredentials) {
            auth
                .createUserWithEmailAndPassword(email, password)
                .user
                ?: throw Throwable("Unable to signup with user credentials: $userCredentials")
        }
    }

    fun test() {
    }
}
