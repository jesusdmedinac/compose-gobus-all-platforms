//
//  MapViewDelegateImpl.swift
//  iosApp
//
//  Created by Jesus Daniel Medina Cruz on 20/01/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import MapKit
import ComposeApp

class MapViewDelegateImpl : NSObject, MKMapViewDelegate {
  var currentLocation: SharedUserLocation = SharedUserLocation(
    email: "",
    latitude: 0.0,
    longitude: 0.0,
    bearing: 0.0,
    timestamp: InstantUtilsKt.now(),
    pathName: ""
  )
  private var mapState: MapState = MapState(
    paths: [],
    userLocations: [],
    currentUser: nil, 
    latitude: 0.0,
    longitude: 0.0,
    bearing: 0.0,
    throwable: nil)
  var mkMapView: MKMapView? = nil
  
  public func bindMap() {
    guard let unwrappedMkMapView = mkMapView else {
      print("MKMapView is nil")
      return
    }
    unwrappedMkMapView.centerCoordinate = CLLocationCoordinate2DMake(
      currentLocation.latitude,
      currentLocation.longitude
    )
    unwrappedMkMapView.setCamera(
      MKMapCamera(
        lookingAtCenter: CLLocationCoordinate2DMake(
          currentLocation.latitude,
          currentLocation.longitude
        ),
        fromEyeCoordinate: CLLocationCoordinate2D(
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude
        ),
        eyeAltitude: CLLocationDistance(1700)),
      animated: true
    )
  }
  
  public func onMapStateChange(mapState: MapState) {
    self.mapState = mapState
    self.currentLocation = mapState.initialLocation()
    guard let unwrappedMkMapView = mkMapView else {
      print("MKMapView is nil")
      return
    }
    unwrappedMkMapView.removeAnnotations(unwrappedMkMapView.annotations)
    
    let busAnnotations = self
      .mapState
      .userLocations
      .map { userLocation in
        let busAnnotation = BusAnnotation()
        busAnnotation.coordinate = CLLocationCoordinate2DMake(userLocation.latitude, userLocation.longitude)
        busAnnotation.path = userLocation.pathName
        busAnnotation.email = userLocation.email
        busAnnotation.angle = CGFloat((userLocation.bearing + 90) * Double.pi / 180)
        busAnnotation.title = userLocation.pathName
        
        return busAnnotation
      }
    
    unwrappedMkMapView.addAnnotations(busAnnotations)
  }
  
  public func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
    guard let busAnnotation = annotation as? BusAnnotation else {
      return nil
    }
    
    var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: busAnnotation.email)
    
    if annotationView == nil {
      annotationView = MKPinAnnotationView(annotation: busAnnotation, reuseIdentifier: busAnnotation.email)
      annotationView?.canShowCallout = true
      
      let btn = UIButton(type: .detailDisclosure)
      annotationView?.rightCalloutAccessoryView = btn
      
      let busImage: UIImage = #imageLiteral(resourceName: "bus")
      annotationView?.image = busImage
      annotationView?.centerOffset = CGPointMake(0, 1)
    } else {
      annotationView?.annotation = annotation
    }
    annotationView?.transform = CGAffineTransform(rotationAngle: busAnnotation.angle)
    
    return annotationView
  }
}
