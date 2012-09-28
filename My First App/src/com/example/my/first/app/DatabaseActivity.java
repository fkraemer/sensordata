package com.example.my.first.app;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import com.example.my.first.app.ChooseTimeActivity.getTimeTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DatabaseActivity extends Activity {

	private DataService dataService;
	private final CountDownLatch latch = new CountDownLatch(1);
	private MyConnection mConnect = new MyConnection(latch);
	private Context cx;
	boolean wasSetOnce=false;
	private TextView text;

	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.select);
		cx=getApplicationContext();
		text = (TextView) findViewById(R.id.textViewDatabase);

		//getting the platform to work on
		Bundle extras = getIntent().getExtras();
			
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

	public void backupWeb(View view) {
		if (dataService.backupDbToWeb()) {
			text.setText("Successfully uploaded to Web");
		} else {
			text.setText("failure");
		}
	}
	
	public void download(View view) {
		if (dataService.downloadDb()) {
			text.setText("Successfully downloaded and set database.");
		}
	}

	public void backupExternal(View view) {
		if (dataService.backupToExternal()) {
			text.setText("Successfully backed up to SD");
		} else {
			text.setText("failure");
			/**try {
				String[] s = getAssets().list("/assets/");
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<s.length;i++) {
					sb.append(s[i]);
				}
				text.setText(sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	

	public void restoreExternal(View view) {
		if (dataService.restoreFromExternal()) {
			text.setText("Successfully restored from SD");
		} else {
			text.setText("failure");
		}
	}
}
