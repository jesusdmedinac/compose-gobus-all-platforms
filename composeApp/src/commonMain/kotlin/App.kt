import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Color(0xFF4285F4), // Deep blue (conveys trust and dependability)
            primaryVariant = Color(0xFF2196F3), // Darker blue (adds depth and contrast)
            secondary = Color(0xFFFFF9C4), // Pale yellow (hints at progress and movement)
            secondaryVariant = Color(0xFFFCE883), // Lighter yellow (soft and approachable)
            background = Color(0xFFFAFAFA), // Light gray (neutral and clean)
            surface = Color(0xFFFFFFFF), // White (crisp and legible)
            error = Color(0xFFF44336), // Orange (warning color for delays or disruptions)
            onPrimary = Color(0xFFFFFFFF), // White on blue
            onSecondary = Color(0xFF000000), // Black on yellow
            onBackground = Color(0xFF212121), // Dark gray on light gray
            onSurface = Color(0xFF212121), // Dark gray on white
            onError = Color(0xFFFFFFFF), // White on error
            isLight = true,
        ),

        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
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
