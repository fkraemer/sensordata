package com.example.my.first.app;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

@TargetApi(9)
public class ChangePlatActivity extends Activity {

	class setUpMetadataTask extends AsyncTask<Void, Void, Void> {
	@Override
		protected Void doInBackground(Void... arg0) {
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			dataService=mConnect.getService();
			 // case NEW NODE
			//done in postexecute
			return null;
		}
	
		//option to update progressbar via this thread
		@Override
		protected void onPostExecute(Void result) {
			//save the Metadata locally: get from existing platform or fill with default values
			if (!newPlat)
			{
		
				//getting the current Metadata from Database
				platformCursor = dataService.getPlatform(platformId);
				sensorCursor = dataService.getSensorsByPlatformId(platformId);
				//bind cursor lifecycle to activity
				startManagingCursor(platformCursor);
				startManagingCursor(sensorCursor);
				//saving Metadata locally, changed data gets written into the newMetadata fields
				mobileNo=platformCursor.getString(platformCursor.getColumnIndex(DatabaseControl.KEY_MOBILENO));
				isReceiving=dataService.platformIdIsBound((int)platformId);	//prepare the toggle-button
				
				platformDescriptionOld=platformDescriptionNew=platformCursor.getString(platformCursor.getColumnIndex(DatabaseControl.KEY_DESCR));
				platformMetadataOld[0]=platformMetadataNew[0]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_PERIOD));	
				platformMetadataOld[1]=platformMetadataNew[1]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_LON));
				platformMetadataOld[2]=platformMetadataNew[2]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_LAT));
		
				//prepare the sensors
				for (int i=0;i<4;i++) {	
					if (sensorCursor.getCount()<sensorCursor.getPosition()) {		//catches rare wrong cursor indexes
						sensorCursor.moveToFirst();
					}
					sensorIds[i]=sensorCursor.getLong(sensorCursor.getColumnIndex(DatabaseControl.KEY_ID));
					sensorMetadataOld[i*3]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFX));
					sensorMetadataOld[i*3+1]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFY));
					sensorMetadataOld[i*3+2]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFZ));
					sensorCursor.moveToNext();
				}
				sensorMetadataNew=Arrays.copyOf(sensorMetadataOld, 12);
			} else	if (newPlat){
				//set flag for postexecute
				showDialog(DIALOG_NEW_NODE);	//getting mobile
				//inserting defaults
				
				isReceiving=true;	//this is set upon saving
				
				platformDescriptionOld=platformDescriptionNew="New Node";
				platformMetadataOld[0]=platformMetadataNew[0]=30;	
				platformMetadataOld[1]=platformMetadataNew[1]=0;
				platformMetadataOld[2]=platformMetadataNew[2]=0;
		
				//prepare the sensors
				for (int i=0;i<4;i++) {	
					
					sensorMetadataOld[i*3]=0;
					sensorMetadataOld[i*3+1]=0;
					sensorMetadataOld[i*3+2]=0;
			}
				sensorMetadataNew=Arrays.copyOf(sensorMetadataOld, 12);
			}
			//setting up togglebutton, done here to be in UI thread
			if (isReceiving) {
				receivesButton.setChecked(true);
				receivesButton.setClickable(false);
			} 
			//-------------------------------set listener -------------------------------------
			receivesButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						dataService.bindNumber(mobileNo,(int) platformId);
						receivesButton.setClickable(false);
					}
				}
			});
			//-----------------------------listener end---------------------------------------
	
			//sets all the textviews
			setPlatformMetadata();
			setSensorMetadata();
			
		
		}	       
    }
	
	private static final int DIALOG_BIND_ID = 4;
	private static final int DIALOG_FAILED_ID = 3;

	private static final int DIALOG_NEW_NODE = 7;
	private static final int DIALOG_NO_PROVIDER = 5;
	
	private static final int DIALOG_ON_WORK = 6;
	private static final int DIALOG_PROMPT_ID = 0;
	private static final int DIALOG_SAVED_ID = 1;
	private static final int DIALOG_SUCCESS_ID = 2;
	private static final int LOC_DISTANCE = 0;
	private static final int LOC_INTERVAL = 5000;
	private static final String[] sensorList = new String[] 
			{"Sensor 1","Sensor 2","Sensor 3", "Sensor 4"};
	private int activeSensor=0;
	private ArrayAdapter<String> adapter;
	private int currentSensorId;
	private DataService dataService;
	private OnClickListener deleteOnClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			 if (dataService.deletePlatform((int) platformId))  {
				 //should delete the whole cascade
				 dataService.deleteboundNumber(mobileNo, platformId);	//deletes flag if it was receiving
				 showDialog(DIALOG_SUCCESS_ID); //this finishes then
			 } else {
				 showDialog(DIALOG_FAILED_ID);
			 }
		}
	};
	private EditText editDescription;
	

	private EditText editLatitude;
	private EditText editLongitude;
	private EditText editPeriod;
	private EditText editSensorElevation;
	private EditText editSensorLatitude;
	private EditText editSensorLongitude;
	private OnClickListener insertOnClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			platformId = dataService.insertPlatform(platformMetadataNew[1], platformMetadataNew[2],
					platformMetadataNew[0], mobileNo, platformDescriptionNew);
			ChangePlatActivity.this.finish();
		}
	};
	//-----------------------------------------------------------------------------------------------

	private boolean isReceiving=false;
	private final CountDownLatch latch = new CountDownLatch(1);
	private ListView list;
	private LocationListener locListener;
	private MyConnection mConnect = new MyConnection(latch);
	private LocationManager mLocationManager;
	private Location mobileLocation;
	private String mobileNo;
	
	private boolean newPlat=false;
	private Cursor platformCursor;
	
	private String platformDescriptionNew;
	private String platformDescriptionOld;
	private long platformId;
	private int[] platformMetadataNew=new int[3];
	private int[] platformMetadataOld=new int[3];	//{period,longitude,latitude,elevation}
	private ToggleButton receivesButton;
	private Cursor sensorCursor;

	private long[] sensorIds=new long[4];
	private int[] sensorMetadataNew=new int[12];
	private int[] sensorMetadataOld=new int[12];
	//------------------------------------------------------------------------------------------------
	//Listener for Dialogs
	private OnClickListener updateOnClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			//updating the platformid
			dataService.updatePlatform((int) platformId, platformMetadataNew[1], platformMetadataNew[2],
					platformMetadataNew[0], mobileNo, platformDescriptionNew); 
			//updating the sensors
			for (int i=0;i<4;i++) {
				dataService.updateSensor((int) sensorIds[i], sensorMetadataNew[i*3], sensorMetadataNew[i*3+1], sensorMetadataNew[i*3+2],(int) platformId);
			}
			ChangePlatActivity.this.finish();
		}
	};

	
	
	private TextView viewId;
	
	private TextView viewMobileNo;
	
    private boolean wasAquired=false; 	//sets up, whether location was already aquired once
		
	
	
    private boolean wasSetOnce=false;

	
	public void dontsave(View view) {
		finish();
	}
	
	public void getGPS(View view) {
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			showDialog(DIALOG_NO_PROVIDER);
		} else {
			Toast.makeText(this, "getting it", Toast.LENGTH_SHORT).show();
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOC_INTERVAL,LOC_DISTANCE,locListener);
		}
	}
	
	public void getPlatformMetadata() {
		platformDescriptionNew=editDescription.getText().toString();
		//the value whether it receives the next sms (togglebutton) is handled seperately
		try {
			platformMetadataNew[0]=Integer.valueOf(editPeriod.getText().toString());
			platformMetadataNew[1]=Integer.valueOf(editLongitude.getText().toString());
			platformMetadataNew[2]=Integer.valueOf(editLatitude.getText().toString());
		}catch (NumberFormatException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "not a number", Toast.LENGTH_SHORT).show();
		}
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changeplat);
		// setting up the platform text fields
		viewId = (TextView) findViewById(R.id.textView02);
		viewMobileNo = (TextView) findViewById(R.id.textView10);
		editPeriod = (EditText) findViewById(R.id.editText21);
		editLongitude = (EditText) findViewById(R.id.editText31);
		editLatitude = (EditText) findViewById(R.id.editText41);
		editDescription = (EditText) findViewById(R.id.editText61);
		receivesButton = (ToggleButton) findViewById(R.id.receivesButton);
		//setting up the sensor-choose-from-list and editTexts
		list = (ListView) findViewById(R.id.sensorList);
		adapter= new  ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,sensorList);
		list.setAdapter(adapter);
		list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		list.setItemChecked(activeSensor, true);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				getSensorMetadata();	//save current data in fiels
				activeSensor=position;	//set next sensor
				setSensorMetadata();	//write the metadata of the next sensor into the text-fields
				}
		});
		editSensorLongitude= (EditText) findViewById(R.id.editText101);
		editSensorLatitude= (EditText) findViewById(R.id.editText111);
		editSensorElevation= (EditText) findViewById(R.id.editText121);
		

		//getting the platform to work on
		Bundle extras =getIntent().getExtras();
		platformId=extras.getLong("platformId");
		if (platformId==-1) {
			newPlat=true;		//case creating an all new platform
		}

		//connecting to dataService
		Intent intent =  new Intent(this, DataService.class);
		bindService(intent, mConnect, 0);	
		
	}
	
	
	@Override
	@Deprecated	//new method since API 11
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case (DIALOG_PROMPT_ID):
		builder.setMessage("Update metadata or save as a new platform ?")
			.setCancelable(false)
			.setPositiveButton("Update", updateOnClickListener)
			.setNeutralButton("delete", deleteOnClickListener)
			.setNegativeButton("Insert", insertOnClickListener);
		break;
		case (DIALOG_SAVED_ID):
			builder.setMessage("successful done")
				.setPositiveButton("Ok", new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						ChangePlatActivity.this.finish();
					}
				});
		break;
		 case (DIALOG_SUCCESS_ID): 
			 builder.setMessage("Successful done!")
			 	.setNeutralButton("Ok", new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						ChangePlatActivity.this.finish();
					}
				}); 
		 break;
		 case (DIALOG_FAILED_ID): 
			 builder.setMessage("Failed!")
			 	.setNeutralButton("Ok", new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						//stay in activity
					}
				}); 
		 break;
		 case (DIALOG_NEW_NODE):
			// Set an EditText view to get user input 
			final EditText input = (new EditText(this));
		 	input.setText("+");

			 builder.setMessage("Provide the mobile number: +##########")
			 	.setView(input)
			 	.setCancelable(false)
			 	.setPositiveButton("Ok", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mobileNo=input.getText().toString();
						//toggle receivebutton on default, is written to dataservice upon save
						receivesButton.setClickable(false);
						receivesButton.setChecked(true);
					}
				})
				.setNegativeButton("Cancel", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ChangePlatActivity.this.finish();
					}
				}); 
			 break;
		 case (DIALOG_NO_PROVIDER):
			 builder.setMessage("Please enable the GPS module.")
			 	.setNeutralButton("Ok", new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();	//just closes the dialog
						
					}
				});
			 break;
		}
		return builder.create();
		
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				mobileLocation = location;
				
				mLocationManager.removeUpdates(locListener); // This needs to stop getting the location data and save the battery power.
				wasAquired=true;
				//gathering data
				double longitude = mobileLocation.getLongitude();
				int longi=(int) Math.round(longitude*1000000); //sufficient accuracy, microdegrees
				double latitude = mobileLocation.getLatitude();
				int lati=(int) Math.round(latitude*1000000); //sufficient accuracy, microdegrees
				saveGMaps("https://maps.google.com/maps?q="+latitude+","+longitude);
				//have to save this on sdcard
				
				platformMetadataNew[1]=longi;
				platformMetadataNew[2]=lati;
				setPlatformMetadata();
				
				

			}
			public void onProviderDisabled(String provider) {}
			public void onProviderEnabled(String provider) {}
			public void onStatusChanged(String provider, int status,Bundle extras) {}
		};
		if (!wasSetOnce) {
        	new setUpMetadataTask().execute(null,null,null);
        	wasSetOnce=true;
        }
	}
	
	@Override
	protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (mConnect.isBound()) {
            unbindService(mConnect);
        }
     //   mLocationManager.removeUP...(locListener);	//save battery !
        wasSetOnce=false;
    }
	
	public void save(View view){
		getPlatformMetadata();
		getSensorMetadata();
		if (newPlat) {
			//save new platform, this creates all necessary subsensors
			platformId = dataService.insertPlatformDefault(platformMetadataNew[1], platformMetadataNew[2], platformMetadataNew[0], mobileNo, platformDescriptionNew);
			//update sensor metadata:
			sensorCursor= dataService.getSensorsByPlatformId(platformId);
			for (int i=0;i<4;i++) {
				dataService.updateSensor(
						sensorCursor.getLong(sensorCursor.getColumnIndex(DatabaseControl.KEY_ID)),
						sensorMetadataNew[i*3], sensorMetadataNew[i*3+1], sensorMetadataNew[i*3+2],(int) platformId);
				sensorCursor.moveToNext();
				}
			//add to known numbers
			dataService.addNumberOfInterest(mobileNo);
			//mark it be the receiving platform for this number:
			dataService.bindNumber(mobileNo, platformId);
			showDialog(DIALOG_SUCCESS_ID);	//this finishes
			
		} else {	//normal case: ask for update/insert/delete
			
			showDialog(DIALOG_PROMPT_ID);
		}
	}
	
	
	
	
	//Button responses:
		
	private void saveGMaps(String gmaps) {
		BufferedWriter out;
		try {
			//MODE_PRIVATE creates file, if it does not exist now
			out = new BufferedWriter(new OutputStreamWriter(openFileOutput(	"GPScoordinates", MODE_APPEND))); // mode_private overwrites old file
			out.write(gmaps);
			out.close();	
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}
		
		

	}
	
	
	public void setPlatformMetadata() {
		viewId.setText(Long.toString(platformId));
		viewMobileNo.setText(mobileNo);
		editDescription.setText(platformDescriptionNew);
		editPeriod.setText(Integer.toString(platformMetadataNew[0]));
		editLongitude.setText(Integer.toString(platformMetadataNew[1]));
		editLatitude.setText(Integer.toString(platformMetadataNew[2]));
	}
	
	public void setSensorMetadata() {
		editSensorLongitude.setText(Integer.toString(sensorMetadataNew[activeSensor*3]));
		editSensorLatitude.setText(Integer.toString(sensorMetadataNew[activeSensor*3+1]));
		editSensorElevation.setText(Integer.toString(sensorMetadataNew[activeSensor*3+2]));
	}
	
}
