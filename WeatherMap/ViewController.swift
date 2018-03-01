//
//  ViewController.swift
//  WeatherMap
//
//  Copyright © 2018 ncarmine. All rights reserved.
//

import UIKit
import MapKit
import CoreLocation

class ViewController: UIViewController, CLLocationManagerDelegate {
    
    @IBOutlet weak var mapView: MKMapView!
    
    var locationManager: CLLocationManager!
    
    // Codable struct for json weather data returned by DarkSky
    struct WeatherData: Codable {
        let latitude: Double
        let longitude: Double
        let timezone: String
        let currently: Currently
    }
    
    // Separate struct for the "currently" dictionary within the json response
    struct Currently: Codable {
        let time: UInt64
        let temperature: Float
        let apparentTemperature: Float
        let humidity: Float
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Declare locationManager
        locationManager = CLLocationManager()
        // Set initalLocation to CU Boulder Engineering Center
        var initalLocation = CLLocation(latitude: 40.006275, longitude: -105.263536)
        
        switch CLLocationManager.authorizationStatus() {
        case .notDetermined:
            // Request location services permission
            locationManager.requestWhenInUseAuthorization()
        case .denied, .restricted:
            // Tell user to enable location services when they have it disabled
            let alert = UIAlertController(title: "Location Services Disabled", message: "Enable location services in Settings to display temperatures in your local area", preferredStyle: .alert)
            let settingsAction = UIAlertAction(title: "Settings", style: .default, handler: { action in
                if UIApplication.shared.canOpenURL(URL(string: UIApplicationOpenSettingsURLString)!) {
                    UIApplication.shared.open(URL(string: UIApplicationOpenSettingsURLString)!, options: [:], completionHandler: nil)
                }
            })
            let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
            alert.addAction(cancelAction)
            alert.addAction(settingsAction)
            present(alert, animated: true, completion: nil)
        case .authorizedWhenInUse, .authorizedAlways:
            // Setup locationManager
            locationManager.delegate = self // delegate is this view
            locationManager.desiredAccuracy = kCLLocationAccuracyKilometer // ~1km accuracy is sufficient for weather
            locationManager.requestLocation() // Request the current location of the device
            let coordinates = locationManager.location?.coordinate // Gett coords of location
            if coordinates != nil {
                // Set initalLocation to coords where the device is - San Francisco in the simulator
                initalLocation = CLLocation(latitude: coordinates!.latitude, longitude: coordinates!.longitude)
            }
        }

        centerMapOnLocation(location: initalLocation) // Center the map of the set location
        // Load the DarkSky JSON at the selected location and add the annotation
        loadJSON(urlPath: "https://api.darksky.net/forecast/251044d8d01971a3d739a13ddd102c08/"+String(initalLocation.coordinate.latitude)+","+String(initalLocation.coordinate.longitude))
    }
    
    func centerMapOnLocation(location: CLLocation) {
        let regionRadius: CLLocationDistance = 5000 // ~5km
        let coordinateRegion = MKCoordinateRegionMakeWithDistance(location.coordinate, regionRadius, regionRadius)
        mapView.setRegion(coordinateRegion, animated: true)
    }
    
    func loadJSON(urlPath: String) {
        // Ensure given string is valid
        guard let url = URL(string: urlPath) else {
            print("URL Error")
            return
        }
        
        // Fetch the data the the url
        let session = URLSession.shared.dataTask(with: url, completionHandler: {(data, response, error) in
            let httpResponse = response as! HTTPURLResponse
            guard httpResponse.statusCode == 200 else {
                print("File Download Error")
                return
            }
            // Process successfully fetched data
            DispatchQueue.main.async {
                let jsonDecoder = JSONDecoder() // Declare JSONDecoder
                do {
                    // Attempt to decode JSON data into WeatherData codeable struct
                    let data = try jsonDecoder.decode(WeatherData.self, from: data!)
                    // Add the temperature as an MKAnnotation set to the locationManager's coordinates
                    let tempAnnotation = CityTemp(
                        title: (String(data.currently.temperature)+"ºF"),
                        coordinate: CLLocationCoordinate2DMake(data.latitude, data.longitude)
                    )
                    self.mapView.addAnnotation(tempAnnotation) // Add the annotation to the mapView
                } catch let err {
                    print("Error", err)
                }
            }
        })
        session.resume()
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        if status == .authorizedWhenInUse {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
            locationManager.requestLocation()
            let coordinates = locationManager.location?.coordinate
            if coordinates != nil {
                centerMapOnLocation(location: CLLocation(latitude: coordinates!.latitude, longitude: coordinates!.longitude))
                loadJSON(urlPath: "https://api.darksky.net/forecast/251044d8d01971a3d739a13ddd102c08/"+String(coordinates!.latitude)+","+String(coordinates!.longitude))
            }
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        print(locations)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("locationManager fialed with error", error)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

