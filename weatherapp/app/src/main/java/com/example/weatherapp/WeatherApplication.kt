package com.example.weatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class that enables Hilt dependency injection
 * throughout the app lifecycle
 */
@HiltAndroidApp
class WeatherApplication : Application()
