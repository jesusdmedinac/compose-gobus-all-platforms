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
    lat: 0.0,
    long: 0.0,
    bearing: 0.0,
    timestamp: InstantUtilsKt.now()
  )
  private var mapState: MapState = MapState(paths: [], currentTraveler: nil, currentDriver: nil)
  var mkMapView: MKMapView? = nil
  
  public func bindMap() {
    guard let unwrappedMkMapView = mkMapView else {
      print("MKMapView is nil")
      return
    }
    unwrappedMkMapView.centerCoordinate = CLLocationCoordinate2DMake(
      currentLocation.lat,
      currentLocation.long_
    )
    unwrappedMkMapView.setCamera(
      MKMapCamera(
        lookingAtCenter: CLLocationCoordinate2DMake(
          currentLocation.lat,
          currentLocation.long_
        ),
        fromEyeCoordinate: CLLocationCoordinate2D(
          latitude: currentLocation.lat,
          longitude: currentLocation.long_
        ), 
        eyeAltitude: CLLocationDistance(1700)),
      animated: true
    )
  }
  
  public func onMapStateChange(mapState: MapState) {
    self.mapState = mapState
    guard let unwrappedMkMapView = mkMapView else {
      print("MKMapView is nil")
      return
    }
    unwrappedMkMapView.removeAnnotations(unwrappedMkMapView.annotations)
    
    let busAnnotations = self
      .mapState
      .paths
      .compactMap { path in
        let travelers = path
          .activeTravelers
          .map { traveler in
            guard traveler.isTraveling else {
              return BusAnnotation()
            }
            guard let currentLocation = traveler.currentLocation else {
              return BusAnnotation()
            }
            let busAnnotation = BusAnnotation()
            busAnnotation.coordinate = CLLocationCoordinate2DMake(currentLocation.lat, currentLocation.long_)
            busAnnotation.path = path.name
            busAnnotation.email = traveler.email
            busAnnotation.angle = CGFloat((currentLocation.bearing + 90) * Double.pi / 180)
            busAnnotation.title = path.name
            
            return busAnnotation
          }
        
        let drivers = path
          .activeDrivers
          .map { driver in
            guard driver.isTraveling else {
              return BusAnnotation()
            }
            guard let currentLocation = driver.currentLocation else {
              return BusAnnotation()
            }
            let busAnnotation = BusAnnotation()
            busAnnotation.coordinate = CLLocationCoordinate2DMake(currentLocation.lat, currentLocation.long_)
            busAnnotation.path = path.name
            busAnnotation.email = driver.email
            busAnnotation.angle = CGFloat((currentLocation.bearing + 90) * Double.pi / 180)
            busAnnotation.title = path.name
            return busAnnotation
          }
        
        return [travelers, drivers]
      }
      .flatMap { $0.flatMap { $0 } }
    
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
