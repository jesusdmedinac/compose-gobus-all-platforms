package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce

class LoginScreenViewModel(
    private val gobusRepository: GobusRepository,
) : ViewModel(), ContainerHost<LoginScreenState, LoginScreenSideEffect> {

    override val container: Container<LoginScreenState, LoginScreenSideEffect> =
        viewModelScope.container(LoginScreenState())

    fun login() = intent {
        gobusRepository.login(state.userCredentials)
            .onSuccess {
                reduce {
                    state.copy(loginStep = LoginStep.Login)
                }
            }
            .onFailure {
                // it.printStackTrace()
            }
    }

    fun onEmailChange(email: String) = intent {
        reduce {
            state.copy(
                userCredentials = state.userCredentials.copy(email = email),
            )
        }
    }

    fun onPasswordChange(password: String) = intent {
        reduce {
            state.copy(
                userCredentials = state.userCredentials.copy(password = password),
            )
        }
    }

    fun onBackClick() = intent {
        when (state.loginStep) {
            LoginStep.Main,
            LoginStep.Login,
            -> Unit

            LoginStep.Email -> {
                reduce {
                    state.copy(loginStep = LoginStep.Main)
                }
            }

            LoginStep.Password -> {
                reduce {
                    state.copy(loginStep = LoginStep.Email)
                }
            }
        }
    }

    fun onNextClick() = intent {
        when (state.loginStep) {
            LoginStep.Main,
            LoginStep.Login,
            LoginStep.Password,
            -> Unit

            LoginStep.Email -> {
                reduce {
                    state.copy(loginStep = LoginStep.Password)
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

sealed class LoginScreenSideEffect

enum class LoginStep {
    Main,
    Email,
    Password,
    Login,
}
