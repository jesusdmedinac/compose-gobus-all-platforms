import UIKit
import SwiftUI
import ComposeApp
import MapKit

extension UIViewController : CLLocationManagerDelegate, MKMapViewDelegate {
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
    HomeScreenViewModel.companion.INSTANCE.onLocationChange(lat: Double(location.coordinate.latitude), long: Double(location.coordinate.longitude))
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
  
  public func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
    if annotation.title == "My Location" {
      return nil
    }
    
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

    return annotationView
  }
}

struct ComposeView: UIViewControllerRepresentable {
  let locationManager = CLLocationManager()
  
  func makeUIViewController(context: Context) -> UIViewController {
    let mkMapView = MKMapView()
    
    let mainViewController: UIViewController = MainViewControllerKt.MainViewController(
      mkMapView: mkMapView,
      onHomeDisplayed: {
      self.locationManager.requestAlwaysAuthorization()
    })
    mkMapView.delegate = mainViewController
    mkMapView.showsUserLocation = true
    locationManager.delegate = mainViewController
    
    return mainViewController
  }
  
  func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
  var body: some View {
    ComposeView()
      .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
  }
}



