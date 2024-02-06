import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import com.jesusdmedinac.gobus.domain.model.UserLocation
import com.jesusdmedinac.gobus.presentation.viewmodel.MapState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.MapKit.MKMapView

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(
    mkMapView: MKMapView,
    onHomeDisplayed: () -> Unit = {},
    onCurrentLocationChange: (UserLocation) -> Unit = {},
    onMapStateChange: (MapState) -> Unit = {},
) = ComposeUIViewController {
    App(
        maps = { modifier, mapState ->
            mapState
                .run {
                    currentTraveler
                        ?.currentLocation
                        ?.let { onCurrentLocationChange(it) }

                    currentDriver
                        ?.currentLocation
                        ?.let { onCurrentLocationChange(it) }
                }
            onMapStateChange(mapState)
            UIKitView(
                modifier = modifier,
                factory = {
                    mkMapView
                },
                update = { mkMapView ->
                },
            )
        },
        onHomeDisplayed = onHomeDisplayed,
    )
}
