package com.example.my.first.app;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class InsertNodeActivity extends Activity {

	private EditText editTextShowLocation;
	private LocationManager locManager;
	private LocationListener locListener;
	private Location mobileLocation;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_test);
		editTextShowLocation = (EditText) findViewById(R.id.editTextShowLocation);

	}
	
	/** Gets the current location and update the mobileLocation variable*/
	private void getCurrentLocation() {
		locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locListener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				mobileLocation = location;
			}
		};
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
	}

	public void gpsClick(View view) {
		getCurrentLocation(); // gets the current location and update mobileLocation variable
		
		if (mobileLocation != null) {
			locManager.removeUpdates(locListener); // This needs to stop getting the location data and save the battery power.
			
			String longitude =String.valueOf(mobileLocation.getLongitude());
			int longi=(int) Math.round(mobileLocation.getLongitude()*1000000); //sufficient accuracy, microdegrees
			String latitude = String.valueOf(mobileLocation.getLatitude());
			String altitiude = "Altitiude: " + mobileLocation.getAltitude();
			String accuracy = "Accuracy: " + mobileLocation.getAccuracy();
			String time = "Time: " + mobileLocation.getTime();

			editTextShowLocation.setText(longitude + "\n" + latitude + "\n"
					+ altitiude + "\n" + accuracy + "\n" + time);
			
			saveGMaps("https://maps.google.com/maps?q="+latitude+","+longitude +"\n");
			//have to save this on sdcard
		} else {
			Toast.makeText(this, "not determined", Toast.LENGTH_LONG).show();
		}
	}
	

public void saveGMaps(String gmaps) {
	BufferedWriter out;
	try {
		//MODE_PRIVATE creates file, if it does not exist now
		out = new BufferedWriter(new FileWriter(getExternalFilesDir(null)+"/gpsdata.txt",true));
		out.write(gmaps);
		out.close();	
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	} catch (IOException e) {
		System.out.println("File/Write problems");
	}
}

}
