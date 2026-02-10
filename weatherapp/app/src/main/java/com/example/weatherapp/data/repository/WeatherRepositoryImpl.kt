package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.local.LastCityStorage
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.remote.WeatherApiService
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WeatherRepository using Retrofit and LastCityStorage.
 * Handles all weather data operations including network calls and local storage.
 */
@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService,
    private val lastCityStorage: LastCityStorage
) : WeatherRepository {

    /**
     * Fetch weather by city name with proper error handling.
     * Appends ",US" to the query per requirement (US city search); API returns imperial units.
     */
    override fun getWeatherByCity(cityName: String): Flow<Resource<WeatherResponse>> = flow {
        try {
            emit(Resource.Loading())
            // OpenWeatherMap: q=city name, country code (e.g. "New York,US")
            val query = cityName.trim().let { if (it.contains(',')) it else "$it,US" }

            val response = apiService.getWeatherByCity(
                cityName = query,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
                // Save the city for auto-load on next launch
                lastCityStorage.setLastCity(cityName.trim())
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "City not found. Please check the city name."
                    401 -> "Invalid API key. Please check your configuration."
                    429 -> "Too many requests. Please try again later."
                    else -> "Failed to fetch weather data: ${response.message()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            // Handle HTTP errors
            emit(Resource.Error("Network error: ${e.localizedMessage ?: "Unknown error"}"))
        } catch (e: IOException) {
            // Handle network connectivity errors
            emit(Resource.Error("Network connection error. Please check your internet connection."))
        } catch (e: Exception) {
            // Handle any other unexpected errors
            emit(Resource.Error("Unexpected error: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    /**
     * Fetch weather by coordinates with proper error handling
     */
    override fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Flow<Resource<WeatherResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getWeatherByCoordinates(
                latitude = latitude,
                longitude = longitude,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                val weatherResponse = response.body()!!
                emit(Resource.Success(weatherResponse))
                lastCityStorage.setLastCity(weatherResponse.cityName)
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Location not found."
                    401 -> "Invalid API key. Please check your configuration."
                    429 -> "Too many requests. Please try again later."
                    else -> "Failed to fetch weather data: ${response.message()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage ?: "Unknown error"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection error. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    override suspend fun saveLastSearchedCity(cityName: String) {
        lastCityStorage.setLastCity(cityName)
    }

    override suspend fun getLastSearchedCity(): String? {
        return lastCityStorage.getLastCity()
    }
}
