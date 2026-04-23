package com.example.timemanager.service.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 步数检测（使用加速度计传感器）
 *
 * 低功耗实现：仅在需要时注册监听
 */
@Singleton
class StepTracker @Inject constructor(
    private val context: Context
) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    private var isListening = false
    private var stepsAtStart = 0

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val totalSteps = event.values[0].toInt()
            if (stepsAtStart == 0) stepsAtStart = totalSteps
            _todaySteps.value = totalSteps - stepsAtStart
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * 开始监听步数
     */
    fun startTracking() {
        if (isListening || stepSensor == null) return
        isListening = true
        stepsAtStart = 0
        sensorManager.registerListener(sensorListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
    }

    /**
     * 停止监听步数
     */
    fun stopTracking() {
        if (!isListening) return
        isListening = false
        sensorManager.unregisterListener(sensorListener)
    }

    /**
     * 是否有步数传感器
     */
    fun hasStepSensor(): Boolean = stepSensor != null
}
