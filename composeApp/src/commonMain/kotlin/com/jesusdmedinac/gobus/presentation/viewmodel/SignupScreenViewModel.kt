package com.jesusdmedinac.gobus.presentation.viewmodel

import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.domain.model.UserCredentials
import com.jesusdmedinac.gobus.domain.model.UserType
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce

class SignupScreenViewModel(
    private val gobusRepository: GobusRepository,
) : ViewModel(), ContainerHost<SignupScreenState, SignupScreenSideEffect> {
    override val container: Container<SignupScreenState, SignupScreenSideEffect> =
        viewModelScope.container(SignupScreenState())

    fun signup() = intent {
        with(
            state,
        ) {
            gobusRepository.signup(
                userCredentials,
                userType,
                path,
            )
        }
            .onSuccess {
                reduce {
                    state.copy(
                        createUserStep = CreateUserStep.Signup,
                        throwable = null,
                    )
                }
            }
            .onFailure { throwable ->
                reduce {
                    state.copy(
                        throwable = throwable,
                    )
                }
            }
    }

    fun onUserTypeChange(userType: UserType) = intent {
        reduce {
            state.copy(
                userType = userType,
            )
        }
    }

    fun onEmailChange(email: String) = intent {
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$".toRegex())) {
            reduce {
                state.copy(
                    throwable = Throwable("Quizás tu correo está mal escrito 🤔"),
                )
            }
        } else {
            reduce {
                state.copy(
                    throwable = null,
                )
            }
        }
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

    fun onPathChange(path: String) = intent {
        reduce {
            state.copy(
                path = path,
            )
        }
    }

    fun onBackClick() = intent {
        when (state.createUserStep) {
            CreateUserStep.Main -> Unit
            CreateUserStep.UserType -> {
                reduce { state.copy(createUserStep = CreateUserStep.Main) }
            }

            CreateUserStep.Email -> {
                reduce { state.copy(createUserStep = CreateUserStep.UserType) }
            }

            CreateUserStep.Password -> {
                reduce { state.copy(createUserStep = CreateUserStep.Email) }
            }

            CreateUserStep.Path -> {
                reduce { state.copy(createUserStep = CreateUserStep.Password) }
            }

            CreateUserStep.Signup -> Unit
        }
    }

    fun onNextClick() = intent {
        when (state.createUserStep) {
            CreateUserStep.UserType -> {
                reduce { state.copy(createUserStep = CreateUserStep.Email) }
            }

            CreateUserStep.Email -> {
                reduce { state.copy(createUserStep = CreateUserStep.Password) }
            }

            CreateUserStep.Password -> {
                reduce { state.copy(createUserStep = CreateUserStep.Path) }
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
    val throwable: Throwable? = null,
)

sealed class SignupScreenSideEffect

enum class CreateUserStep {
    Main,
    UserType,
    Email,
    Password,
    Path,
    Signup,
}
