package com.jesusdmedinac.gobus.di

import com.jesusdmedinac.gobus.data.FirebaseAuthDataSource
import com.jesusdmedinac.gobus.data.GobusRemoteDataSource
import com.jesusdmedinac.gobus.data.GobusRemoteDataSourceImpl
import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.data.local.model.Driver
import com.jesusdmedinac.gobus.data.local.model.Travel
import com.jesusdmedinac.gobus.data.local.model.Traveler
import com.jesusdmedinac.gobus.presentation.viewmodel.HomeScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.LoginScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.MainScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.MapViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.SignupScreenViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.koin.core.module.Module
import org.koin.dsl.module

fun gobusKoinModules(): List<Module> = listOf(
    dataModule(),
    domainModule(),
    presentationModule(),
)

fun dataModule() = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    single { FirebaseAuthDataSource(get()) }
    single {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                Traveler::class,
                Driver::class,
                Travel::class,
            ),
        )
            .name("gobus-local.realm")
            .build()
        Realm.open(config)
    }
    single<GobusRemoteDataSource> { GobusRemoteDataSourceImpl(get()) }
    single { GobusLocalDataSource(get()) }
}

fun domainModule() = module {
    single { GobusRepository(get(), get(), get()) }
}

fun presentationModule() = module {
    single { HomeScreenViewModel(get()) }
    single { LoginScreenViewModel(get()) }
    single { MainScreenViewModel(get()) }
    single { MapViewModel(get()) }
    single { SignupScreenViewModel(get()) }
}
