
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
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChoosePlatActivity extends Activity {
	
	DataService dataService;
	boolean mBound ;
	MyConnection myConnection = new MyConnection();
	ListView list;
	Cursor curs;
	SimpleCursorAdapter adapter;
	final CountDownLatch latch = new CountDownLatch(1);
	Context cx;

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
				intent.putExtra("platformId", curs.getLong(0));
				startActivity(intent);
				// TODO Auto-generated method stub
			}
		});
        // Bind to LocalService	
        Intent intent =  new Intent(this, DataService.class);
		bindService(intent, myConnection,0);
	}
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            dataService = binder.getService();
            mBound = true;
            latch.countDown();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
	
	@SuppressWarnings("deprecation") // both just since API11
	protected void onStart() {
        super.onStart();
		boolean con =false;
		    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if ("com.example.my.first.app.DataService".equals(service.service.getClassName())) {
		            con=true;
		        } 
		    } 

		
		    Thread t = new Thread() {
	        	public void run() {
					try {
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
				}
					//doesnt rech this point
				dataService=myConnection.getService();
				curs = dataService.getPlatforms();
				startManagingCursor(curs);
				adapter = new SimpleCursorAdapter(cx, R.id.platform_entry,curs, new String[] { DatabaseControl.KEY_MOBILENO,DatabaseControl.KEY_ID,
								DatabaseControl.KEY_DESCR }, new int[] {R.id.mobile_number, R.id.id, R.id.description });
				list.setAdapter(adapter);
			}

		};
		t.start();
    }

    protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (mBound) {
            unbindService(mConnection);
        }
    }

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
		onStart();
	}
	
	public void backToMain(View view) {
		onStop();
		finish();
	}

}
