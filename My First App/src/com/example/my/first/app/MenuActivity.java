package com.example.my.first.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.database.Cursor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;

import com.example.sensor.data.DataSet;
import com.example.sensor.data.DataStorage;

public class MenuActivity extends Activity {

	private String[] data = new String[0];
	private String[] numbers = new String[0];
	private String[] adapterFill = new String[0];
	private DataStorage storage = new DataStorage(); 
	private final static String[] NUMBERSOFINTEREST = { "+61431220285" };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		ListView list = (ListView) findViewById(R.id.listView1);
		if (getSms(this) > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, adapterFill);
			list.setAdapter(adapter);
			saveSms();
		}

	}
	

	public void saveSms() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWritable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWritable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWritable = false;
		}

		String FILENAME = "smsdata";
		try {
			File smsdata = new File(getExternalFilesDir(null), "smsdata.txt");
			FileOutputStream fos = new FileOutputStream(smsdata);

			for (int i = 0; i < data.length; i++) {
				fos.write((data[i] + "\n").getBytes());
			}
			
			fos.close();
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}

	}


	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
	}

	@TargetApi(9)
	public int getSms(Context cx) {
		Cursor curs = cx.getContentResolver().query(
				Uri.parse("content://sms/inbox"), null, null, null, null);

		int count = curs.getCount();
		data = new String[count];
		numbers = new String[count];
		String[] searchNumbers = new String[count];
		int interestCount = 0;

		if (curs.moveToFirst()) {
			for (int i = 0; i < curs.getCount(); i++) {
				try {
					searchNumbers[i] = curs.getString(curs
							.getColumnIndex("address"));
					boolean check = false;
					for (int j = 0; j < NUMBERSOFINTEREST.length; j++) {
						if (searchNumbers[i].equals(NUMBERSOFINTEREST[j])) {
							check = true;
						}
					}
					if (check) {
						numbers[interestCount] = searchNumbers[i];
						data[interestCount] = curs.getString(curs
								.getColumnIndex("body"));
						interestCount++;
					}
					curs.moveToNext();
				} catch (IllegalArgumentException e) {

					System.out.println("No Sms in Database.");
				}
			}
		}
		curs.close();
		numbers = Arrays.copyOfRange(numbers, 0, interestCount);
		data = Arrays.copyOfRange(data, 0, interestCount);
		adapterFill = new String[interestCount];
		
		for (int i=0; i <interestCount; i++) 
		{
			DataSet neu = storage.addNewDataSet(data[i], Long.getLong(numbers[i].substring(1, numbers[i].length())));
			Date time=neu.getDate();
			adapterFill[i]=(Integer.toString(i)+")  from  +"+neu.getId()+"   "+time.toString());
		
		}
		
		
		return interestCount;
	}

	public void plotPlots(View view) {
		Intent myIntent = new Intent(this, PlotActivity.class);
		startActivity(myIntent);
	}

}