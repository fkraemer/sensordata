package com.example.my.first.app;


import java.util.concurrent.CountDownLatch;

import com.example.my.first.app.DataService.LocalBinder;

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
	
	DataService dataService;
	ListView list;
	Cursor curs=null;
	SimpleCursorAdapter adapter;
	final CountDownLatch latch = new CountDownLatch(1);
	MyConnection mConnect = new MyConnection(latch);
	Context cx;
	String[] stringList= new String[]{"a","b"};
	boolean wasSetOnce=false;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.chooseplat);
		cx=getApplicationContext();
		
		//setting up list
		list= (ListView) findViewById(R.id.platList);
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				curs.moveToPosition(position);
				Intent intent = new Intent(getApplicationContext(), ChangePlatActivity.class);
				intent.putExtra("platformId", curs.getLong(0)); //sending platformId to the new activity
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
			// dataService now has been set through mConnect
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
				adapter = new SimpleCursorAdapter(cx, R.layout.platform_entry, curs, new String[] 
						{ DatabaseControl.KEY_MOBILENO,	DatabaseControl.KEY_ID, DatabaseControl.KEY_DESCR }, 
						new int[] {R.id.mobile_number, R.id.id, R.id.description }); 
				list.setAdapter(adapter);
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
		onStop();
		finish();
	}

}
