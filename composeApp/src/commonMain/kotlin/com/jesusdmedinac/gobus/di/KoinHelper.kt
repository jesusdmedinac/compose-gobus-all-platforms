package com.jesusdmedinac.gobus.di

import com.jesusdmedinac.gobus.presentation.viewmodel.HomeScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.LoginScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.MainScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.MapViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.SignupScreenViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

object KoinHelper : KoinComponent {
    private val homeScreenViewModel: HomeScreenViewModel by inject()
    private val loginScreenViewModel: LoginScreenViewModel by inject()
    private val mainScreenViewModel: MainScreenViewModel by inject()
    private val mapViewModel: MapViewModel by inject()
    private val signupScreenViewModel: SignupScreenViewModel by inject()

    fun homeScreenViewModel() = homeScreenViewModel
    fun loginScreenViewModel() = loginScreenViewModel
    fun mainScreenViewModel() = mainScreenViewModel
    fun mapViewModel() = mapViewModel
    fun signupScreenViewModel() = signupScreenViewModel

    fun initKoin() {
        startKoin {
            modules(gobusKoinModules())
        }
    }
}
