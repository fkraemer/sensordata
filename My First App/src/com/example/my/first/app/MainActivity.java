package com.example.my.first.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	//sets numbers to be recognized as platformnumbers
	public static final String[] NUMBERSOFINTEREST = { "+61431220285","+61415361829" };//TODO
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
		 // Intent myIntent = new Intent(this,SelectActivity.class);
		 // startActivity(myIntent);
	}


	public void insertNode(View view) {
		finish(); //calls ondestroy then
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
