package com.example.my.first.app;


import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChoosePlatActivity extends Activity {
	
	class getPlatformsTask extends AsyncTask<Void, Void, Void> {


		@Override
		protected Void doInBackground(Void... arg0) {
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
		@Override
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
					simpleCA= new SimpleCursorAdapter(cx, R.layout.list_item_platform, curs,
							new String[] {DatabaseControl.KEY_ID, DatabaseControl.KEY_MOBILENO,DatabaseControl.KEY_DESCR},
							new int[] {R.id.id,R.id.mobile_number,R.id.description});
					list.setAdapter(simpleCA);
					break;
				}
			}
		}
		       
    }
	private Cursor curs=null;
	private CustomCursorAdapter customAdapter;
	private Context cx;
	private DataService dataService;
	private final CountDownLatch latch = new CountDownLatch(1);
	private ListView list;
	private MyConnection mConnect = new MyConnection(latch);
	private int mode;
	private SimpleCursorAdapter simpleCA;
	private String[] stringList= new String[]{"a","b"};

	boolean wasSetOnce=false;
	
	public void backToMain(View view) {
		finish();
	}

	@Override
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
						intent = new Intent(cx, ChoosePeriodActivity.class);
						break;
				}
				intent.putExtra("platformId", curs.getLong(curs.getColumnIndex(DatabaseControl.KEY_ID))); //sending platformId to the new activity
				startActivity(intent);
			}
		});
	}
	
    @Override
	protected void onResume() {
		super.onResume();
		//updating the data on resume
		  if (!wasSetOnce) {
	        	new getPlatformsTask().execute(null,null,null);
	        	wasSetOnce=true;
		  }
	}


	@Override
	@SuppressWarnings("deprecation") // both just since API11
	protected void onStart() {
        super.onStart();
        // Bind to LocalService, happens in UI-thread, watch time delays !!
        Intent intent =  new Intent(this, DataService.class);
		bindService(intent, mConnect,0);
        if (!wasSetOnce) {
        	new getPlatformsTask().execute(null,null,null);
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
        wasSetOnce=false;	//flag to execute listupdate on next startup
    }

}
