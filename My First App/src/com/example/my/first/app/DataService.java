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
import android.location.GpsStatus.NmeaListener;
import android.net.Uri;
import android.os.Binder;
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
 */
public class DataService extends Service {

		private boolean receivedNewSms=false; //flag, that update to global db is necessary, false for very first start, is updated then
	    private final IBinder mBinder = new LocalBinder();
		private Context cx;
		private String[] data = new String[0];
		private String[] numbers = new String[0];
		private Thread checkSmsThread;
		private DatabaseControl db;
		private NotificationManager mNotificationManager;
		
		

	@Override
	public void onCreate() {
		cx =getApplicationContext();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//setup as it was before
		getStateFromFile();

		//opening database
		db = new DatabaseControl(cx);
		db.open();
		//test
		Cursor curs = db.getAllPlatforms();
		Toast.makeText(cx, "platforms: "+curs.getPosition()+" count: "+curs.getCount(), Toast.LENGTH_SHORT).show();
		curs = db.getMeasurement();
		Toast.makeText(cx, "number of measurements: "+curs.getCount(), Toast.LENGTH_LONG).show();
	//	Toast.makeText(cx, "Inhalt: "+curs.getLong(curs.getColumnIndex("id")), Toast.LENGTH_LONG).show();
		
		//starting periodical check for new SMS
		checkSmsThread = new Thread() {
			@SuppressWarnings("deprecation") //since deprecated for API 11 (new notification builder), but no workaround for API 9
			public void run() {
				while (!isInterrupted()) {
					try {
						boolean justonce=false;		//TODO, just for debug, allowing for just one sms to be read
						int received=0;
						received=getSms(cx);
						if (received > 0) {
							//showing successful read and save to user
							int icon = R.drawable.notification;
							CharSequence tickerText ="Saved "+received+" new messages";
							long when = System.currentTimeMillis();
							
							//creating a new notification
							Notification notification = new Notification(icon, tickerText, when);
							CharSequence contentTitle = "Sensordata";
							CharSequence contentText = tickerText;
							Intent notificationIntent = new Intent();
							PendingIntent contentIntent = PendingIntent.getActivity(cx, 0, notificationIntent, 0);
							notification.setLatestEventInfo(cx, contentTitle, contentText, contentIntent);
							mNotificationManager.notify(1, notification);	//show it
							receivedNewSms=true; 
						}
						//only check every XX millisecs
						sleep(MainActivity.CHECK_SMS_PERIOD);
					} catch (InterruptedException e) {
						interrupt();
					}
				}
			}
		};
		checkSmsThread.start();
	}

	//cleaning up on ending
	@Override
	public void onDestroy() {
		checkSmsThread.interrupt();
		saveStateToFile();		//can be read on next startup
		db.close();
		mNotificationManager.cancel(1);		//destroying the notification we set before
		super.onDestroy();
	}
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	/**
	 * provides the following methods:
	 * set whether to connect to the internet or not
     * updates global database (evntl local database also)
	 * querying data, by mobileNo, by timeIntervall
	 * get saved values (to plot them)
	 */

	
	//necessary to let activities bind to the service:
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	public class LocalBinder extends Binder {
        DataService getService() {
            // Return this instance of DataService so clients can call public methods
            return DataService.this;
        }
    }
	
	//****************************************************************************************************
	//public methods used by bound activities
	
	
	//database methods, just connecting to databaseControl, called here for easy access in all activities,
	//makes sure db.open/close is done clean

	public Cursor getPlatform(long platformId) {
		return db.getPlatform((int) platformId);
	}
	
	public Cursor getPlatforms() {
		return db.getAllPlatforms();
	}
	
	public Cursor getSensorsByPlatformId(long platformId) {
		return db.getSensorsByPlatformId(platformId);
	}
	
	public boolean updatePlatform(int id, int lon, int lat, int elev,int period, String mobileNo, String descr){
		return db.updatePlatform(id, lon,lat, elev,period, mobileNo,descr);
	}
	
	public boolean updateSensor(int id, int offX, int offY, int offZ, int platformId) {
		return db.updateSensor(id, offX, offY, offZ, platformId);
		
	}
	
	//helper methods:
	//****************************************************************************************************
	
	//logging the read SMS to a file on the sd-card, must be allowed in manifest
	public void logSms(String write) {

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
	 * Reading state before last shutdown:
	 * The lines of lastState should look like this
	 * [0]:(0-false/1-true)(receivednewSms),
	 * [1]:numbers of interest //TODO
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
			//reading line by line
			int count=0;
			String string=null;
			while ((string=in.readLine())!=null) {
				stringStore[count] = string;
				count++;
			}
			in.close();
		} catch (FileNotFoundException e1) {
			saveStateToFile(); // called at the very first startup if file doesnt exist already
			e1.printStackTrace();
			return;	//then exiting here, cause
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}
		
		//working with the read data and the expected scheme
		if (stringStore[0]!=null) {
			receivedNewSms = (stringStore[0].charAt(0)!='0');	//sets false if 0, true if 1
		}

	}
	
	
	//saving the state before shutdown
	public void saveStateToFile() {
		String [] stringStore=new String[5];
		//preparing data to be written
		stringStore[0]=(new char[]{((receivedNewSms) ? '1' : '0'),'\n'}).toString(); //puts a 1 for receivedSms==true
	
		//writing data to file
		BufferedWriter out;
		try {
			//MODE_PRIVATE creates file, if it does not exist now
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
	
	//getSms checks for new Sms of Numbers_of_interest senders
	//these are logged, decoded, saved into the database and then deleted from the phone-inbox
	//returns the number of SMS that have been successful decoded and saved to the db
	@TargetApi(9)
	public int getSms(Context cx) {
		//opening inbox:
		Uri smsInbox=Uri.parse("content://sms/inbox");
		Cursor curs = cx.getContentResolver().query(
				smsInbox, null, null, null, null);
		int count = curs.getCount();
		data = new String[count];
		numbers = new String[count];
		String[] searchNumbers = new String[count];
		//interestCount counts how many of the read sms are actually sensor-messages we care about
		int interestCount = 0;
		//reading sms from phone
		if (curs.moveToFirst()) {
			for (int i = 0; i < 1; i++) {  //<curs.getCount()  //TODO debug mode !!!!!!!!!!!
				try {
					//checking sender mobile number against the numbers of interest:
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
		
		//decoding sms, fatalCount counts the fatalerrors, sms that could not be decoded
		int fatalCount=0;
		for (int i=0; i <interestCount; i++) 
		{
			int [][] sensorData = null;
			
			try {
				//decoding data
				sensorData = DataCompression.decode(data[i]);				
			} catch (DecodeFatalException e) {
				fatalCount++;
				continue;	//starts with next sms
			}catch (DecodeRecoverException e) {
				//recoeverd data is saved in the exception
				sensorData = ((DecodeRecoverException) e).getDataInts();
			} catch (DecodeException e) {			//unreachable
			}
			//try to find existing platform with this mobileNo, if several take the latest //TODO
			long platformId = db.putPlatform(numbers[i]);
			if (platformId == -1) {
				//TODO start translucent activity, ask user for metadata, e.g. GPS and description
					int period = sensorData[0][0];   //getting period from decoded data
					db.insertPlatformToSubsensorWithoutMetadata(period,	numbers[i],"");
				}
				db.putMeasurements(sensorData, (int) platformId);
			
		}
		return interestCount-fatalCount;
	}





}

