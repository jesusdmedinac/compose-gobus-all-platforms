package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val gobusRepository: GobusRepository,
) {
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state

    fun login() {
        scope.launch {
            state
                .value
                .userCredentials
                .let { gobusRepository.login(it) }
                .onSuccess {
                    state.update {
                        it.copy(loginStep = LoginStep.Login)
                    }
                }
                .onFailure {
                    // it.printStackTrace()
                }
        }
    }

    fun onEmailChange(email: String) {
        scope.launch {
            state.update {
                it.copy(
                    userCredentials = it.userCredentials.copy(email = email),
                )
            }
        }
    }

    fun onPasswordChange(password: String) {
        scope.launch {
            state.update {
                it.copy(
                    userCredentials = it.userCredentials.copy(password = password),
                )
            }
        }
    }

    fun onBackClick() = scope.launch {
        when (state.value.loginStep) {
            LoginStep.Main,
            LoginStep.Login,
            -> Unit

            LoginStep.Email -> {
                _state.update {
                    it.copy(loginStep = LoginStep.Main)
                }
            }

            LoginStep.Password -> {
                _state.update {
                    it.copy(loginStep = LoginStep.Email)
                }
            }
        }
    }

    fun onNextClick() = scope.launch {
        when (state.value.loginStep) {
            LoginStep.Main,
            LoginStep.Login,
            LoginStep.Password,
            -> Unit

            LoginStep.Email -> {
                _state.update {
                    it.copy(loginStep = LoginStep.Password)
                }
            }
        }
    }
}

data class LoginScreenState(
    val userCredentials: UserCredentials = UserCredentials(),
    val loginStep: LoginStep = LoginStep.Email,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
)

enum class LoginStep {
    Main,
    Email,
    Password,
    Login,
}
