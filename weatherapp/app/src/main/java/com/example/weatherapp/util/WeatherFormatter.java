package com.example.weatherapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Java utility class for weather-related formatting
 * Demonstrates Java/Kotlin interoperability as required
 * This class can be called from Kotlin code seamlessly
 */
public class WeatherFormatter {

    /**
     * Format temperature with proper unit
     * @param temperature Temperature value
     * @param isCelsius True for Celsius, false for Fahrenheit
     * @return Formatted temperature string
     */
    public static String formatTemperature(double temperature, boolean isCelsius) {
        int roundedTemp = (int) Math.round(temperature);
        return roundedTemp + (isCelsius ? "°C" : "°F");
    }

    /**
     * Convert Kelvin to Celsius
     * @param kelvin Temperature in Kelvin
     * @return Temperature in Celsius
     */
    public static double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    /**
     * Convert Kelvin to Fahrenheit
     * @param kelvin Temperature in Kelvin
     * @return Temperature in Fahrenheit
     */
    public static double kelvinToFahrenheit(double kelvin) {
        return (kelvin - 273.15) * 9/5 + 32;
    }

    /**
     * Format Unix timestamp to readable date
     * @param timestamp Unix timestamp in seconds
     * @return Formatted date string
     */
    public static String formatDate(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return format.format(date);
    }

    /**
     * Format Unix timestamp to time
     * @param timestamp Unix timestamp in seconds
     * @return Formatted time string (HH:mm)
     */
    public static String formatTime(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(date);
    }

    /**
     * Get wind direction from degrees
     * @param degrees Wind direction in degrees
     * @return Cardinal direction (N, NE, E, SE, S, SW, W, NW)
     */
    public static String getWindDirection(int degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round((degrees % 360) / 45.0) % 8;
        return directions[index];
    }

    /**
     * Get weather condition description
     * @param weatherId OpenWeatherMap weather condition ID
     * @return Human-readable description
     */
    public static String getWeatherConditionDescription(int weatherId) {
        if (weatherId >= 200 && weatherId < 300) {
            return "Thunderstorm";
        } else if (weatherId >= 300 && weatherId < 400) {
            return "Drizzle";
        } else if (weatherId >= 500 && weatherId < 600) {
            return "Rain";
        } else if (weatherId >= 600 && weatherId < 700) {
            return "Snow";
        } else if (weatherId >= 700 && weatherId < 800) {
            return "Atmosphere";
        } else if (weatherId == 800) {
            return "Clear Sky";
        } else if (weatherId > 800 && weatherId < 900) {
            return "Cloudy";
        }
        return "Unknown";
    }

    /**
     * Calculate heat index (feels like temperature)
     * Simplified formula for demonstration
     * @param temperature Temperature in Celsius
     * @param humidity Relative humidity percentage
     * @return Heat index in Celsius
     */
    public static double calculateHeatIndex(double temperature, int humidity) {
        // Convert to Fahrenheit for calculation
        double tempF = temperature * 9/5 + 32;
        double hi = -42.379 + 2.04901523 * tempF + 10.14333127 * humidity
                - 0.22475541 * tempF * humidity - 0.00683783 * tempF * tempF
                - 0.05481717 * humidity * humidity + 0.00122874 * tempF * tempF * humidity
                + 0.00085282 * tempF * humidity * humidity - 0.00000199 * tempF * tempF * humidity * humidity;
        
        // Convert back to Celsius
        return (hi - 32) * 5/9;
    }

    /**
     * Format visibility in kilometers
     * @param visibilityMeters Visibility in meters
     * @return Formatted visibility string
     */
    public static String formatVisibility(int visibilityMeters) {
        double km = visibilityMeters / 1000.0;
        if (km >= 10) {
            return String.format(Locale.getDefault(), "%.0f km", km);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", km);
        }
    }

    /**
     * Get UV index description
     * @param uvIndex UV index value
     * @return Description (Low, Moderate, High, Very High, Extreme)
     */
    public static String getUVIndexDescription(double uvIndex) {
        if (uvIndex < 3) {
            return "Low";
        } else if (uvIndex < 6) {
            return "Moderate";
        } else if (uvIndex < 8) {
            return "High";
        } else if (uvIndex < 11) {
            return "Very High";
        } else {
            return "Extreme";
        }
    }
}
