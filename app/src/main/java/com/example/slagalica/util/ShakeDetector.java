package com.example.slagalica.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7f;
    private static final int SHAKE_SLOP_TIME_MS = 1000;

    private long lastShakeTime = 0;

    public interface OnShakeListener {
        void onShake();
    }

    private final OnShakeListener listener;

    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            long now = System.currentTimeMillis();
            if (lastShakeTime + SHAKE_SLOP_TIME_MS > now) {
                return;
            }
            lastShakeTime = now;
            listener.onShake();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}