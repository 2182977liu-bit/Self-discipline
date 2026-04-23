package com.example.timemanager.data.remote.weather

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 天气 API（Open-Meteo，免费无需 key）
 */
interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,is_day"
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}
