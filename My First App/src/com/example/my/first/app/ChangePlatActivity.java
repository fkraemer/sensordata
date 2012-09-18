package com.example.my.first.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(9)
public class ChangePlatActivity extends Activity {

	private DataService dataService;
	
	private Cursor sensorCursor;
	private Cursor platformCursor;

	private final CountDownLatch latch = new CountDownLatch(1);
	private MyConnection mConnect = new MyConnection(latch);
	
	private long platformId;
	private long currentSensorId;
	private TextView viewId;
	private TextView viewMobileNo;
	private EditText editPeriod;
	private EditText editLongitude;
	private EditText editLatitude;
	private EditText editElevation;
	private EditText editDescription;
	private ListView list;
	private EditText editSensorLongitude;
	private EditText editSensorLatitude;
	private EditText editSensorElevation;

	private String mobileNo;
	private String platformDescriptionOld;
	private String platformDescriptionNew;
	private int[] platformMetadataOld=new int[4];	//{period,longitude,latitude,elevation}
	private int[] platformMetadataNew=new int[4];
	private static final String[] sensorList = new String[] 
			{"Sensor 1","Sensor 2","Sensor 3", "Sensor 4"};

	private static final int DIALOG_PROMPT_ID = 0;
	private static final int DIALOG_SAVED_ID = 1;
	private static final int DIALOG_SUCCESS_ID = 2;
	private static final int DIALOG_FAILED_ID = 3;
	
	private int activeSensor=0;
	private long[] sensorIds=new long[4];
	private int[] sensorMetadataOld=new int[12];
	private int[] sensorMetadataNew=new int[12];
	private ArrayAdapter<String> adapter;
	private boolean wasSetOnce=false;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changeplat);
		// setting up the platform text fields
		viewId = (TextView) findViewById(R.id.textView02);
		viewMobileNo = (TextView) findViewById(R.id.textView10);
		editPeriod = (EditText) findViewById(R.id.editText21);
		editLongitude = (EditText) findViewById(R.id.editText31);
		editLatitude = (EditText) findViewById(R.id.editText41);
		editElevation = (EditText) findViewById(R.id.editText51);
		editDescription = (EditText) findViewById(R.id.editText61);
		//setting up the sensor-choose-from-list and editTexts
		list = (ListView) findViewById(R.id.sensorList);
		adapter= new  ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,sensorList);
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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

		//connecting to dataService
		Intent intent =  new Intent(this, DataService.class);
		bindService(intent, mConnect, 0);	
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	    class getPlatformsTask extends AsyncTask<Void, Void, Void> {
		@Override
			protected Void doInBackground(Void... arg0) {
				wasSetOnce=true;
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// dataService now has been set through mConnect
				dataService=mConnect.getService();
		
		
				//getting the current Metadata from Database
				platformCursor = dataService.getPlatform(platformId);
				sensorCursor = dataService.getSensorsByPlatformId(platformId);
				//bind cursor lifecycle to activity

						startManagingCursor(platformCursor);
				startManagingCursor(platformCursor);
				//saving Metadata locally, changed data gets written into the newMetadata fields
				mobileNo=platformCursor.getString(platformCursor.getColumnIndex(DatabaseControl.KEY_MOBILENO));
				platformDescriptionOld=platformDescriptionNew=platformCursor.getString(platformCursor.getColumnIndex(DatabaseControl.KEY_DESCR));
				platformMetadataOld[0]=platformMetadataNew[0]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_PERIOD));	
				platformMetadataOld[1]=platformMetadataNew[1]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_LON));
				platformMetadataOld[2]=platformMetadataNew[2]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_LAT));
				platformMetadataOld[3]=platformMetadataNew[3]=platformCursor.getInt(platformCursor.getColumnIndex(DatabaseControl.KEY_ELEV));
		
				//prepare the sensors
				for (int i=0;i<4;i++) {	
					if (sensorCursor.getCount()<sensorCursor.getPosition()) {		//catches rare wrong cursor indexes
						break;
					}
					sensorIds[i]=sensorCursor.getLong(sensorCursor.getColumnIndex(DatabaseControl.KEY_ID));
					sensorMetadataOld[i*3]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFX));
					sensorMetadataOld[i*3+1]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFY));
					sensorMetadataOld[i*3+2]=sensorCursor.getInt(sensorCursor.getColumnIndex(DatabaseControl.KEY_OFFZ));
					sensorCursor.moveToNext();
				}
				sensorMetadataNew=Arrays.copyOf(sensorMetadataOld, 12);
				return null;
			}
			//option to update progressbar via this thread
			protected void onPostExecute(Void result) {
				//displaying Metadata
				setPlatformMetadata();
				setSensorMetadata();
			}	       
	    }
        if (!wasSetOnce) new getPlatformsTask().execute(null,null,null);
		
	}
	
    protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (mConnect.isBound()) {
            unbindService(mConnect);
        }
        wasSetOnce=false;
    }

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
	}
	
	public void setPlatformMetadata() {
		viewId.setText(Long.toString(platformId));
		viewMobileNo.setText(mobileNo);
		editDescription.setText(platformDescriptionNew);
		editPeriod.setText(Integer.toString(platformMetadataNew[0]));
		editLongitude.setText(Integer.toString(platformMetadataNew[1]));
		editLatitude.setText(Integer.toString(platformMetadataNew[2]));
		editElevation.setText(Integer.toString(platformMetadataNew[3]));
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
		editSensorLongitude.setText(Integer.toString(sensorMetadataNew[activeSensor*3]));
		editSensorLatitude.setText(Integer.toString(sensorMetadataNew[activeSensor*3+1]));
		editSensorElevation.setText(Integer.toString(sensorMetadataNew[activeSensor*3+2]));
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
		}
		return builder.create();
		
		
	}

	private OnClickListener updateOnClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			//updating the platformid
			dataService.updatePlatform((int) platformId, platformMetadataNew[1], platformMetadataNew[2], platformMetadataNew[3],
					platformMetadataNew[0], mobileNo, platformDescriptionNew); 
			//updating the sensors
			for (int i=0;i<4;i++) {
				dataService.updateSensor((int) sensorIds[i], sensorMetadataNew[i*3], sensorMetadataNew[i*3+1], sensorMetadataNew[i*3+2],(int) platformId);
			}
			ChangePlatActivity.this.finish();
		}
	};
	
	private OnClickListener deleteOnClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			 if (dataService.deletePlatform((int) platformId))  {
				 //should delete the whole cascade
				 showDialog(DIALOG_SUCCESS_ID); //this finishes then
			 } else {
				 showDialog(DIALOG_FAILED_ID);
			 }
		}
	};
	
	private OnClickListener insertOnClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dataService.insertPlatform(platformMetadataNew[1], platformMetadataNew[2], platformMetadataNew[3],
					platformMetadataNew[0], mobileNo, platformDescriptionNew);
			//updating the sensors
			for (int i=0;i<4;i++) {
				dataService.updateSensor((int) sensorIds[i], sensorMetadataNew[i*3], sensorMetadataNew[i*3+1], sensorMetadataNew[i*3+2],(int) platformId);
			}
			ChangePlatActivity.this.finish();
		}
	};
	
	//Button responses
	public void dontsave(View view) {
		finish();
	}
	
	public void save(View view){
		getPlatformMetadata();
		getSensorMetadata();
		showDialog(DIALOG_PROMPT_ID);
	}
	
}
