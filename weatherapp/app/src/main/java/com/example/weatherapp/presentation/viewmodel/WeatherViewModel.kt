package com.example.weatherapp.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.util.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for weather screen
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val error: String? = null,
    val lastSearchedCity: String? = null
)

/**
 * ViewModel for weather screen following MVVM architecture pattern
 * Manages UI state and business logic
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    init {
        // Requirement: if user has granted location, retrieve weather by location by default;
        // otherwise auto-load last searched city.
        if (hasLocationPermission()) {
            fetchWeatherByLocation()
        } else {
            loadLastSearchedCity()
        }
    }

    /**
     * Load the last searched city and fetch its weather (used when location is not available or denied).
     */
    private fun loadLastSearchedCity() {
        viewModelScope.launch {
            val lastCity = repository.getLastSearchedCity()
            if (lastCity != null) {
                _uiState.value = _uiState.value.copy(lastSearchedCity = lastCity)
                searchWeatherByCity(lastCity)
            }
        }
    }

    /**
     * Search weather by city name
     * @param cityName Name of the city to search
     */
    fun searchWeatherByCity(cityName: String) {
        if (cityName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a city name"
            )
            return
        }

        viewModelScope.launch {
            repository.getWeatherByCity(cityName.trim()).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = WeatherUiState(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _uiState.value = WeatherUiState(
                            isLoading = false,
                            weatherData = resource.data,
                            error = null,
                            lastSearchedCity = cityName
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = WeatherUiState(
                            isLoading = false,
                            error = resource.message ?: "An unexpected error occurred"
                        )
                    }
                }
            }
        }
    }

    /**
     * Fetch weather using device location
     * Checks for location permission before proceeding
     */
    fun fetchWeatherByLocation() {
        // Check if location permissions are granted
        if (!hasLocationPermission()) {
            _uiState.value = _uiState.value.copy(
                error = "Location permission not granted"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        try {
            // Get current location
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    fetchWeatherByCoordinates(location.latitude, location.longitude)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Unable to get current location. Please try again."
                    )
                }
            }.addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get location: ${exception.message}"
                )
            }
        } catch (e: SecurityException) {
            // This shouldn't happen as we check permissions first, but handle it defensively
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Location permission denied"
            )
        }
    }

    /**
     * Fetch weather by coordinates
     */
    private fun fetchWeatherByCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.getWeatherByCoordinates(latitude, longitude).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _uiState.value = WeatherUiState(
                            isLoading = false,
                            weatherData = resource.data,
                            error = null,
                            lastSearchedCity = resource.data?.cityName
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = WeatherUiState(
                            isLoading = false,
                            error = resource.message ?: "An unexpected error occurred"
                        )
                    }
                }
            }
        }
    }

    /**
     * Check if location permission is granted
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
