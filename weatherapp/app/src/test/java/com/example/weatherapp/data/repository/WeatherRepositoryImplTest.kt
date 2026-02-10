package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.local.LastCityStorage
import com.example.weatherapp.data.model.*
import com.example.weatherapp.data.remote.WeatherApiService
import com.example.weatherapp.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for WeatherRepositoryImpl
 * Tests repository logic and error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryImplTest {

    @Mock
    private lateinit var apiService: WeatherApiService

    @Mock
    private lateinit var lastCityStorage: LastCityStorage

    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `getWeatherByCity returns success when API call succeeds`() = runTest {
        // Given: repository appends ",US" to city name for US search
        val cityName = "New York"
        val query = "$cityName,US"
        val weatherResponse = createMockWeatherResponse(cityName)
        val apiResponse = Response.success(weatherResponse)

        whenever(apiService.getWeatherByCity(query, BuildConfig.WEATHER_API_KEY))
            .thenReturn(apiResponse)

        repository = WeatherRepositoryImpl(apiService, lastCityStorage)

        // When: flow emits Loading then Success; we take the Success
        val result = repository.getWeatherByCity(cityName).drop(1).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(weatherResponse, result.data)
    }

    @Test
    fun `getWeatherByCity returns error when city not found`() = runTest {
        // Given: repository sends "InvalidCity,US"
        val cityName = "InvalidCity"
        val query = "$cityName,US"
        val apiResponse = Response.error<WeatherResponse>(
            404,
            "Not Found".toResponseBody()
        )

        whenever(apiService.getWeatherByCity(query, BuildConfig.WEATHER_API_KEY))
            .thenReturn(apiResponse)

        repository = WeatherRepositoryImpl(apiService, lastCityStorage)

        // When: take the Error emission (after Loading)
        val result = repository.getWeatherByCity(cityName).drop(1).first()

        // Then
        assertTrue(result is Resource.Error)
        assertTrue(result.message?.contains("City not found") == true)
    }

    @Test
    fun `getWeatherByCity returns error when API key is invalid`() = runTest {
        // Given: repository sends "Boston,US"
        val cityName = "Boston"
        val query = "$cityName,US"
        val apiResponse = Response.error<WeatherResponse>(
            401,
            "Unauthorized".toResponseBody()
        )

        whenever(apiService.getWeatherByCity(query, BuildConfig.WEATHER_API_KEY))
            .thenReturn(apiResponse)

        repository = WeatherRepositoryImpl(apiService, lastCityStorage)

        // When: take the Error emission (after Loading)
        val result = repository.getWeatherByCity(cityName).drop(1).first()

        // Then
        assertTrue(result is Resource.Error)
        assertTrue(result.message?.contains("Invalid API key") == true)
    }

    @Test
    fun `getWeatherByCoordinates returns success when API call succeeds`() = runTest {
        // Given
        val latitude = 40.7128
        val longitude = -74.0060
        val weatherResponse = createMockWeatherResponse("New York")
        val apiResponse = Response.success(weatherResponse)
        
        whenever(
            apiService.getWeatherByCoordinates(
                latitude,
                longitude,
                BuildConfig.WEATHER_API_KEY
            )
        ).thenReturn(apiResponse)

        repository = WeatherRepositoryImpl(apiService, lastCityStorage)

        // When: flow emits Loading then Success
        val result = repository.getWeatherByCoordinates(latitude, longitude).drop(1).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(weatherResponse, result.data)
    }

    /**
     * Helper function to create mock weather response
     */
    private fun createMockWeatherResponse(cityName: String): WeatherResponse {
        return WeatherResponse(
            coordinates = Coordinates(longitude = -74.0060, latitude = 40.7128),
            weather = listOf(
                Weather(
                    id = 800,
                    main = "Clear",
                    description = "clear sky",
                    icon = "01d"
                )
            ),
            base = "stations",
            main = Main(
                temperature = 22.0,
                feelsLike = 21.5,
                tempMin = 20.0,
                tempMax = 24.0,
                pressure = 1013,
                humidity = 65
            ),
            visibility = 10000,
            wind = Wind(
                speed = 4.0,
                degrees = 180
            ),
            clouds = Clouds(cloudiness = 10),
            timestamp = 1609459200,
            sys = Sys(
                country = "US",
                sunrise = 1609459200,
                sunset = 1609495200
            ),
            timezone = -18000,
            cityId = 5128581,
            cityName = cityName,
            code = 200
        )
    }
}
