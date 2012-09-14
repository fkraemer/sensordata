package com.example.my.first.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.example.my.first.app.LocalService.LocalBinder;
import com.example.sensor.data.DataSet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class SelectActivity extends Activity {
	
	LocalService mService ;
	boolean mBound ;
	final CountDownLatch latch = new CountDownLatch(1);
    Intent serviceIntent;
    private ListView list;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select);
		//create new thread to bind to service: (avoids doing this in the ui-thread)
		serviceIntent = new Intent(this, LocalService.class);
		
		// Bind to LocalService
        bindService(serviceIntent, mConnection, Context.BIND_ABOVE_CLIENT);

        list=(ListView) findViewById(R.id.listView1);

		
	}
	
	
	
	
/**	public void plotSelected(View view) {
		
		
			Intent myIntent = new Intent(this, PlotActivity.class);
	//		myIntent.putParcelableArrayListExtra("selected", selected);
			startActivity(myIntent);
	}*/
	
	
	  protected void onStart() {
		  super.onStart();

	        Thread t = new Thread() {
	        	public void run() {
					try {
						latch.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            int num = mService.getRandomNumber();
          //  Toast.makeText(getApplicationContext(), "number: " + num, Toast.LENGTH_SHORT).show();
	        	}
	        	
	        };
	        t.start();
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        // Unbind from the service
	        if (mBound) {
	            unbindService(mConnection);
	            mBound = false;
	        }
	    }

	    /** Called when a button is clicked (the button in the layout file attaches to
	      * this method with the android:onClick attribute) */
	    public void plotSelected(View view) {
	    }

	    /** Defines callbacks for service binding, passed to bindService() */
	    private ServiceConnection mConnection = new ServiceConnection() {

	        public void onServiceConnected(ComponentName className,
	                IBinder service) {
	            // We've bound to LocalService, cast the IBinder and get LocalService instance
	            LocalBinder binder = (LocalBinder) service;
	            mService = binder.getService();
	            mBound = true;
                latch.countDown();
	        }

	        public void onServiceDisconnected(ComponentName arg0) {
	            mBound = false;
	        }
	    };
	}
