
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jesusdmedinac.gobus.data.GobusRepository
import com.jesusdmedinac.gobus.data.MongoDBAtlasDataSource
import com.jesusdmedinac.gobus.data.MongoDBRealmDataSource
import com.jesusdmedinac.gobus.data.local.GobusLocalDataSource
import com.jesusdmedinac.gobus.domain.model.UserLocation
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

@Composable
fun App(
    maps: @Composable (Modifier, UserLocation, MapState) -> Unit,
    onHomeDisplayed: () -> Unit = {},
) {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf("splash") }
        when (currentScreen) {
            "splash" -> {
                val mainScreenViewModel = remember {
                    MainScreenViewModel(
                        GobusRepository(
                            MongoDBAtlasDataSource(),
                            GobusLocalDataSource(MongoDBRealmDataSource().realm),
                        ),
                    )
                }
                MainScreen(
                    mainScreenViewModel,
                    onLoginClick = {
                        currentScreen = "login"
                    },
                    onSignupClick = {
                        currentScreen = "signup"
                    },
                    onUserIsLoggedIn = {
                        currentScreen = "home"
                    },
                )
            }

            "login" -> {
                val loginScreenViewModel = remember {
                    LoginScreenViewModel(
                        GobusRepository(
                            MongoDBAtlasDataSource(),
                            GobusLocalDataSource(MongoDBRealmDataSource().realm),
                        ),
                    )
                }
                LoginScreen(
                    loginScreenViewModel,
                    navigateToMain = {
                        currentScreen = "splash"
                    },
                    navigateToHome = {
                        currentScreen = "home"
                    },
                )
            }

            "signup" -> {
                val signupScreenViewModel = remember {
                    SignupScreenViewModel(
                        GobusRepository(
                            MongoDBAtlasDataSource(),
                            GobusLocalDataSource(MongoDBRealmDataSource().realm),
                        ),
                    )
                }
                SignupScreen(
                    signupScreenViewModel,
                    navigateToMain = {},
                    navigateToHome = {
                        currentScreen = "home"
                    },
                )
            }

            "home" -> {
                val homeScreenViewModel = remember { HomeScreenViewModel.INSTANCE }
                val mapViewModel = remember { MapViewModel.INSTANCE }
                HomeScreen(
                    homeScreenViewModel,
                    mapViewModel,
                    maps,
                )
                LaunchedEffect(Unit) {
                    onHomeDisplayed()
                    mapViewModel.fetchTravelingDriversAndTravelers()
                }
            }
        }
    }
}
