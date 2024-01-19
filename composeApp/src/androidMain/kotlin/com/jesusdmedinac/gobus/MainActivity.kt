package com.jesusdmedinac.gobus

import App
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jesusdmedinac.gobus.domain.model.Travel

class MainActivity : ComponentActivity() {

    private val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private var mapProperties by mutableStateOf(
        MapProperties(
            maxZoomPreference = 20f,
            minZoomPreference = 5f,
            isMyLocationEnabled = false,
        ),
    )

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                val intent =
                    Intent(this, CurrentLocationService::class.java)
                startForegroundService(intent)
                mapProperties = mapProperties.copy(
                    isMyLocationEnabled = true,
                )
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(
                maps = { modifier, userPosition, mapState ->
                    var currentLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(currentLatLng, 17f)
                    }
                    var wasCameraMoved by remember { mutableStateOf(false) }
                    LaunchedEffect(userPosition) {
                        currentLatLng = LatLng(userPosition.lat, userPosition.long)
                        if (!wasCameraMoved &&
                            userPosition.lat != 0.0 &&
                            userPosition.long != 0.0
                        ) {
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(currentLatLng, 17f)
                            wasCameraMoved = true
                        }
                    }
                    val mapUiSettings = remember {
                        MapUiSettings(
                            mapToolbarEnabled = false,
                        )
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties,
                        uiSettings = mapUiSettings,
                    ) {
                        mapState
                            .travels
                            .forEach { travel: Travel ->
                                travel
                                    .activeTravelers
                                    .mapNotNull { traveler ->
                                        if (traveler.isTraveling) {
                                            traveler
                                        } else {
                                            null
                                        }
                                    }
                                    .forEach { traveler ->
                                        traveler
                                            .currentPosition
                                            ?.let { LatLng(it.lat, it.long) }
                                            ?.let { markerPosition ->
                                                Marker(
                                                    state = MarkerState(
                                                        position = markerPosition,
                                                    ),
                                                    title = travel.path,
                                                    snippet = travel.path,
                                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_ceil),
                                                )
                                            }
                                    }
                                travel
                                    .activeDrivers
                                    .mapNotNull { driver ->
                                        if (driver.isTraveling) {
                                            driver
                                        } else {
                                            null
                                        }
                                    }
                                    .forEach { driver ->
                                        driver
                                            .currentPosition
                                            ?.let { LatLng(it.lat, it.long) }
                                            ?.let { markerPosition ->
                                                Marker(
                                                    state = MarkerState(
                                                        position = markerPosition,
                                                    ),
                                                    title = travel.path,
                                                    snippet = travel.path,
                                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_ceil),
                                                )
                                            }
                                    }
                            }
                    }
                },
                onHomeDisplayed = {
                    startTrackingLocation(onPermissionGranted = {
                        mapProperties = mapProperties.copy(
                            isMyLocationEnabled = true,
                        )
                    })
                },
            )
        }
    }

    private fun startTrackingLocation(onPermissionGranted: () -> Unit) {
        val checkSelfPermissionMap = permissions
            .map {
                it to (
                    ContextCompat.checkSelfPermission(
                        this,
                        it,
                    ) == PackageManager.PERMISSION_GRANTED
                    )
            }
        when {
            checkSelfPermissionMap.all { it.second } -> {
                val intent =
                    Intent(this, CurrentLocationService::class.java)
                startForegroundService(intent)
                onPermissionGranted()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) -> {
                Log.d("MainActivity", "Fine")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) -> {
                Log.d("MainActivity", "Coarse")
            }

            else -> {
                checkSelfPermissionMap
                    .first { !it.second }
                    .let {
                        requestPermissionLauncher.launch(
                            it.first,
                        )
                    }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(maps = { modifier, userPosition, mapState -> })
}
