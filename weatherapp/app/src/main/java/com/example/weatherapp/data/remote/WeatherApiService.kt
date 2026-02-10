package com.example.weatherapp.data.remote

import com.example.weatherapp.data.model.WeatherResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for OpenWeatherMap API
 * Supports both Coroutines and RxJava
 */
interface WeatherApiService {
    
    /**
     * Get weather by city name using Coroutines
     * @param cityName Name of the city
     * @param apiKey API key for OpenWeatherMap
     * @param units Units of measurement (metric/imperial)
     * @return Weather response
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Response<WeatherResponse>
    
    /**
     * Get weather by coordinates using Coroutines
     * @param latitude Latitude
     * @param longitude Longitude
     * @param apiKey API key for OpenWeatherMap
     * @param units Units of measurement (metric/imperial)
     * @return Weather response
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Response<WeatherResponse>
    
    /**
     * Get weather by city name using RxJava
     * @param cityName Name of the city
     * @param apiKey API key for OpenWeatherMap
     * @param units Units of measurement (metric/imperial)
     * @return Single with weather response
     */
    @GET("data/2.5/weather")
    fun getWeatherByCityRx(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Single<Response<WeatherResponse>>
    
    /**
     * Get weather by coordinates using RxJava
     * @param latitude Latitude
     * @param longitude Longitude
     * @param apiKey API key for OpenWeatherMap
     * @param units Units of measurement (metric/imperial)
     * @return Single with weather response
     */
    @GET("data/2.5/weather")
    fun getWeatherByCoordinatesRx(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Single<Response<WeatherResponse>>
}
