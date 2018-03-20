//
//  CityTemp.swift
//  WeatherMap
//
//  Copyright © 2018 ncarmine. All rights reserved.
//

import Foundation
import MapKit

// Annotation class for a city's current temperature
class CityTemp: NSObject, MKAnnotation {
    let title: String? // title of annotation - current temp
    let coordinate: CLLocationCoordinate2D // lat, long of annotation
    
    // Initialize class with given values
    init(title: String, coordinate: CLLocationCoordinate2D) {
        self.title = title
        self.coordinate = coordinate
        super.init()
    }
}
