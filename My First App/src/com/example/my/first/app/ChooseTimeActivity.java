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

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosetime);
		cx=getApplicationContext();

		//getting the platform to work on
		Bundle extras = getIntent().getExtras();
		platformId=extras.getLong("platformId");
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
			
			
			return null;
		}
		//option to update progressbar via this thread
		protected void onPostExecute(Void result) {
			//set listener, do some sort of action, then set time interval
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
	
	public void plot(View view) {
		Intent intent=new Intent(cx, PlotActivity.class);
		intent.putExtra("minTime", timeMin);
		intent.putExtra("maxTime", timeMax);
		intent.putExtra("platformId", platformId);
		startActivity(intent);
		
		
	}
}
