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
    @IBAction func getWeatherButton(_ sender: UIButton) {
        addNewAnnotations()
    }
    
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
        locationManager.delegate = self // delegate is this view
        mapView.delegate = self
        // Supposedly remove points of interest
        // Currently, there is a bug where this does not entirely work: https://openradar.appspot.com/28980142
        // As such, POIs may cause the temperature to not be displayed on the annotation and may required zooming in/out to see temp
        mapView.showsPointsOfInterest = false
        // Set initalLocation to CU Boulder Engineering Center
        var initalLocation = CLLocation(latitude: 40.006275, longitude: -105.263536)
        // Request location services permission
        locationManager.requestWhenInUseAuthorization()
        
        if CLLocationManager.authorizationStatus() == .authorizedWhenInUse {
            // Setup locationManager
            locationManager.desiredAccuracy = kCLLocationAccuracyKilometer // ~1km accuracy is sufficient for weather
            locationManager.requestLocation() // Request the current location of the device
            let coordinates = locationManager.location?.coordinate // Gett coords of location
            if coordinates != nil {
                // Set initalLocation to coords where the device is - San Francisco in the simulator
                initalLocation = CLLocation(latitude: coordinates!.latitude, longitude: coordinates!.longitude)
            }
        } else {
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
        }
        
        

        centerMapOnLocation(location: initalLocation) // Center the map of the set location
        // Create five new temperature annotatations
        addNewAnnotations()
    }
    
    func centerMapOnLocation(location: CLLocation) {
        let regionRadius: CLLocationDistance = 10000 // ~10km
        let coordinateRegion = MKCoordinateRegionMakeWithDistance(location.coordinate, regionRadius, regionRadius)
        mapView.setRegion(coordinateRegion, animated: true)
    }
    
    func loadTemperatureJSON(location: CLLocationCoordinate2D) {
        print("load json", location)
        // Ensure given string is valid
        guard let url = URL(string: "https://api.darksky.net/forecast/251044d8d01971a3d739a13ddd102c08/"+String(location.latitude)+","+String(location.longitude)) else {
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
                    self.addAnnotation(temperature: data.currently.temperature, coordinate: CLLocationCoordinate2DMake(data.latitude, data.longitude))
                    
                } catch let err {
                    print("Error", err)
                }
            }
        })
        session.resume()
    }
    
    func addAnnotation(temperature: Float, coordinate: CLLocationCoordinate2D) {
        // Create a CityTemp MKAnnotation object to be added to the mapView
        let tempAnnotation = CityTemp(
            title: String(temperature)+"ºF",
            coordinate: coordinate
        )
        self.mapView.addAnnotation(tempAnnotation) // Add the annotation to the mapView
    }
    
    func addNewAnnotations() {
        // Remove all currently existing annotations
        mapView.removeAnnotations(mapView.annotations)
        // Get region, center, latitudeDelta, and longitudeDelta
        let region = mapView.region
        let center = region.center
        let latDelta = region.span.latitudeDelta
        let longDelta = region.span.longitudeDelta
        var locations = [center] // Initialize locations array with center
        // Append other four locations starting at top left and moving clockwise
        locations.append(CLLocationCoordinate2DMake(center.latitude + (latDelta / 4), center.longitude - (longDelta / 4)))
        locations.append(CLLocationCoordinate2DMake(center.latitude + (latDelta / 4), center.longitude + (longDelta / 4)))
        locations.append(CLLocationCoordinate2DMake(center.latitude - (latDelta / 4), center.longitude + (longDelta / 4)))
        locations.append(CLLocationCoordinate2DMake(center.latitude - (latDelta / 4), center.longitude - (longDelta / 4)))
        // Get the json data for each location and add its annotation
        for location in locations {
            loadTemperatureJSON(location: location)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        print("authorization change", status)
        if status == .authorizedWhenInUse {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
            locationManager.requestLocation()
            let coordinates = locationManager.location?.coordinate
            if coordinates != nil {
                centerMapOnLocation(location: CLLocation(latitude: coordinates!.latitude, longitude: coordinates!.longitude))
                addNewAnnotations()
            }
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        print(locations)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("locationManager failed with error", error)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

extension ViewController: MKMapViewDelegate {
    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        guard let annotation = annotation as? CityTemp else { return nil }

        let identifer = "temperature"
        var view: MKMarkerAnnotationView

        if let dequeuedView = mapView.dequeueReusableAnnotationView(withIdentifier: identifer) as? MKMarkerAnnotationView {
            dequeuedView.annotation = annotation
            view = dequeuedView
        } else {
            view = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifer)
            view.titleVisibility = .visible
            view.displayPriority = .required
            view.canShowCallout = false
        }
        return view
    }
}

