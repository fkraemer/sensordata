package com.example.my.first.app;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.database.Cursor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.example.sensor.data.DataCompression;
import com.example.sensor.data.DataSet;
import com.example.sensor.data.DataStorage;
import com.example.sensor.data.DecodeException;
import com.example.sensor.data.DecodeFatalException;
import com.example.sensor.data.DecodeRecoverException;

public class MenuActivity extends Activity {

	private ListView list;
	private String[] data = new String[0];
	private String[] numbers = new String[0];
	private String[] adapterFill = new String[0];
	private DataStorage storage = new DataStorage();
	private final static String[] NUMBERSOFINTEREST = { "+61431220285" };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
	//	LayoutInflater inflater = getLayoutInflater();
	//	LinearLayout listFooterView= (LinearLayout) inflater.inflate(R.layout.footer_layout, null);
		

		//TODO Progressbar with another thread
		list = (ListView) findViewById(R.id.listView1);
		//list.addFooterView(listFooterView);
		if (getSms(this) > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_multiple_choice, adapterFill);
			list.setAdapter(adapter);
		    list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		    
		  /**  list.setOnItemClickListener(new OnItemClickListener() {
		    	
		    	  public void onItemClick(AdapterView<?> parent, View view,
		    	    int position, long id) {
		    		  
		    		
		    		  StringBuilder sb = new StringBuilder();
		    		  for (int i=0;i<selectedIds.size();i++)
		    		  {
		    			  sb.append(Integer.toString(selectedIds.get(i)));
		    			  sb.append("   ");
		    		  }
		    		  Context cx = getApplicationContext();
		    		  Toast toast = Toast.makeText(cx, sb.toString(), Toast.LENGTH_SHORT);
		    		  toast.show();
		    	  }
		    	
		    	
		    	
		    	
		    	
		    	
			});*/
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

		//reading sms from phone
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
						data[interestCount] = curs.getString(curs.getColumnIndex("body"));
						interestCount++;
					}
					curs.moveToNext();
				} catch (IllegalArgumentException e) {

					System.out.println("No Sms in Database.");
				}
			}
		}
		curs.close();
		
		//decoding sms
		int fatalCount=0;
		
		for (int i=0; i <interestCount; i++) 
		{
			DataSet neu = null;
			try {
				neu = storage.addNewDataSet(DataCompression.decode(data[i]), 
						Long.decode(numbers[i].substring(1, numbers[i].length())));
			} catch (DecodeFatalException e) {
				Toast.makeText(cx, e.toString(), Toast.LENGTH_LONG).show();
				fatalCount++;
				continue;
			}catch (DecodeRecoverException e) {
				Toast.makeText(cx, e.toString(), Toast.LENGTH_SHORT).show();
				
				neu=storage.addNewDataSet(((DecodeRecoverException) e).getDataInts(),
						Long.decode(numbers[i].substring(1, numbers[i].length())));
			} catch (DecodeException e) {			//unreachable}
			}
			
		
		}
		
		//filling adapter with successfull d0ecoded datasets
		adapterFill = new String[storage.size()];		//TODO change to ArrayList, if adding service checking for new SMS
		for (int i=0; i<storage.size(); i++)
		{
			adapterFill[i] = storage.getDatabyLocalId(i).toString();
		}
		
		return interestCount-fatalCount;
	}

	public void plotPlots(View view) {
		ArrayList<DataSet> selected =  new ArrayList<DataSet>();
		SparseBooleanArray checked = list.getCheckedItemPositions();
		for (int i=checked.size();i>=0;i--)
		{
			  if (checked.valueAt(i)) selected.add(storage.getDatabyLocalId(checked.keyAt(i)));
		}
		if (selected.size()>0) {
			Intent myIntent = new Intent(this, PlotActivity.class);
			myIntent.putParcelableArrayListExtra("selected", selected);
			startActivity(myIntent);
		} else {
			Toast.makeText(getApplicationContext(), "No data selected!", Toast.LENGTH_LONG).show();
		}
	}
}
