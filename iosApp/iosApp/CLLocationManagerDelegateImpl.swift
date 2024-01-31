//
//  CLLocationManagerDelegateImpl.swift
//  iosApp
//
//  Created by Jesus Daniel Medina Cruz on 20/01/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import MapKit
import ComposeApp

class CLLocationManagerDelegateImpl : NSObject, CLLocationManagerDelegate {
  public func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
    switch manager.authorizationStatus {
    case .notDetermined:
      print("When traveler did not yet determined")
    case .restricted:
      print("Restricted by parental control")
    case .denied:
      print("When traveler select option Dont't Allow")
    case .authorizedAlways:
      setupLocationManager(manager)
    default:
      print("default")
    }
  }
  
  public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let location = locations.last else { return }
    HomeScreenViewModel.companion.INSTANCE.onLocationChange(
      userLocation: SharedUserLocation(
        lat: Double(location.coordinate.latitude),
        long: Double(location.coordinate.longitude),
        bearing: Double(location.course), 
        timestamp: InstantUtilsKt.now()
      )
    )
  }
  
  public func locationManager(_ manager: CLLocationManager, didFailWithError error: Swift.Error) {
    
  }
  
  private func setupLocationManager(_ manager: CLLocationManager) {
    if CLLocationManager.locationServicesEnabled() {
      manager.delegate = self
      manager.allowsBackgroundLocationUpdates = true
      manager.showsBackgroundLocationIndicator = true
      manager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
      manager.startUpdatingLocation()
      manager.requestLocation()
    }
  }
}
