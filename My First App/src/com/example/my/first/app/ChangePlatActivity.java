package com.example.my.first.app;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(9)
public class ChangePlatActivity extends Activity {

	private DataService dataService;
	private MyConnection myConnection;
	
	private Cursor sensorCursor;
	private Cursor platformCursor;
	
	private long platformId;
	private long currentSensorId;
	private TextView viewId;
	private TextView viewMobileNo;
	private EditText editPeriod;
	private EditText editLongitude;
	private EditText editLatitude;
	private EditText editElevation;
	private EditText editDescription;
	private ExpandableListView list;
	private EditText editSensorLongitude;
	private EditText editSensorLatitude;
	private EditText editSensorElevation;

	private String mobileNo;
	private String platformDescriptionOld;
	private String platformDescriptionNew;
	private int[] platformMetadataOld=new int[4];	//{period,longitude,latitude,elevation}
	private int[] platformMetadataNew=new int[4];
	private static final String[] sensorList = new String[] {"Sensor 1","Sensor 2","Sensor 3", "Sensor 4"};
	private int activeSensor=0;
	private long[] sensorIds=new long[4];
	private int[] sensorMetadataOld=new int[12];
	private int[] sensorMetadataNew=new int[12];
	private ArrayAdapter<String> adapter;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changeplat);
		viewId = (TextView) findViewById(R.id.textView02);
		viewMobileNo = (TextView) findViewById(R.id.textView10);
		editPeriod = (EditText) findViewById(R.id.editText21);
		editLongitude = (EditText) findViewById(R.id.editText31);
		editLatitude = (EditText) findViewById(R.id.editText41);
		editElevation = (EditText) findViewById(R.id.editText51);
		editDescription = (EditText) findViewById(R.id.editText61);
		list = (ExpandableListView) findViewById(R.id.sensorList);
		adapter= new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_single_choice,sensorList); 		
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				getSensorMetadata();
				activeSensor=position;
				setSensorMetadata();
				}
			
		});
		editSensorLongitude= (EditText) findViewById(R.id.editText101);
		editSensorLatitude= (EditText) findViewById(R.id.editText111);
		editSensorElevation= (EditText) findViewById(R.id.editText121);
	}
	
	@Override
	protected void onStart() {
		//getting the platform to work with
		Bundle extras =getIntent().getExtras();
		platformId=extras.getLong("platformId");

		//connecting to dataServicto be displayede
		Intent intent =  new Intent(this, DataService.class);
		bindService(intent, myConnection, this.BIND_AUTO_CREATE);	
		dataService=myConnection.getService();
		
		
		//getting the current Metadata from Database
		platformCursor = dataService.getPlatform(platformId);
		sensorCursor = dataService.getSensorsByPlatformId(platformId);
		
		//saving Metadata locally
		mobileNo=platformCursor.getString(platformCursor.getColumnIndex(DatabaseControl.KEY_MOBILENO));
		platformDescriptionOld=platformDescriptionNew=platformCursor.getString(platformCursor.getColumnIndex(DatabaseControl.KEY_DESCR));
		platformMetadataOld[0]=platformMetadataNew[0]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_PERIOD));	
		platformMetadataOld[1]=platformMetadataNew[1]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_LON));
		platformMetadataOld[2]=platformMetadataNew[2]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_LAT));
		platformMetadataOld[3]=platformMetadataNew[3]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_ELEV));
		
		//prepare the sensors
		for (int i=0;i<4;i++) {
			sensorIds[i]=sensorCursor.getLong(sensorCursor.getColumnIndex(DatabaseControl.KEY_ID));
			sensorMetadataOld[i*3]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFX));
			sensorMetadataOld[i*3+1]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFY));
			sensorMetadataOld[i*3+2]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFZ));
			sensorCursor.moveToNext();
		}
		sensorMetadataNew=Arrays.copyOf(sensorMetadataOld, 12);
		
		//displaying Metadata
		setPlatformMetadata();
		setSensorMetadata();
	}
	
    protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (myConnection.isBound()) {
            unbindService(myConnection);
        }
    }

	protected void onPause() {
		onStop();
	}

	protected void onResume() {
		onStart();
	}
	
	public void setPlatformMetadata() {
		viewId.setText(Long.toString(platformId));
		viewMobileNo.setText(mobileNo);
		editDescription.setText(platformDescriptionNew);
		editPeriod.setText(platformMetadataNew[0]);
		editLongitude.setText(platformMetadataNew[1]);
		editLatitude.setText(platformMetadataNew[2]);
		editElevation.setText(platformMetadataNew[3]);
	}
	
	public void getPlatformMetadata() {
		platformDescriptionNew=editDescription.getText().toString();
		try {
			platformMetadataNew[0]=Integer.valueOf(editPeriod.getText().toString());
			platformMetadataNew[1]=Integer.valueOf(editLongitude.getText().toString());
			platformMetadataNew[2]=Integer.valueOf(editLatitude.getText().toString());
			platformMetadataNew[3]=Integer.valueOf(editElevation.getText().toString());
		}catch (NumberFormatException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "not a number", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void setSensorMetadata() {
		editSensorLongitude.setText(sensorMetadataNew[activeSensor*3]);
		editSensorLatitude.setText(sensorMetadataNew[activeSensor*3+1]);
		editSensorElevation.setText(sensorMetadataNew[activeSensor*3+2]);
	}
	
	public void getSensorMetadata(){
		try {
			sensorMetadataNew[activeSensor*3]=Integer.valueOf(editSensorLongitude.getText().toString());
			sensorMetadataNew[activeSensor*3+1]=Integer.valueOf(editSensorLatitude.getText().toString());
			sensorMetadataNew[activeSensor*3+2]=Integer.valueOf(editSensorElevation.getText().toString());
		}catch (NumberFormatException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "not a number", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	//Button responses
	public void dontsave(View view) {
		
	}
	
	public void save(View view){
		dataService.updatePlatform((int) platformId, platformMetadataNew[1], platformMetadataNew[2], platformMetadataNew[3],
				platformMetadataNew[0], mobileNo, platformDescriptionNew); 
		for (int i=0;i<4;i++) {
			dataService.updateSensor((int) sensorIds[i], sensorMetadataNew[i*3], sensorMetadataNew[i*3+1], sensorMetadataNew[i*3+2],(int) platformId);
		}
		
		//evntl check whether successfull, else try to re-update with old data to keep database consistent
		finish();
	}
}
