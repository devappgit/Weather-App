package com.example.weatherapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_preferences")

/**
 * DataStore-backed implementation of LastCityStorage for production.
 */
@Singleton
class DataStoreLastCityStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : LastCityStorage {

    companion object {
        private val LAST_SEARCHED_CITY_KEY = stringPreferencesKey("last_searched_city")
    }

    override suspend fun getLastCity(): String? {
        return context.dataStore.data
            .map { it[LAST_SEARCHED_CITY_KEY] }
            .first()
    }

    override suspend fun setLastCity(cityName: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEARCHED_CITY_KEY] = cityName
        }
    }
}
