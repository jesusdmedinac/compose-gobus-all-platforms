package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import kotlinx.coroutines.delay
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce

class HomeScreenViewModel(
    private val gobusRepository: GobusRepository,
) : ViewModel(), ContainerHost<HomeScreenState, HomeScreenSideEffect> {

    override val container: Container<HomeScreenState, HomeScreenSideEffect> =
        viewModelScope.container(HomeScreenState())

    init {
        intent {
            gobusRepository.stopTravelOn()
        }
    }

    fun onStartTravelingClick() = intent {
        reduce { state.copy(isStartTravelingDialogShown = true) }
    }

    fun onStopTravelingClick() = intent {
        reduce { state.copy(isStopTravelingDialogShown = true) }
    }

    fun onDismissTravelingClick() = intent {
        reduce {
            state.copy(
                isStartTravelingDialogShown = false,
                isStopTravelingDialogShown = false,
            )
        }
    }

    fun startTraveling() = intent {
        reduce {
            state.copy(
                isStartTravelingDialogShown = false,
                isStopTravelingDialogShown = false,
            )
        }
        gobusRepository.startTravelOn(state.selectedPath)
    }

    fun stopTraveling() = intent {
        reduce {
            state.copy(
                isStartTravelingDialogShown = false,
                isStopTravelingDialogShown = false,
            )
        }
        gobusRepository.stopTravelOn()
    }

    fun onPathChange(path: String) = intent {
        reduce {
            state.copy(
                selectedPath = path,
            )
        }
        gobusRepository
            .getPathsThatContains(path)
            .onSuccess { paths ->
                reduce { state.copy(paths = paths.map { it.name }) }
            }
            .onFailure {
                it.printStackTrace()
            }
    }

    fun onUserLocationClick() = intent {
        postSideEffect(HomeScreenSideEffect.MoveToUserLocation)
        delay(500)
        postSideEffect(HomeScreenSideEffect.Idle)
    }
}

data class HomeScreenState(
    val isStartTravelingDialogShown: Boolean = false,
    val isStopTravelingDialogShown: Boolean = false,
    val paths: List<String> = emptyList(),
    val selectedPath: String = "",
)

sealed class HomeScreenSideEffect {
    data object Idle : HomeScreenSideEffect()
    data object MoveToUserLocation : HomeScreenSideEffect()
}
