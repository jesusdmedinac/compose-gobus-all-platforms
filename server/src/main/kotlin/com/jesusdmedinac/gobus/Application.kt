package com.jesusdmedinac.gobus

import SERVER_PORT
import com.jesusdmedinac.gobus.data.MongoDBAtlasDataSource
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import com.jesusdmedinac.gobus.data.remote.server.model.UserCredentials as DataUserCredentials

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    contentNegotiation()
    authentication()
    routing {
        post("/register") {
            /*val userCredential = call.receive<DataUserCredentials>()
            GobusRepository(
                MongoDBAtlasDataSource(),
                GobusLocalDataSource(MongoDBRealmDataSource().realm),
            )
                .signup(userCredential.toUserCredentials())
                .onSuccess {
                    call.respondText("Traveler registered")
                }
                .onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message.toString())
                }*/
        }
        authenticate("auth-basic") {
            get("/") {
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
            }
        }
    }
}

private fun DataUserCredentials.toUserCredentials() = UserCredentials(email, password)

fun Application.authentication() {
    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to the '/' path"
            validate { credentials ->
                val userCredentials = DataUserCredentials(credentials.name, credentials.password)
                MongoDBAtlasDataSource()
                    .login(userCredentials)
                UserIdPrincipal(userCredentials.email)
            }
        }
    }
}

fun Application.contentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}
