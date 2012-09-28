package com.example.my.first.app;


import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import com.example.my.first.app.DataService.LocalBinder;
import com.example.sensor.data.Main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChooseTimeActivity extends Activity {
	
	private DataService dataService;
	private Cursor curs=null;
	private final CountDownLatch latch = new CountDownLatch(1);
	private MyConnection mConnect = new MyConnection(latch);
	private Context cx;
	boolean wasSetOnce=false;
	private long platformId; 
	
	//starting with max interval
	private long timeMin=Long.MIN_VALUE;
	private long timeMax=Long.MAX_VALUE;
	
	private ListView list;
	private String[] listOptions;

	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosetime);
		cx=getApplicationContext();

		//getting the platform to work on
		Bundle extras = getIntent().getExtras();
		platformId=extras.getLong("platformId");
		//Setting up ListView
		list = (ListView) findViewById(R.id.chooseTimeList);
		listOptions=new String[]{"last day",
				"last week",
				"last day in database",
				"last week in database", 
				"everything"};
		list.setAdapter(new ArrayAdapter<String>(cx,
				android.R.layout.simple_list_item_checked, listOptions));	
			
        // Bind to LocalService, happens in UI-thread, watch time delays !!
        Intent intent =  new Intent(this, DataService.class);
		bindService(intent, mConnect,0);
	}
	
	@SuppressWarnings("deprecation") // both just since API11
	protected void onStart() {
        super.onStart();
        if (!wasSetOnce) {
        	new getTimeTask().execute(null,null,null);
        	wasSetOnce=true;
        }
    }

	class getTimeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			dataService=mConnect.getService();
			
			
			//could be used to retrieve dynamic information from the database
			
			return null;
		}
		
		//option to update progressbar via this thread
		protected void onPostExecute(Void result) {
			//set listener, do some sort of action, then set time interval
			list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					Intent intent=new Intent(cx ,PlotActivity.class);
					switch(position) {
						case 0:	//last day
							Calendar c1=Calendar.getInstance();
							timeMax=c1.getTimeInMillis();
							timeMin=timeMax-24*60*60*1000;
							break;
						case 1:	//last week
							Calendar c2=Calendar.getInstance();
							timeMax=c2.getTimeInMillis();
							timeMin=timeMax-7*24*60*60*1000;
							break;
						case 2:	//get last timestamp, go back a day from there
							Cursor subsensorCursor1=dataService.getSubsensorsByPlatformId(platformId);  
							long subsensorId1=subsensorCursor1.getLong(subsensorCursor1.getColumnIndex(DatabaseControl.KEY_ID));
							Cursor measurementCursor1=dataService.getMeasuremntBySubsensor(subsensorId1);	//these are returned in ascending(by timestamp) order
							measurementCursor1.moveToLast();
							timeMax=measurementCursor1.getLong(measurementCursor1.getColumnIndex(DatabaseControl.KEY_TIMESTAMP));		
							timeMin=timeMax-24*60*60*1000;
							break;
						case 3:	//get last timestamp, go back a week from there
							Cursor subsensorCursor2=dataService.getSubsensorsByPlatformId(platformId);  
							long subsensorId2=subsensorCursor2.getLong(subsensorCursor2.getColumnIndex(DatabaseControl.KEY_ID));
							Cursor measurementCursor2=dataService.getMeasuremntBySubsensor(subsensorId2);	//these are returned in ascending(by timestamp) order
							measurementCursor2.moveToLast();
							timeMax=measurementCursor2.getLong(measurementCursor2.getColumnIndex(DatabaseControl.KEY_TIMESTAMP));		
							timeMin=timeMax-7*24*60*60*1000;
							
							break;
						case 4://get the first and last timestamp
							Cursor subsensorCursor3=dataService.getSubsensorsByPlatformId(platformId);  
							long subsensorId3=subsensorCursor3.getLong(subsensorCursor3.getColumnIndex(DatabaseControl.KEY_ID));
							Cursor measurementCursor3=dataService.getMeasuremntBySubsensor(subsensorId3);	//these are returned in ascending(by timestamp) order
							timeMin=measurementCursor3.getLong(measurementCursor3.getColumnIndex(DatabaseControl.KEY_TIMESTAMP));
							measurementCursor3.moveToLast();
							timeMax=measurementCursor3.getLong(measurementCursor3.getColumnIndex(DatabaseControl.KEY_TIMESTAMP));
							break;
							
					}
					intent.putExtra("minTime", timeMin);
					intent.putExtra("maxTime", timeMax);
					intent.putExtra("platformId", platformId);
					startActivity(intent);
				}
		}); 
		}
	}
	
    protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (mConnect.isBound()) {
            unbindService(mConnect);
        }
        wasSetOnce=false;	//flag to execute listupdate on next startup
    }

	protected void onResume() {
		super.onResume();
		//updating the data on resume
		if (!wasSetOnce) {
			new getTimeTask().execute(null,null,null);
			wasSetOnce=true;
        }
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
}
