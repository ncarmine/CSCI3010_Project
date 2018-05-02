package com.ncarmine.weathermap

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.experimental.async
import java.net.URL

import kotlinx.serialization.*
import kotlinx.serialization.json.JSON
import android.os.StrictMode
import android.widget.ImageButton
import com.google.android.gms.maps.model.Marker
import kotlin.math.abs


// Serializable Weather and CurrentData data classes to put json data into
@Serializable
data class Weather(val timezone: String, val latitude: Double, val longitude: Double, val currently: CurrentData)

@Serializable
data class CurrentData(val time: Long, val temperature: Float, val apparentTemperature: Float, val humidity: Float, val precipProbability: Float)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var markers = arrayOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val fetchWeatherBtn: ImageButton = findViewById(R.id.fetchWeatherBtn)
        fetchWeatherBtn.setOnClickListener { fetchWeather() }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Engineering Center location
        val location = LatLng(40.006275, -105.263536)
        // Focus over center
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12.5f))
        // Get current weather points
        fetchWeather()
    }

    fun addMarkerFromJSON(location: LatLng) {
        async {
            // Load weather at location
            val result = URL("https://api.darksky.net/forecast/251044d8d01971a3d739a13ddd102c08/"+location.latitude.toString()+","+location.longitude.toString()).readText()
            runOnUiThread {
                // Serialize JSON for easy referencing
                val weather = JSON.nonstrict.parse<Weather>(result)
                // Add a marker at the location
                markers += mMap.addMarker(MarkerOptions().position(LatLng(weather.latitude, weather.longitude)).title(weather.currently.temperature.toString()))
            }
        }
    }

    fun fetchWeather() {
        // Enable multiple async
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        async {
            runOnUiThread {
                // Remove existing markers
                for (marker in markers) {
                    marker.remove()
                }
                markers = emptyArray() // Empty marker array
                // Get the region of the current map and center
                val region = mMap.projection.visibleRegion.latLngBounds
                val center = region.center
                val top = center.latitude + abs((center.latitude - region.northeast.latitude)/2)
                val bottom = center.latitude - abs((center.latitude - region.southwest.latitude)/2)
                val left = center.longitude - abs((center.longitude - region.southwest.longitude)/2)
                val right = center.longitude + abs((center.longitude - region.northeast.longitude)/2)
                // Put markers in the center and each corner, starting with top-left and moving clockwise
                addMarkerFromJSON(center)
                addMarkerFromJSON(LatLng(top, left))
                addMarkerFromJSON(LatLng(top, right))
                addMarkerFromJSON(LatLng(bottom, right))
                addMarkerFromJSON(LatLng(bottom, left))
            }
        }
    }
}