package com.example.my.first.app;

import java.util.ArrayList;
import java.util.Date;

import com.example.sensor.data.DataSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Toast;

public class SelectActivity extends Activity {
	
	
	


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select);
		
		
		
	}
	
	
	
	
	public void plotSelected(View view) {
		
		
			Intent myIntent = new Intent(this, PlotActivity.class);
	//		myIntent.putParcelableArrayListExtra("selected", selected);
			startActivity(myIntent);
	}
}
