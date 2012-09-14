package com.example.my.first.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	public final static String[] NUMBERSOFINTEREST = { "+61431220285","+61415361829" };

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent intent = new Intent(this, DataService.class);
		startService(intent);
		intent = new Intent(this, LocalService.class);
		startService(intent);
		
		
	}




	@Override
	protected void onDestroy() {
		if (isFinishing()) {
			//Intent myIntent = new Intent(this,DataService.class);
			//stopService(myIntent);
		}
		super.onDestroy();
	}

	
	@Override
	protected void onStop() {
		onDestroy();
		super.onStop();
	}




	public void insertNode(View view) {
		finish(); //calls ondestroy then
	}

	public void changeMetadata(View view) {
		  Intent myIntent = new Intent(this,ChoosePlatActivity.class);
		  startActivity(myIntent);
	  }
	  

	  public void selectPlots(View view) {
		  Intent myIntent = new Intent(this,SelectActivity.class);
		  startActivity(myIntent);
	  }
	  

}
