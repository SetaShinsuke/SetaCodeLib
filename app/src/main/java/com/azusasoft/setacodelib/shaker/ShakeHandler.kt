package com.seta.shaker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Vibrator
import android.util.Log
import kotlin.properties.Delegates

/**
 * Created by SETA_WORK on 2017/9/14.
 * 监听手机晃动事件
 * [UPDATE_INTERVAL_TIME] 两次检测的时间间隔
 * [SPEED_THRESHOLD] 用于控制敏感度
 * [registerSensor] 注册监听
 * [unRegisterSensor] 反注册监听
 *
 * 需要震动权限
 * ```xml
 * <uses-permission android:name="android.permission.VIBRATE"/>
 * ```
 */
class ShakeHandler(mContext: Context, val shakeListener: (() -> Unit)) : SensorEventListener {
    //两次检测的时间间隔
    val UPDATE_INTERVAL_TIME = 100
    //加速变化阈值，当晃动达到这值后产生作用
    val SPEED_THRESHOLD = 5000

    var mSensorManager by Delegates.notNull<SensorManager>()
    var mVibrator by Delegates.notNull<Vibrator>()

    var lastUpdateTime: Long = 0
    var lastX: Float = 0f
    var lastY: Float = 0f
    var lastZ: Float = 0f

    init {
        mSensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mVibrator = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        val currentUpdateTime = System.currentTimeMillis()
        val timeInterval = currentUpdateTime - lastUpdateTime
        if (timeInterval < UPDATE_INTERVAL_TIME) {
            return
        }
        lastUpdateTime = currentUpdateTime
        val values: FloatArray = event.values
        //获得x,y,z加速度
        val x = values[0]
        val y = values[1]
        val z = values[2]
        //获得x,y,z加速度的变化值
        val deltaX = x - lastX
        val deltaY = x - lastY
        val deltaZ = z - lastZ
        //将现在的坐标变成last坐标
        lastX = x
        lastY = y
        lastZ = z

        Log.d(javaClass.simpleName, "onSensorChanged, values : ${values.toList().map { it.toString() + ", " }}")
        val speed: Double = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / timeInterval * 10000
        if (speed > SPEED_THRESHOLD) {
            mVibrator.vibrate(300)
            shakeListener()
        }
    }


    fun registerSensor() = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
        mSensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unRegisterSensor() {
        lastX = 0f
        lastY = 0f
        lastZ = 0f
        mSensorManager.unregisterListener(this)
    }
}