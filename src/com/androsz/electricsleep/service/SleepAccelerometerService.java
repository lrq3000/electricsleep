package com.androsz.electricsleep.service;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.ui.SleepActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class SleepAccelerometerService extends Service implements
		SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;

	private PowerManager powerManager;
	private WakeLock partialWakeLock;

	private NotificationManager notificationManager;
	private Notification notification;

	public void onCreate() {
		super.onCreate();
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		partialWakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,
				"Electric Sleep Accelerometer WakeLock");

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		partialWakeLock.acquire();

		// Obtain a reference to system-wide sensor event manager.
		sensorManager = (SensorManager) getApplicationContext()
				.getSystemService(Context.SENSOR_SERVICE);

		// Get the default sensor for accel
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Register for events.
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();

		// Unregister from SensorManager.
		sensorManager.unregisterListener(this);

		partialWakeLock.release();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;

		int icon = R.drawable.icon;
		CharSequence tickerText = event.values[0] + " " + event.values[1] + " "
				+ event.values[2];
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "My notification";
		CharSequence contentText = "Hello World!";
		Intent notificationIntent = new Intent(this, SleepActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		notificationManager.notify(1, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
