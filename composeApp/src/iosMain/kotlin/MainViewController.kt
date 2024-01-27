import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGAffineTransform
import platform.CoreGraphics.CGAffineTransformMakeRotation
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.setValue
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKAnnotationViewDragState
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.darwin.NSObject
import kotlin.math.PI

@OptIn(ExperimentalForeignApi::class)
data class ActiveUser(
    val path: String,
    val email: String,
    val angle: CValue<CGAffineTransform>,
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
                                        angle = CGAffineTransformMakeRotation(
                                            traveler.currentPosition?.bearing ?: 0.0,
                                        ),
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
                                    val radians =
                                        ((driver.currentPosition?.bearing ?: 0.0) * PI / 180)
                                    println("radians $radians")
                                    ActiveUser(
                                        travel.path,
                                        driver.email,
                                        angle = CGAffineTransformMakeRotation(radians),
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
                            MKPointAnnotation().apply {
                                setTitle(activeUser.email)
                                activeUser.coordindate?.let { setCoordinate(it) }
                            }
                            /*val annotation = MKPointAnnotation().apply {
                                setTitle(activeUser.path)
                                activeUser.coordindate?.let { setCoordinate(it) }
                            }
                            val mkMarkerAnnotationView = MKMarkerAnnotationView(
                                annotation,
                                reuseIdentifier = activeUser.email,
                            ).apply {
                                setTransform(activeUser.angle)
                            }
                            mkMarkerAnnotationView
                                .annotation*/
                        }
                    mkMapView.addAnnotations(annotations)
                },
            )
        },
        onHomeDisplayed = onHomeDisplayed,
    )
}
