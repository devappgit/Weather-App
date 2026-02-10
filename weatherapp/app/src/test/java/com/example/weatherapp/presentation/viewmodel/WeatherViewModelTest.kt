package com.example.weatherapp.presentation.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.data.model.*
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for WeatherViewModel
 * Tests the business logic and state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: WeatherRepository

    @Mock
    private lateinit var context: Context

    private lateinit var viewModel: WeatherViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock context so hasLocationPermission() is false; init then uses loadLastSearchedCity()
        whenever(context.checkSelfPermission(org.mockito.ArgumentMatchers.anyString())).thenReturn(PackageManager.PERMISSION_DENIED)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchWeatherByCity with valid city returns success`() = runTest {
        // Given
        val cityName = "New York"
        val weatherResponse = createMockWeatherResponse(cityName)
        whenever(repository.getWeatherByCity(cityName))
            .thenReturn(flowOf(Resource.Success(weatherResponse)))
        
        viewModel = WeatherViewModel(repository, context)
        advanceUntilIdle()

        // When
        viewModel.searchWeatherByCity(cityName)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(weatherResponse, state.weatherData)
        assertEquals(null, state.error)
        verify(repository).getWeatherByCity(cityName)
    }

    @Test
    fun `searchWeatherByCity with empty city shows error`() = runTest {
        // Given
        viewModel = WeatherViewModel(repository, context)
        advanceUntilIdle()

        // When
        viewModel.searchWeatherByCity("")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Please enter a city name", state.error)
        verify(repository, never()).getWeatherByCity(anyString())
    }

    @Test
    fun `searchWeatherByCity with network error returns error`() = runTest {
        // Given
        val cityName = "Invalid City"
        val errorMessage = "City not found"
        whenever(repository.getWeatherByCity(cityName))
            .thenReturn(flowOf(Resource.Error(errorMessage)))
        
        viewModel = WeatherViewModel(repository, context)
        advanceUntilIdle()

        // When
        viewModel.searchWeatherByCity(cityName)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        assertEquals(null, state.weatherData)
    }

    @Test
    fun `searchWeatherByCity shows loading state`() = runTest {
        // Given
        val cityName = "London"
        whenever(repository.getWeatherByCity(cityName))
            .thenReturn(flowOf(Resource.Loading()))
        
        viewModel = WeatherViewModel(repository, context)
        advanceUntilIdle()

        // When
        viewModel.searchWeatherByCity(cityName)
        testDispatcher.scheduler.runCurrent()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
    }

    @Test
    fun `clearError resets error state`() = runTest {
        // Given
        viewModel = WeatherViewModel(repository, context)
        viewModel.searchWeatherByCity("") // This will set an error
        advanceUntilIdle()

        // When
        viewModel.clearError()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(null, state.error)
    }

    @Test
    fun `loadLastSearchedCity fetches weather for saved city`() = runTest {
        // Given
        val savedCity = "Boston"
        val weatherResponse = createMockWeatherResponse(savedCity)
        
        whenever(repository.getLastSearchedCity()).thenReturn(savedCity)
        whenever(repository.getWeatherByCity(savedCity))
            .thenReturn(flowOf(Resource.Success(weatherResponse)))

        // When
        viewModel = WeatherViewModel(repository, context)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(savedCity, state.lastSearchedCity)
        assertEquals(weatherResponse, state.weatherData)
        verify(repository).getLastSearchedCity()
        verify(repository).getWeatherByCity(savedCity)
    }

    /**
     * Helper function to create mock weather response
     */
    private fun createMockWeatherResponse(cityName: String): WeatherResponse {
        return WeatherResponse(
            coordinates = Coordinates(longitude = -0.1257, latitude = 51.5085),
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
                temperature = 20.0,
                feelsLike = 19.5,
                tempMin = 18.0,
                tempMax = 22.0,
                pressure = 1013,
                humidity = 60
            ),
            visibility = 10000,
            wind = Wind(
                speed = 3.5,
                degrees = 180
            ),
            clouds = Clouds(cloudiness = 0),
            timestamp = 1609459200,
            sys = Sys(
                country = "US",
                sunrise = 1609459200,
                sunset = 1609495200
            ),
            timezone = 0,
            cityId = 123456,
            cityName = cityName,
            code = 200
        )
    }
}
