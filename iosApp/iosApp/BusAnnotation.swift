//
//  BusAnnotation.swift
//  iosApp
//
//  Created by Jesus Daniel Medina Cruz on 29/01/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import MapKit

class BusAnnotation : NSObject, MKAnnotation {
  var coordinate: CLLocationCoordinate2D = CLLocationCoordinate2DMake(0.0, 0.0)
  
  var path: String = ""
  var email: String = ""
  var angle: CGFloat = 0.0
  var title: String?
}
