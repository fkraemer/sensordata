package com.example.my.first.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	//sets numbers to be recognized as platformnumbers
	//these are default numbers, they can also be added through the "insert new node" button
	public static final String[] NUMBERSOFINTEREST = { "+61431220285","+61415361829" };
	//sets the time between to checks of the inbox:
	public static final int CHECK_SMS_PERIOD = 10000;

	public static final int SELECT_AND_PLOT=0;
	public static final int SELECT_AND_CHANGE=1;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent intent = new Intent(this, DataService.class);
		startService(intent);
	}




	@Override
	protected void onDestroy() {
		if (isFinishing()) {
			Intent myIntent = new Intent(this,DataService.class);
			stopService(myIntent);
		}
		super.onDestroy();
	}

	
	@Override
	protected void onStop() {
		super.onStop();
	}

	public void plotPlots(View view) {
	//	Intent intent= new Intent(this, PlotTestActivity.class);
	//	startActivity(intent);
		
		
		//Intent intent=new Intent(this, PlotActivity.class);
		//intent.putExtra("minTime", Long.MIN_VALUE);
		//intent.putExtra("maxTime", Long.MAX_VALUE);
		//intent.putExtra("platformId",new Long(6));
		//startActivity(intent);
	}
	
	public void databaseAct(View view) {
		Intent myIntent = new Intent(this,DatabaseActivity.class);
		 startActivity(myIntent);
	}


	public void insertNode(View view) {
		 Intent myIntent = new Intent(this,ChangePlatActivity.class);
		 myIntent.putExtra("platformId", -1L);		//showing the activity to create a new platform
		 startActivity(myIntent);
	}

	public void changeMetadata(View view) {
		  Intent myIntent = new Intent(this,ChoosePlatActivity.class);
		  myIntent.putExtra("mode", SELECT_AND_CHANGE);
		  startActivity(myIntent);
	  }
	  

	  public void selectPlots(View view) {
		  Intent myIntent = new Intent(this,ChoosePlatActivity.class);
		  myIntent.putExtra("mode", SELECT_AND_PLOT);
		  startActivity(myIntent);
	  }
	  

}
