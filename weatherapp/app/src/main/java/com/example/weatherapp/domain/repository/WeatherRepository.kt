package com.example.weatherapp.domain.repository

import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for weather data operations
 * This abstraction allows for easier testing and separation of concerns
 */
interface WeatherRepository {
    
    /**
     * Fetch weather data by city name
     * @param cityName Name of the city
     * @return Flow of Resource containing WeatherResponse
     */
    fun getWeatherByCity(cityName: String): Flow<Resource<WeatherResponse>>
    
    /**
     * Fetch weather data by coordinates
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Flow of Resource containing WeatherResponse
     */
    fun getWeatherByCoordinates(latitude: Double, longitude: Double): Flow<Resource<WeatherResponse>>
    
    /**
     * Save last searched city
     * @param cityName Name of the city to save
     */
    suspend fun saveLastSearchedCity(cityName: String)
    
    /**
     * Get last searched city
     * @return Last searched city name or null
     */
    suspend fun getLastSearchedCity(): String?
}
