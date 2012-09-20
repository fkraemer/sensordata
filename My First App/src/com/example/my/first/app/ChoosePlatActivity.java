package com.example.my.first.app;


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
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChoosePlatActivity extends Activity {
	
	private DataService dataService;
	private ListView list;
	private Cursor curs=null;
	private CustomCursorAdapter customAdapter;
	private SimpleCursorAdapter simpleCA;
	private final CountDownLatch latch = new CountDownLatch(1);
	private MyConnection mConnect = new MyConnection(latch);
	private Context cx;
	private String[] stringList= new String[]{"a","b"};
	boolean wasSetOnce=false;
	private int mode;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.chooseplat);
		cx=getApplicationContext();

		//getting the platform to work on
		Bundle extras =getIntent().getExtras();
		mode=extras.getInt("mode");
		
		//setting up list
		list= (ListView) findViewById(R.id.platList);
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				curs.moveToPosition(position);
				Intent intent = null;
				switch (mode) {
					case(MainActivity.SELECT_AND_CHANGE): 
						intent = new Intent(cx, ChangePlatActivity.class);
						break;
					case(MainActivity.SELECT_AND_PLOT): 
						intent = new Intent(cx, ChooseTimeActivity.class);
						break;
				}
				intent.putExtra("platformId", curs.getLong(curs.getColumnIndex(DatabaseControl.KEY_ID))); //sending platformId to the new activity
				startActivity(intent);
			}
		});
	}
	
	@SuppressWarnings("deprecation") // both just since API11
	protected void onStart() {
        super.onStart();
        // Bind to LocalService, happens in UI-thread, watch time delays !!
        Intent intent =  new Intent(this, DataService.class);
		bindService(intent, mConnect,0);
        if (!wasSetOnce) new getPlatformsTask().execute(null,null,null);
    }

	class getPlatformsTask extends AsyncTask<Void, Void, Void> {


		@Override
		protected Void doInBackground(Void... arg0) {
			wasSetOnce=true;
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			dataService = mConnect.getService();
			curs = dataService.getPlatforms();
			startManagingCursor(curs);
			return null;
		}
		//option to update progressbar via this thread
		protected void onPostExecute(Void result) {
			if (curs.getCount() == 0) {
				Toast.makeText(cx, "No platforms found!", Toast.LENGTH_LONG).show();
				finish();
			} else {
				//checking, which paltform is bound to save the next sms
				boolean[] isBound = new boolean[curs.getCount()];
				for (int i=0;i<curs.getCount();i++) {	//this is saved in the dataservice
					long platformId=curs.getLong(curs.getColumnIndex(DatabaseControl.KEY_ID));
					isBound[i]=dataService.platformIdIsBound((int) platformId);
					curs.moveToNext();
				}
				curs.moveToFirst();
				//set up the adapter, both use R.layout.platform_entry as layout for the listView
				switch (mode) {
				case (MainActivity.SELECT_AND_CHANGE):
					//this sets up the custom adapter, which adds the checkboxes, whether or not this platform is receiving the next sms
					customAdapter = new CustomCursorAdapter(cx, curs, isBound);
					list.setAdapter(customAdapter);
					break;
				case (MainActivity.SELECT_AND_PLOT):
					//sets up a standard cursoradapter
					simpleCA= new SimpleCursorAdapter(cx, R.layout.platform_entry2, curs,
							new String[] {DatabaseControl.KEY_ID, DatabaseControl.KEY_MOBILENO,DatabaseControl.KEY_DESCR},
							new int[] {R.id.id,R.id.mobile_number,R.id.description});
					list.setAdapter(simpleCA);
					break;
				}
			}
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
		new getPlatformsTask().execute(null,null,null);
	}
	
	public void backToMain(View view) {
		finish();
	}

}
