import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.jesusdmedinac.gobus.presentation.ui.screen.HomeScreen
import com.jesusdmedinac.gobus.presentation.ui.screen.LoginScreen
import com.jesusdmedinac.gobus.presentation.ui.screen.MainScreen
import com.jesusdmedinac.gobus.presentation.ui.screen.SignupScreen
import com.jesusdmedinac.gobus.presentation.viewmodel.HomeScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.LoginScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.MainScreenViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.MapState
import com.jesusdmedinac.gobus.presentation.viewmodel.MapViewModel
import com.jesusdmedinac.gobus.presentation.viewmodel.SignupScreenViewModel
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App(
    maps: @Composable (Modifier, MapState) -> Unit,
    onHomeDisplayed: () -> Unit = {},
) {
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Color(0xFF4285F4),
            primaryVariant = Color(0xFF2196F3),
            secondary = Color(0xFFFFF9C4),
            secondaryVariant = Color(0xFFFCE883),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFF44336),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFF000000),
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF212121),
            onError = Color(0xFFFFFFFF),
            isLight = true,
        ),

        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
        KoinContext {
            PreComposeApp {
                val navigator = rememberNavigator()
                NavHost(
                    navigator = navigator,
                    navTransition = NavTransition(),
                    initialRoute = "splash",
                ) {
                    scene(
                        route = "splash",
                        navTransition = NavTransition(),
                    ) {
                        val mainScreenViewModel: MainScreenViewModel = koinInject()
                        MainScreen(
                            mainScreenViewModel,
                            onLoginClick = {
                                navigator.navigate("login")
                            },
                            onSignupClick = {
                                navigator.navigate("signup")
                            },
                            onUserIsLoggedIn = {
                                navigator.navigate("home")
                            },
                        )
                    }

                    scene(
                        route = "login",
                        navTransition = NavTransition(),
                    ) {
                        val loginScreenViewModel: LoginScreenViewModel = koinInject()
                        LoginScreen(
                            loginScreenViewModel,
                            navigateToMain = {
                                navigator.navigate("splash")
                            },
                            navigateToHome = {
                                navigator.navigate("home")
                            },
                        )
                    }

                    scene(
                        route = "signup",
                        navTransition = NavTransition(),
                    ) {
                        val signupScreenViewModel: SignupScreenViewModel = koinInject()
                        SignupScreen(
                            signupScreenViewModel,
                            navigateToMain = {},
                            navigateToHome = {
                                navigator.navigate("home")
                            },
                        )
                    }

                    scene(
                        route = "home",
                        navTransition = NavTransition(),
                    ) {
                        val homeScreenViewModel: HomeScreenViewModel = koinInject()
                        val mapViewModel: MapViewModel = koinInject()
                        HomeScreen(
                            homeScreenViewModel,
                            mapViewModel,
                            maps,
                        )
                        LaunchedEffect(Unit) {
                            onHomeDisplayed()
                            mapViewModel.fetchMapState()
                        }
                    }
                }
            }
        }
    }
}
