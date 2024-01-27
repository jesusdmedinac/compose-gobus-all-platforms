import UIKit
import SwiftUI
import ComposeApp
import MapKit

struct ComposeView: UIViewControllerRepresentable {
  let locationManager = CLLocationManager()
  let mapViewDelegateImpl = MapViewDelegateImpl()
  let cLLocationManagerDelegateImpl = CLLocationManagerDelegateImpl()
  
  func makeUIViewController(context: Context) -> UIViewController {
    let mkMapView = MKMapView()
    let mainViewController: UIViewController = MainViewControllerKt.MainViewController(
      mkMapView: mkMapView,
      onHomeDisplayed: {
        self.locationManager.requestAlwaysAuthorization()
      })
    mkMapView.delegate = mapViewDelegateImpl
    mkMapView.showsUserLocation = true
    locationManager.delegate = cLLocationManagerDelegateImpl
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



