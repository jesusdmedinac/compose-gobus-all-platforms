package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce

class MainScreenViewModel(
    private val gobusRepository: GobusRepository,
) : ViewModel(), ContainerHost<MainScreenState, MainScreenSideEffect> {
    override val container: Container<MainScreenState, MainScreenSideEffect> =
        viewModelScope.container(MainScreenState())

    fun validateUserLoggedIn() = intent {
        val isUserLoggedIn = gobusRepository.isUserLoggedIn()
        reduce {
            state.copy(
                isUserLoggedIn = isUserLoggedIn,
            )
        }
    }
}

data class MainScreenState(
    val isUserLoggedIn: Boolean = false,
)

sealed class MainScreenSideEffect
