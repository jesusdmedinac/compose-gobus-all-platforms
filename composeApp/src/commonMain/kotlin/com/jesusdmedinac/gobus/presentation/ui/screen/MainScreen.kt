package com.jesusdmedinac.gobus.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jesusdmedinac.gobus.presentation.viewmodel.MainScreenViewModel

@Composable
fun MainScreen(
    mainScreenViewModel: MainScreenViewModel,
    onLoginClick: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onUserIsLoggedIn: () -> Unit = {},
) {
    val mainScreenState by mainScreenViewModel.container.stateFlow.collectAsState()
    LaunchedEffect(Unit) {
        mainScreenViewModel.validateUserLoggedIn()
    }
    LaunchedEffect(mainScreenState.isUserLoggedIn) {
        if (mainScreenState.isUserLoggedIn) {
            onUserIsLoggedIn()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onLoginClick,
                ) {
                    Text("Iniciar sesión")
                }
                TextButton(
                    onClick = onSignupClick,
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}
