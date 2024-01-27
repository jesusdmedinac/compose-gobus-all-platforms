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
  
  public func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
    if annotation.title == "My Location" {
      return nil
    }
    
    if let mapState = MapViewModel
      .companion
      .INSTANCE
      .state
      .value as? MapState {
      
      let annotationIdentifier: String = (annotation.title ?? "") ?? ""
      
      let busImage: UIImage = #imageLiteral(resourceName: "bus")
      var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: annotationIdentifier)
      
      if annotationView == nil {
        annotationView = MKPinAnnotationView(annotation: annotation, reuseIdentifier: annotationIdentifier)
        annotationView?.canShowCallout = true
        
        let btn = UIButton(type: .detailDisclosure)
        annotationView?.rightCalloutAccessoryView = btn
        annotationView?.image = busImage
      } else {
        annotationView?.annotation = annotation
      }
      let email = (annotation.title ?? "") ?? ""
      let traveler = mapState
        .getTravelerBy(email: email)
      let driver = mapState
        .getDriverBy(email: email)
      if traveler != nil {
        annotation.title = ""
      }
      /*
       guard let rotation = annotationView?.transform.rotated(by: 1) else {
       return nil
       }
       annotationView?.transform = rotation*/
      
      return annotationView
    } else { return nil }
  }
}
