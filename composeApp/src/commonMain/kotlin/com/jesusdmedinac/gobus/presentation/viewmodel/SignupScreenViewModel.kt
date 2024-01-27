package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import com.jesusdmedinac.gobus.domain.model.UserType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupScreenViewModel(
    private val gobusRepository: GobusRepository,
) {
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(SignupScreenState())
    val state = _state

    fun signup() = scope.launch {
        with(
            state
                .value,
        ) {
            gobusRepository.signup(
                userCredentials,
                userType,
                path,
            )
        }
            .onSuccess {
                _state.update {
                    it.copy(createUserStep = CreateUserStep.Signup)
                }
            }
            .onFailure {
                it.printStackTrace()
            }
    }

    fun onUserTypeChange(userType: UserType) = scope.launch {
        state.update {
            it.copy(
                userType = userType,
            )
        }
    }

    fun onEmailChange(email: String) = scope.launch {
        state.update {
            it.copy(
                userCredentials = it.userCredentials.copy(email = email),
            )
        }
    }

    fun onPasswordChange(password: String) = scope.launch {
        state.update {
            it.copy(
                userCredentials = it.userCredentials.copy(password = password),
            )
        }
    }

    fun onPathChange(path: String) = scope.launch {
        state.update {
            it.copy(
                path = path,
            )
        }
    }

    fun onBackClick() = scope.launch {
        when (state.value.createUserStep) {
            CreateUserStep.Main -> Unit
            CreateUserStep.UserType -> {
                state.update { it.copy(createUserStep = CreateUserStep.Main) }
            }

            CreateUserStep.Email -> {
                state.update { it.copy(createUserStep = CreateUserStep.UserType) }
            }

            CreateUserStep.Password -> {
                state.update { it.copy(createUserStep = CreateUserStep.Email) }
            }

            CreateUserStep.Path -> {
                state.update { it.copy(createUserStep = CreateUserStep.Password) }
            }

            CreateUserStep.Signup -> Unit
        }
    }

    fun onNextClick() = scope.launch {
        when (state.value.createUserStep) {
            CreateUserStep.UserType -> {
                _state.update { it.copy(createUserStep = CreateUserStep.Email) }
            }

            CreateUserStep.Email -> {
                _state.update { it.copy(createUserStep = CreateUserStep.Password) }
            }

            CreateUserStep.Password -> {
                _state.update { it.copy(createUserStep = CreateUserStep.Path) }
            }

            CreateUserStep.Path,
            CreateUserStep.Main,
            CreateUserStep.Signup,
            -> Unit
        }
    }
}

data class SignupScreenState(
    val userCredentials: UserCredentials = UserCredentials(),
    val path: String = "",
    val createUserStep: CreateUserStep = CreateUserStep.UserType,
    val userType: UserType = UserType.Traveler,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
)

enum class CreateUserStep {
    Main,
    UserType,
    Email,
    Password,
    Path,
    Signup,
}
