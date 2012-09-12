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
		
		
	}


	

	  public void choosePlots(View view) {
		  Intent myIntent = new Intent(this,MenuActivity.class);
//		  startActivity(myIntent);
	  }
	  

	  public void selectPlots(View view) {
		  Intent myIntent = new Intent(this,SelectActivity.class);
		  startActivity(myIntent);
	  }
	  

}
