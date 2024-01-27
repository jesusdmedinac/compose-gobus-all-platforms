package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.data.MongoDBAtlasDataSource
import com.jesusdmedinac.gobus.data.MongoDBRealmDataSource
import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.domain.model.UserLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val gobusRepository: GobusRepository,
) {
    companion object {
        val INSTANCE = HomeScreenViewModel(
            GobusRepository(
                MongoDBAtlasDataSource(),
                GobusLocalDataSource(MongoDBRealmDataSource().realm),
            ),
        )
    }

    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    fun onCleared() = scope.launch {
        gobusRepository
            .stopTravelOn()
    }

    fun onLocationChange(userLocation: UserLocation) = scope.launch {
        _state.update {
            it.copy(
                userLocation = userLocation,
            )
        }
        gobusRepository
            .updateCurrentPosition(userLocation = userLocation)
    }

    fun onStartTravelingClick() = scope.launch {
        _state.update { it.copy(isStartTravelingDialogShown = true) }
    }

    fun onStopTravelingClick() = scope.launch {
        _state.update { it.copy(isStopTravelingDialogShown = true) }
    }

    fun onDismissTravelingClick() = scope.launch {
        _state.update {
            it.copy(
                isStartTravelingDialogShown = false,
                isStopTravelingDialogShown = false,
            )
        }
    }

    fun startTraveling() = scope.launch {
        _state.update {
            it.copy(
                isTraveling = true,
                isStartTravelingDialogShown = false,
                isStopTravelingDialogShown = false,
            )
        }
        state
            .value
            .selectedPath
            .takeIf { it.isNotEmpty() }
            ?.let { selectedPath -> gobusRepository.startTravelOn(selectedPath) }
    }

    fun stopTraveling() = scope.launch {
        _state.update {
            it.copy(
                isTraveling = false,
                isStartTravelingDialogShown = false,
                isStopTravelingDialogShown = false,
            )
        }
        gobusRepository.stopTravelOn()
    }

    fun onPathChange(path: String) = scope.launch {
        _state.update {
            it.copy(
                selectedPath = path,
            )
        }
        gobusRepository
            .getPathsThatContains(path)
            .onSuccess { paths ->
                _state.update { state -> state.copy(paths = paths.map { it.name }) }
            }
            .onFailure {
                // it.printStackTrace()
            }
    }
}

data class HomeScreenState(
    val isTraveling: Boolean = false,
    val isStartTravelingDialogShown: Boolean = false,
    val isStopTravelingDialogShown: Boolean = false,
    val paths: List<String> = emptyList(),
    val selectedPath: String = "",
    val userLocation: UserLocation = UserLocation(),
)
