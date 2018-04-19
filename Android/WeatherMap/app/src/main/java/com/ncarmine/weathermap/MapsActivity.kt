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



// Serializable Weather and CurrentData data classes to put json data into
@Serializable
data class Weather(val timezone: String, val latitude: Double, val longitude: Double, val currently: CurrentData)

@Serializable
data class CurrentData(val time: Long, val temperature: Float, val apparentTemperature: Float, val humidity: Float, val precipProbability: Float)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13.5f))
        // Enable multiple async
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        async {
            runOnUiThread {
                // Get the region of the current map
                val region = mMap.projection.visibleRegion
                // Put markers in the center, and each corner
                addMarkerFromJSON(location)
                addMarkerFromJSON(region.farLeft)
                addMarkerFromJSON(region.farRight)
                addMarkerFromJSON(region.nearLeft)
                addMarkerFromJSON(region.nearRight)
            }
        }
        // Zoom outwards
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12.5f))

    }

    fun addMarkerFromJSON(location: LatLng) {
        async {
            // Load weather at location
            val result = URL("https://api.darksky.net/forecast/251044d8d01971a3d739a13ddd102c08/"+location.latitude.toString()+","+location.longitude.toString()).readText()
            runOnUiThread {
                // Serialize JSON for easy referencing
                val weather = JSON.nonstrict.parse<Weather>(result)
                // Add a marker at the location
                mMap.addMarker(MarkerOptions().position(LatLng(weather.latitude, weather.longitude)).title(weather.currently.temperature.toString()))
            }
        }

    }
}