package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val gobusRepository: GobusRepository,
) {
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    fun validateUserLoggedIn() {
        scope.launch {
            val isUserLoggedIn = gobusRepository.isUserLoggedIn()
            _state.update {
                it.copy(
                    isUserLoggedIn = isUserLoggedIn,
                )
            }
        }
    }
}

data class MainScreenState(
    val isUserLoggedIn: Boolean = false,
)
