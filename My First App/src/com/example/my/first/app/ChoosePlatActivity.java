
package com.example.my.first.app;

import com.example.my.first.app.DataService.LocalBinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ChoosePlatActivity extends Activity {
	
	DataService dataService;
	MyConnection myConnection;
	ListView list;
	Cursor curs;
	SimpleCursorAdapter adapter;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changeplat);
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
	}
	
	@SuppressWarnings("deprecation") // both just since API11
	protected void onStart() {
        super.onStart();
        // Bind to LocalService
		Intent intent =  new Intent(this, DataService.class);
		bindService(intent, myConnection, this.BIND_AUTO_CREATE);	
		dataService=myConnection.getService();
		
		curs = dataService.getPlatforms();
		startManagingCursor(curs);
		adapter= new SimpleCursorAdapter(this,R.id.platform_entry, curs,
				new String[]{DatabaseControl.KEY_MOBILENO,DatabaseControl.KEY_ID,DatabaseControl.KEY_DESCR},
				new int[]{R.id.mobile_number,R.id.id,R.id.description});
		list.setAdapter(adapter);
    }

    protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (myConnection.isBound()) {
            unbindService(myConnection);
        }
    }

	protected void onPause() {
		onStop();
	}

	protected void onResume() {
		onStart();
	}
	
	public void backToMain(View view) {
		finish();
	}

}
