package com.example.my.first.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		
	}


	

	  public void choosePlots(View view) {
		  Intent myIntent = new Intent(this,MenuActivity.class);
		  startActivity(myIntent);
	  }
	  

}
