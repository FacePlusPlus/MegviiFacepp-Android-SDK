package com.facepp.library.util;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorEventUtil implements SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mSensor;
	
	public int orientation = 0;

	public SensorEventUtil(Activity activity) {
		mSensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
		// 参数三，检测的精准度
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor == null) {
			return;
		}

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			
			if(x >= 9)
				orientation = 1;
			else if(x <= -9)
				orientation = 2;
			else if(y <= -9)
				orientation = 3;
			else
				orientation = 0;
		}
	}
}
