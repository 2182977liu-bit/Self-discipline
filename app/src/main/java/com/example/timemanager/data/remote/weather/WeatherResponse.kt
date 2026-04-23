package com.example.timemanager.data.remote.weather

import com.google.gson.annotations.SerializedName

/**
 * 天气响应（使用免费 Open-Meteo API，无需 key）
 */
data class WeatherResponse(
    @SerializedName("current")
    val current: CurrentWeather? = null
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Double = 0.0,
    @SerializedName("relative_humidity_2m")
    val humidity: Int = 0,
    @SerializedName("weather_code")
    val weatherCode: Int = 0,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double = 0.0,
    @SerializedName("is_day")
    val isDay: Int = 1
) {
    fun getDescription(): String {
        return when (weatherCode) {
            0 -> "晴朗"
            1, 2, 3 -> "多云"
            45, 48 -> "雾"
            51, 53, 55 -> "毛毛雨"
            61, 63, 65 -> "小雨"
            71, 73, 75 -> "小雪"
            80, 81, 82 -> "阵雨"
            95 -> "雷阵雨"
            96, 99 -> "雷阵雨伴冰雹"
            else -> "未知"
        }
    }

    fun getExerciseSuggestion(): String {
        return when (weatherCode) {
            0, 1, 2 -> "天气不错，适合户外运动"
            3, 45, 48 -> "天气一般，建议室内运动"
            51, 53, 55, 61, 63, 65 -> "有雨，建议室内运动"
            71, 73, 75 -> "有雪，注意保暖，建议室内运动"
            80, 81, 82, 95, 96, 99 -> "恶劣天气，建议室内运动"
            else -> "建议室内运动"
        }
    }
}
