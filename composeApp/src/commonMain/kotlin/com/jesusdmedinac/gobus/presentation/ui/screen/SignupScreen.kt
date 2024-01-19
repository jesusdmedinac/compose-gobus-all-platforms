package com.jesusdmedinac.gobus.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.jesusdmedinac.gobus.domain.model.UserType
import com.jesusdmedinac.gobus.presentation.viewmodel.CreateUserStep
import com.jesusdmedinac.gobus.presentation.viewmodel.SignupScreenViewModel

@Composable
fun SignupScreen(
    signupScreenViewModel: SignupScreenViewModel,
    navigateToMain: () -> Unit = {},
    navigateToHome: () -> Unit = {},
) {
    val signupScreenState by signupScreenViewModel.state.collectAsState()
    LaunchedEffect(signupScreenState.createUserStep) {
        if (signupScreenState.createUserStep == CreateUserStep.Signup) {
            navigateToHome()
        } else if (signupScreenState.createUserStep == CreateUserStep.Main) {
            navigateToMain()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    when (signupScreenState.createUserStep) {
                        CreateUserStep.UserType -> {
                            IconButton(
                                onClick = {
                                    signupScreenViewModel.onBackClick()
                                },
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        }

                        CreateUserStep.Email -> {
                            IconButton(
                                onClick = {
                                    signupScreenViewModel.onBackClick()
                                },
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        }

                        CreateUserStep.Password -> {
                            IconButton(
                                onClick = {
                                    signupScreenViewModel.onBackClick()
                                },
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        }

                        CreateUserStep.Path -> {
                            IconButton(
                                onClick = {
                                    signupScreenViewModel.onBackClick()
                                },
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        }

                        CreateUserStep.Signup,
                        CreateUserStep.Main,
                        -> Unit
                    }
                },
                title = {},
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            var email by remember { mutableStateOf(TextFieldValue("")) }
            var password by remember { mutableStateOf(TextFieldValue("")) }
            var path by remember { mutableStateOf(TextFieldValue("")) }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
            ) {
                when (signupScreenState.createUserStep) {
                    CreateUserStep.Main,
                    CreateUserStep.Signup,
                    -> Unit

                    CreateUserStep.UserType -> {
                        Text("¿Viajas o manejas?", style = MaterialTheme.typography.h4)
                        Text(
                            "Viajas en autobus o eres un conductor de autobus",
                            style = MaterialTheme.typography.subtitle2,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = signupScreenState.userType == UserType.Traveler,
                                onClick = {
                                    signupScreenViewModel.onUserTypeChange(UserType.Traveler)
                                },
                            )
                            Text("Soy viajero")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = signupScreenState.userType == UserType.Driver,
                                onClick = {
                                    signupScreenViewModel.onUserTypeChange(UserType.Driver)
                                },
                            )
                            Text("Soy conductor")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                signupScreenViewModel.onNextClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Continuar")
                        }
                    }

                    CreateUserStep.Email -> {
                        Text("Compártenos tu email", style = MaterialTheme.typography.h4)
                        OutlinedTextField(
                            email,
                            onValueChange = {
                                email = it
                                signupScreenViewModel.onEmailChange(email.text)
                            },
                            placeholder = {
                                Text("Correo electrónico")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    signupScreenViewModel.onNextClick()
                                },
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                signupScreenViewModel.onNextClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = email.text.isNotEmpty(),
                        ) {
                            Text("Continuar")
                        }
                    }

                    CreateUserStep.Password -> {
                        Text("Elige una contraseña", style = MaterialTheme.typography.h4)
                        OutlinedTextField(
                            password,
                            onValueChange = {
                                password = it
                                signupScreenViewModel.onPasswordChange(password.text)
                            },
                            placeholder = {
                                Text("Contraseña")
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    signupScreenViewModel.onNextClick()
                                },
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                signupScreenViewModel.onNextClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = password.text.isNotEmpty(),
                        ) {
                            Text("Continuar")
                        }
                    }

                    CreateUserStep.Path -> {
                        Text("¿Y tu ruta favorita?", style = MaterialTheme.typography.h4)
                        OutlinedTextField(
                            path,
                            onValueChange = {
                                path = it
                                signupScreenViewModel.onPathChange(path.text)
                            },
                            placeholder = {
                                Text("Ruta de autobus")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    signupScreenViewModel.signup()
                                },
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                signupScreenViewModel.signup()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = path.text.isNotEmpty(),
                        ) {
                            Text("Crear cuenta")
                        }
                    }
                }
            }
        }
    }
}
