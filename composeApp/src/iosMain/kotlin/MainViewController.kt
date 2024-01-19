import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKMapView
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
data class ActiveUser(
    val path: String,
    val email: String,
    val coordindate: CValue<CLLocationCoordinate2D>?,
)

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(
    mkMapView: MKMapView,
    onHomeDisplayed: () -> Unit = {},
) = ComposeUIViewController {
    App(
        maps = { modifier, currentLocationState, mapState ->
            UIKitView(
                modifier = modifier,
                factory = {
                    mkMapView.apply {
                        centerCoordinate = CLLocationCoordinate2DMake(
                            currentLocationState.lat,
                            currentLocationState.long,
                        )
                        setCamera(
                            camera().apply {
                                setCenterCoordinate(
                                    CLLocationCoordinate2DMake(
                                        currentLocationState.lat,
                                        currentLocationState.long,
                                    ),
                                )
                                setAltitude(1700.0)
                            },
                            animated = true,
                        )
                    }
                },
                update = { mkMapView ->
                    mkMapView.removeAnnotations(mkMapView.annotations())
                    val annotations: List<MKAnnotationProtocol?> = mapState
                        .travels
                        .flatMap { travel ->
                            val travelers = travel
                                .activeTravelers
                                .mapNotNull { traveler ->
                                    if (traveler.isTraveling) {
                                        traveler
                                    } else {
                                        null
                                    }
                                }
                                .map { traveler ->
                                    ActiveUser(
                                        travel.path,
                                        traveler.email,
                                        traveler
                                            .currentPosition
                                            ?.let {
                                                CLLocationCoordinate2DMake(it.lat, it.long)
                                            },
                                    )
                                }
                            val drivers = travel
                                .activeDrivers
                                .mapNotNull { driver ->
                                    if (driver.isTraveling) {
                                        driver
                                    } else {
                                        null
                                    }
                                }
                                .map { driver ->
                                    ActiveUser(
                                        travel.path,
                                        driver.email,
                                        driver
                                            .currentPosition
                                            ?.let {
                                                CLLocationCoordinate2DMake(it.lat, it.long)
                                            },
                                    )
                                }
                            listOf(travelers, drivers)
                        }
                        .flatten()
                        .mapNotNull { activeUser ->
                            if (mkMapView
                                    .subviews
                                    .filterIsInstance<MKMarkerAnnotationView>()
                                    .any { mkMarkerAnnotationView: MKMarkerAnnotationView ->
                                        mkMarkerAnnotationView.reuseIdentifier() == activeUser.email
                                    }
                            ) {
                                null
                            } else {
                                activeUser
                            }
                        }
                        .map { activeUser ->
                            val annotation = MKPointAnnotation().apply {
                                setTitle(activeUser.path)
                                activeUser.coordindate?.let { setCoordinate(it) }
                            }
                            val mkMarkerAnnotationView = MKMarkerAnnotationView(
                                annotation,
                                reuseIdentifier = activeUser.email,
                            )
                            mkMarkerAnnotationView
                                .annotation
                        }
                    mkMapView.addAnnotations(annotations)
                },
            )
        },
        onHomeDisplayed = {
            onHomeDisplayed()
        },
    )
}
