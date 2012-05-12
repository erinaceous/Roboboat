package uk.ac.aber.dcs.roboboat;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

public class PhoneSensors extends BroadcastReceiver
implements SensorEventListener, LocationListener {
	private final Context context;
	private final SensorManager sensors;
	private final Sensor orient;
	private final Sensor compass;
	private final Sensor temp;
	private final LocationManager gps;
	private final String gpsProvider;
	private final IntentFilter batteryFilter;
	
	public Location location;
	public float[] orientation = new float[3];
	public float[] apr = new float[3];
	private float[] gravity = new float[3];
	private float[] geomag = new float[3];
	public float battery;
	public float[] temperature;
	
	/* amount of time spent sleeping between sensor updates */
	public final static int UPDATE_RATE = 1000;
	
	public PhoneSensors(Context c) {
		context = c;
		
		sensors =
			(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		
		/*
		 * SENSOR TYPES (from API docs):
		 * Sensor.TYPE_ACCELEROMETER = 1;
		 * Sensor.TYPE_COMPASS = 2;
		 * Sensor.TYPE_ORIENTATION = 3;
		 * Sensor.TYPE_GYROSCOPE = 4;
		 * Sensor.TYPE_LIGHT = 5;
		 * Sensor.TYPE_PRESSURE = 6;
		 * Sensor.TYPE_TEMPERATURE = 7;
		 * Sensor.TYPE_PROXIMITY = 8;
		 * Sensor.TYPE_GRAVITY = 9;
		 * Sensor.TYPE_LINEAR_ACCELERATION = 10;
		 * Sensor.TYPE_ROTATION_VECTOR = 11;
		 */
		List<Sensor> availableSensors = sensors.getSensorList(Sensor.TYPE_ALL);
		for(Sensor s : availableSensors) {
			Log.d("boatSensors",s.getType()+"\t"+s.getName());
		}
		
		gps =
			(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		gpsProvider = gps.getBestProvider(criteria, true);
		
		orient =
			sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		compass =
			sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		temp =
			sensors.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

		/* Register updates for the compass (magnetometer) and accelerometer */
		sensors.registerListener(this, orient, SensorManager.SENSOR_DELAY_FASTEST);
		sensors.registerListener(this, compass, SensorManager.SENSOR_DELAY_FASTEST);
		sensors.registerListener(this, temp, SensorManager.SENSOR_DELAY_NORMAL);
		
		/* Register updates for the GPS */
		gps.requestLocationUpdates(gpsProvider, UPDATE_RATE, 0, this);
		location = gps.getLastKnownLocation(gpsProvider);
		
		/* Register the intent filter for getting battery level */
		batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		context.registerReceiver(this, batteryFilter);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] inR = new float[16];
		float[] I = new float[16];
		float[] orientVals = new float[3];
		float[] apr = new float[3];
		
		double azimuth = 0, pitch = 0, roll = 0;
		
		switch(event.sensor.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD:
			geomag = event.values.clone();
			break;
		case Sensor.TYPE_ACCELEROMETER:
			gravity = event.values.clone();
			break;
		}
		
		if(gravity != null && geomag != null) {
			boolean success =
				SensorManager.getRotationMatrix(inR, I, gravity, geomag);
			if(success) {
				SensorManager.getOrientation(inR, orientVals);
				for(int i=0; i<3; i++) {
					apr[i] = (float) Math.toDegrees(orientVals[i]);
				}
				this.orientation = orientVals;
				this.apr = apr;
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		battery = ((float)level/(float)scale)*100;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do noooothing.
	}

	@Override
	public void onProviderDisabled(String provider) {
		// deliberately do nothing
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// deliberately do nothing
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// deliberately do nothing
		
	}
}
