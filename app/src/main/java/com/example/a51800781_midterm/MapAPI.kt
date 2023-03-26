package com.example.a51800781_midterm

import android.content.pm.PackageManager
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap


data class GeoCode(
    val results: List<Result>,
    val status: String
)

data class Direction(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)

data class Result(
    val address_components: List<AddressComponent>,
    val formatted_address: String,
    val geometry: Geometry,
    val partial_match: Boolean,
    val place_id: String,
    val plus_code: PlusCode,
    val types: List<String>
)

data class AddressComponent(
    val long_name: String,
    val short_name: String,
    val types: List<String>
)

data class Geometry(
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)

data class PlusCode(
    val compound_code: String,
    val global_code: String
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)

data class GeocodedWaypoint(
    val geocoder_status: String,
    val place_id: String,
    val types: List<String>
)

data class Route(
    val bounds: Bounds,
    val copyrights: String,
    val legs: List<Leg>,
    val overview_polyline: OverviewPolyline,
    val summary: String,
    val warnings: List<Any>,
    val waypoint_order: List<Any>
)

data class Bounds(
    val northeast: Location,
    val southwest: Location
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val end_address: String,
    val end_location: Location,
    val start_address: String,
    val start_location: Location,
    val steps: List<Step>,
    val traffic_speed_entry: List<Any>,
    val via_waypoint: List<Any>
)

data class OverviewPolyline(
    val points: String
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)

data class Step(
    val distance: Distance,
    val duration: Duration,
    val end_location: Location,
    val html_instructions: String,
    val maneuver: String,
    val polyline: Polyline,
    val start_location: Location,
    val travel_mode: String
)

data class Polyline(
    val points: String
)

private const val API_KEY = "AIzaSyCOi9tAVIFl2RCoAcaur5_9iIaw50tdpOI"

interface GoogleMapAPI {
    @GET("/maps/api/directions/json")
    suspend fun getDirection(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("sensor") sensor: Boolean = false,
        @Query("key") key: String = API_KEY
    ): Response<Direction>

    @GET("/maps/api/geocode/json")
    suspend fun getGeoCode(
        @Query("address") address: String,
        @Query("key") key: String = API_KEY
    ): Response<GeoCode>
}

class MapAPI {
    companion object {
        private const val BASE_API = "https://maps.googleapis.com/"
        fun getInstance(): Retrofit {
            return Retrofit.Builder().baseUrl(BASE_API)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }

    }
}