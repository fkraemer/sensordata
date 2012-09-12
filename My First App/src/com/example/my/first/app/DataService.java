package com.example.my.first.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import com.example.sensor.data.DataCompression;
import com.example.sensor.data.DataSet;
import com.example.sensor.data.DataStorage;
import com.example.sensor.data.DecodeException;
import com.example.sensor.data.DecodeFatalException;
import com.example.sensor.data.DecodeRecoverException;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

/**
 * The DataService acts as a backbone of the whole app.
 * It checks for new SMS periodically, decodes and saves.
 * interacts with the online database, also saving the data there.
 * It queries the local and online database, saves the data to be
 * plotted locally and provides it to the plotActivity when bind by it. 
 *
 */
public class DataService extends Service {

		private boolean receivedNewSms=false;
		private Context cx;
		private String[] data = new String[0];
		private String[] numbers = new String[0];
		private String[] adapterFill = new String[0];
		private Thread checkSmsThread;
		
		
	public DataService() {
		//TODO get old state from file
	}

	@Override
	public void onCreate() {
		//setup as it was before
		getStateFromFile();
		cx =getApplicationContext();

			
			//starting periodical check for new SMS
			checkSmsThread = new Thread() {
				@SuppressWarnings("deprecation") //since deprecated for API 11 (new notification builder), but no workaround for API 9
				public void run() {
					while (!isInterrupted()) {
						try {
							int received=getSms(cx);
							if (received > 0) {
								//showing successfull read and save to user
								String ns = Context.NOTIFICATION_SERVICE;
								NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
								
								int icon = R.drawable.notification;
								CharSequence tickerText ="Saved "+received+" new messages";
								long when = System.currentTimeMillis();
								
								Notification notification = new Notification(icon, tickerText, when);
								
								CharSequence contentTitle = "My notification";
								CharSequence contentText = "Hello World!";
								Intent notificationIntent = new Intent();
								PendingIntent contentIntent = PendingIntent.getActivity(cx, 0, notificationIntent, 0);

								notification.setLatestEventInfo(cx, contentTitle, contentText, contentIntent);

								mNotificationManager.notify(1, notification);
								receivedNewSms=true;
							}
							sleep(5000);
						} catch (InterruptedException e) {
							interrupt();
						}
					}
				}
			};
			
			checkSmsThread.start();
					

	}

	@Override
	public void onDestroy() {
		checkSmsThread.interrupt();
		saveStateToFile();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	/**
	 * provides the following methods:
	 * set whether to connect to the internet or not
	 * connect to database   -  updates global database (evntl local database also)
	 * querying data, by mobileNo, by timeIntervall
	 * saving values/timestamps with platformId after query    - get number of values
	 * get saved values (to plot them)
	 */
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	//helper methods:
	//****************************************************************************************************
	
	public void logSms(String write) {

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
			BufferedWriter out = new BufferedWriter(new FileWriter(getExternalFilesDir(null)+"/smsdata.txt",true));
				out.write(write + "\n");
			out.close();
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}

	}

	/**
	 * The lastState should look like this
	 * [0]:(0-false/1-true)(receivednewSms),
	 * [1]:
	 * [2]:
	 * [3]:
	 * [4]:
	 * @param write
	 */
	public void getStateFromFile() {
		BufferedReader in;
		String [] stringStore=new String[5];
		try {
			in = new BufferedReader(new InputStreamReader(openFileInput("lastState")));
		
		//String string=in.readLine();		//not using/dismissing first line, should be used for ringbuffer sort of file
		int count=0;
		String string=null;
		while ((string=in.readLine())!=null) {
			stringStore[count]=string;
		}
		in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}
		
		//working with the read data, might want to catch indexoutofbound
		if (stringStore[0]!=null) {
			receivedNewSms = (stringStore[0].charAt(0)!='0');	//sets false if 0, true if 1
		}

	}
	
	
	public void saveStateToFile() {
		String [] stringStore=new String[5];
		//preparing data to be written
		stringStore[0]=(new char[]{((receivedNewSms) ? '1' : '0'),'\n'}).toString(); //puts a 1 for receivedSms==true
		
		
		//writing data to file
		BufferedWriter out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(openFileOutput(	"lastState", MODE_PRIVATE))); // mode_private overwrites old file
			for (int i = 0; i < 5; i++) {
				out.write(stringStore[i]);
			}
			out.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}
		

	}
	
	@TargetApi(9)
	public int getSms(Context cx) {
		Uri smsInbox=Uri.parse("content://sms/inbox");
		Cursor curs = cx.getContentResolver().query(
				smsInbox, null, null, null, null);

		int count = curs.getCount();
		data = new String[count];
		numbers = new String[count];
		String[] searchNumbers = new String[count];
		int interestCount = 0;

		//reading sms from phone
		if (curs.moveToFirst()) {
			for (int i = 0; i < 1; i++) {  //<curs.getCount()  //TODO debug mode !!!!!!!!!!!
				try {
					searchNumbers[i] = curs.getString(curs.getColumnIndex("address"));
					boolean check = false;
					for (int j = 0; j < MainActivity.NUMBERSOFINTEREST.length; j++) {
						if (searchNumbers[i].equals(MainActivity.NUMBERSOFINTEREST[j])) {
							check = true;
						}
					}
					if (check) {
						//copy data for decoding
						numbers[interestCount] = searchNumbers[i];
						data[interestCount] = curs.getString(curs.getColumnIndex("body"));
						//save and remove from phone
						String id=curs.getString(curs.getColumnIndex("_id"));
						logSms(curs.getString(curs.getColumnIndex("date"))+"/"+numbers[interestCount] + "/" + data[interestCount]);
						cx.getContentResolver().delete(Uri.parse("content://sms"), "_id=?", new String[] {id});
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

		DatabaseControl db = new DatabaseControl(this);
		db.open();
		
		for (int i=0; i <interestCount; i++) 
		{
			int [][] sensorData = null;
			
			//decoding data
			try {
				sensorData = DataCompression.decode(data[i]);
				//database save
				
			} catch (DecodeFatalException e) {
				Toast.makeText(cx, e.toString(), Toast.LENGTH_LONG).show();
				fatalCount++;
				continue;
			}catch (DecodeRecoverException e) {
				Toast.makeText(cx, "DataSet "+i+": "+ e.toString(), Toast.LENGTH_SHORT).show();
				
				sensorData = ((DecodeRecoverException) e).getDataInts();
			} catch (DecodeException e) {			//unreachable
			}
			
			//----------------------------------------------------------------------------------------------------------------------------------------
			//try to minimize extra effort in dataset, maybe dismiss datastorage, maybe not, since will be used rarely
			//datenaufnahme, siehe zettel, abarbeiten
			//set default phenomenas here or in databasecontrol
			//----------------------------------------------------------------------------------------------------------------------------------------
			//add to database if decoding was successful
			if (sensorData != null) {
				long platformId = db.putPlatform(numbers[i]);
				if (platformId == -1) {
					//TODO start translucent activity, ask user for metadata
					int period = sensorData[0][0];   //getting period from decoded data, must be new version!
					db.insertPlatformToSubsensorWithoutMetadata(period,	numbers[i]);
				}
				db.putMeasurements(sensorData, (int) platformId);
			}
		}

		db.close();
		
		
		
		//filling adapter with successfull d0ecoded datasets
		//adapterFill = new String[storage.size()];		//TODO change to ArrayList, if adding service checking for new SMS
		return interestCount-fatalCount;
	}

}

