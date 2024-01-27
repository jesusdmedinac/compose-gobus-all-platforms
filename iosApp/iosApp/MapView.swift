//
//  MapView.swift
//  iosApp
//
//  Created by Jesus Daniel Medina Cruz on 20/01/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import MapKit

struct MapView : View {
    var body: some View {
        Map {
          Marker("San Francisco", coordinate: CLLocationCoordinate2D(latitude: 0, longitude: 0))
            /*Marker("San Francisco City Hall", coordinate: cityHallLocation)
                .tint(.orange)
            Marker("San Francisco Public Library", coordinate: publicLibraryLocation)
                .tint(.blue)
            Annotation("Diller Civic Center Playground", coordinate: playgroundLocation) {
                ZStack {
                    RoundedRectangle(cornerRadius: 5)
                        .fill(Color.yellow)
                    Text("🛝")
                        .padding(5)
                }
            }*/
        }
        .mapControlVisibility(.hidden)
    }
}
