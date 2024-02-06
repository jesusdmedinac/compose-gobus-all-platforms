package com.jesusdmedinac.gobus.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.jesusdmedinac.gobus.presentation.viewmodel.LoginScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.LoginStep

@Composable
fun LoginScreen(
    loginScreenViewModel: LoginScreenViewModel,
    navigateToMain: () -> Unit = {},
    navigateToHome: () -> Unit = {},
) {
    val loginScreenState by loginScreenViewModel.container.stateFlow.collectAsState()
    LaunchedEffect(loginScreenState.loginStep) {
        if (loginScreenState.loginStep == LoginStep.Login) {
            navigateToHome()
        } else if (loginScreenState.loginStep == LoginStep.Main) {
            navigateToMain()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    when (loginScreenState.loginStep) {
                        LoginStep.Email,
                        LoginStep.Password,
                        -> IconButton(
                            onClick = {
                                loginScreenViewModel.onBackClick()
                            },
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }

                        LoginStep.Login,
                        LoginStep.Main,
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
            ) {
                var email by remember { mutableStateOf(TextFieldValue("")) }
                var password by remember { mutableStateOf(TextFieldValue("")) }
                when (loginScreenState.loginStep) {
                    LoginStep.Email -> {
                        Text("Correo electrónico", style = MaterialTheme.typography.h4)
                        OutlinedTextField(
                            email,
                            onValueChange = {
                                email = it
                                loginScreenViewModel.onEmailChange(email.text)
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
                                    loginScreenViewModel.onNextClick()
                                },
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                loginScreenViewModel.onNextClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = email.text.isNotEmpty(),
                        ) {
                            Text("Continuar")
                        }
                    }

                    LoginStep.Password -> {
                        Text("Contraseña", style = MaterialTheme.typography.h4)
                        OutlinedTextField(
                            password,
                            onValueChange = {
                                password = it
                                loginScreenViewModel.onPasswordChange(password.text)
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
                                    loginScreenViewModel.login()
                                },
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                loginScreenViewModel.login()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = password.text.isNotEmpty(),
                        ) {
                            Text("Iniciar sesión")
                        }
                    }

                    LoginStep.Login,
                    LoginStep.Main,
                    -> Unit
                }
            }
        }
    }
}
