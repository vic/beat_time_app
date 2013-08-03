package org.hackunamatata.beat_time_app;

import java.io.IOException;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	private SensorManager sensorManager;
	private Sensor accelSensor;
	private Sensor magnetSensor;
	private Sensor proximitySensor;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);
	    sensorManager.registerListener(orientationListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);
	    sensorManager.registerListener(orientationListener, magnetSensor, SensorManager.SENSOR_DELAY_GAME);
	    sensorManager.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
	}
	
	@Override
	protected void onPause() {
	    sensorManager.unregisterListener(accelListener);
	    sensorManager.unregisterListener(orientationListener);
	    super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClick(View view) {
		System.out.println("CLICKED");
	}
	
	private void onShake() {
		System.out.println("SHAKED");
	}
	
	private void onNear() {
		System.out.println("NEAR");
	}
	
	private void onFar() {
		System.out.println("FAR");
	}
	
	private  SensorEventListener proximityListener = new SensorEventListener() {		
		@Override
		public void onSensorChanged(SensorEvent event) {
			float proximity = event.values[0];
			if (proximity == 0f) {
				onNear();
			} else if (proximity == event.sensor.getMaximumRange()) {
				onFar();
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private  SensorEventListener orientationListener = new SensorEventListener() {

	    private float[] mLastAccelerometer = new float[3];
	    private float[] mLastMagnetometer = new float[3];
	    private boolean mLastAccelerometerSet = false;
	    private boolean mLastMagnetometerSet = false;

	    private float[] mR = new float[9];
	    private float[] mOrientation = new float[3];
	    
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor == accelSensor) {
	            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
	            mLastAccelerometerSet = true;
	        } else if (event.sensor == magnetSensor) {
	            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
	            mLastMagnetometerSet = true;
	        }
	        if (mLastAccelerometerSet && mLastMagnetometerSet) {
	            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
	            SensorManager.getOrientation(mR, mOrientation);
	            //System.out.println("OrientationTestActivity: " + String.format("Orientation: %f, %f, %f",mOrientation[0], mOrientation[1], mOrientation[2]));
	        }
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {			
		}		
	};

	private  SensorEventListener accelListener = new SensorEventListener() {
		
		private float mAccel = 0.00f;
		private float mAccelCurrent = SensorManager.GRAVITY_EARTH;
		private float mAccelLast = SensorManager.GRAVITY_EARTH;
		
		@Override
		public void onSensorChanged(SensorEvent se) {
		  float x = se.values[0];
	      float y = se.values[1];
	      float z = se.values[2];
	      mAccelLast = mAccelCurrent;
	      mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
	      float delta = mAccelCurrent - mAccelLast;
	      mAccel = mAccel * 0.9f + delta; // perform low-cut filter
	      if (mAccel > 7) {
	    	  onShake();
	      }
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {			
		}
	}; 
	
	private class PlayAsyncTask extends AsyncTask<String, Void, Void> implements OnCompletionListener {

		private MediaPlayer player;

		@Override
		protected Void doInBackground(String... params) {
			String track = params[0];
			try {
				playTrack(track);
			} catch (IOException e) {
				
			}
			return null;
		}

		private void playTrack(String track) throws IOException {
			AssetFileDescriptor afd = getAssets().openFd(track);
		    player = new MediaPlayer();
		    player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
		    player.setOnCompletionListener(this);
		    player.prepare();
		    player.start();
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			mp.release();
		}
	}
}
