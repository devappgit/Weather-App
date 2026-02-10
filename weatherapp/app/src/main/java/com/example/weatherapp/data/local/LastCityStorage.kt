package com.example.weatherapp.data.local

/**
 * Abstraction for persisting the last searched city.
 * Allows testing the repository without a real DataStore.
 */
interface LastCityStorage {

    suspend fun getLastCity(): String?

    suspend fun setLastCity(cityName: String)
}
