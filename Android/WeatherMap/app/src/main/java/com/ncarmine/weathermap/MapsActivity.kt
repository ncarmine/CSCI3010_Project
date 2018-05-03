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
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import com.google.android.gms.maps.model.Marker
import kotlin.math.abs


// Serializable Weather and HourData data classes to put json data into
@Serializable
data class Weather(val timezone: String, val latitude: Double, val longitude: Double,
                   val currently: HourData, val hourly: HourWeather, val daily: DayWeather)

@Serializable
data class HourWeather(val summary: String, val icon: String, val data: Array<HourData>)

@Serializable
data class DayWeather(val summary: String, val icon: String, val data: Array<DayData>)

@Serializable
data class HourData(val time: Long, val temperature: Float, val apparentTemperature: Float,
                    val humidity: Float, val precipProbability: Float)

@Serializable
data class DayData(val time: Long, val temperatureHigh: Float, val temperatureLow: Float,
                   val apparentTemperatureHigh: Float, val apparentTemperatureLow: Float,
                   val humidity: Float, val precipProbability: Float)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var markers = arrayOf<Marker>()
    private var weatherOnMap = arrayOf<Weather>()
    // HashMap to grab index of where weather is in data
    private val timeMap = hashMapOf(
            "1h" to 1,
            "2h" to 2,
            "3h" to 3,
            "6h" to 4,
            "12h" to 5,
            "1d" to 1,
            "2d" to 2,
            "3d" to 3
    )
    private var timeSelected = "Now"

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
                weatherOnMap += weather // add weather to weatherOnMap array
                // Get the index of where to grab time from based off time hashmap
                val timeIndex = timeMap[timeSelected]
                // Add a marker at the location for the weather based off the appropriate time slot
                when (timeSelected) {
                    "Now" -> markers += mMap.addMarker(MarkerOptions().position(LatLng(weather.latitude, weather.longitude)).title(weather.currently.temperature.toString()))
                    "1h", "2h", "3h", "6h", "12h" -> { // Hourly
                        if (timeIndex != null) {
                            markers += mMap.addMarker(MarkerOptions().position(LatLng(weather.latitude, weather.longitude)).title(weather.hourly.data[timeIndex].temperature.toString()))
                        }
                    }
                    "1d", "2d", "3d" -> { // Daily
                        if (timeIndex != null) {
                            markers += mMap.addMarker(MarkerOptions().position(LatLng(weather.latitude, weather.longitude)).title(weather.daily.data[timeIndex].temperatureHigh.toString()))
                        }
                    }
                }
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
                weatherOnMap = emptyArray() // Empty weather array
                // Get the region of the current map and center
                val region = mMap.projection.visibleRegion.latLngBounds
                val center = region.center
                // Calculate the four cross-sections to put markers based off average of center and respective corners
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.time_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        timeSelected = item.toString()
        // Get the index of where to grab time from based off time hashmap
        val timeIndex = timeMap[timeSelected]
        // Get the weather for the appropriate time slot
        when (timeSelected) {
            "Now" -> {
                for ((marker, weather) in markers.zip(weatherOnMap)) {
                    marker.title = weather.currently.temperature.toString()
                }
            }
            "1h", "2h", "3h", "6h", "12h" -> { // Hourly
                for ((marker, weather) in markers.zip(weatherOnMap)) {
                    if (timeIndex != null) {
                        marker.title = weather.hourly.data[timeIndex].temperature.toString()
                    } else {
                        marker.title = "Error"
                    }
                }
            }
            "1d", "2d", "3d" -> { // Daily
                for ((marker, weather) in markers.zip(weatherOnMap)) {
                    if (timeIndex != null) {
                        marker.title = weather.daily.data[timeIndex].temperatureHigh.toString()
                    } else {
                        marker.title = "Error"
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}