package com.example.my.first.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream.PutField;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus.NmeaListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View.MeasureSpec;
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
		private Thread checkSmsThread;
		private DatabaseControl db;
		private NotificationManager mNotificationManager;
		
		//these two ArrayLists connect the mobile numbers with the platform id of the database, where there shall be saved next time
		private ArrayList<String> lastMobileNo =new ArrayList<String>();
		private ArrayList<Long> lastPlatformId =new ArrayList<Long>();
		
		private ArrayList<String> numbersOfInterest= new ArrayList<String>();
		
		

	@Override
	public void onCreate() {
		cx =getApplicationContext();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//setup default numbers from main activity:
		for (int i=0; i<MainActivity.NUMBERSOFINTEREST.length;i++) {
			numbersOfInterest.add(MainActivity.NUMBERSOFINTEREST[i]);
		}
		//setup as it was before
		getStateFromFile();

		//opening database
		db = new DatabaseControl(cx);
		db.open();
		//test
		Cursor curs = db.getAllPlatforms();
		Toast.makeText(cx, "platforms: "+curs.getPosition()+" count: "+curs.getCount(), Toast.LENGTH_SHORT).show();
		//curs = db.getMeasurement();
		//Toast.makeText(cx, "number of measurements: "+curs.getCount(), Toast.LENGTH_LONG).show();
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
							CharSequence tickerText ="Saved "+((received>1) ? received+" new messages" : "1 new message");
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

	
	public void insertTestData() {
		for(int platformNo=1;platformNo<=2;platformNo++) {
			long testplat = db.insertPlatformDefault(0, 0, 30, "+61123456"+platformNo, "");
			Cursor subsensorCursor=db.getSubsenorsByPlatform(testplat);
			int [][] measurements= new int[24*365*5][9];
			for (int i=0;i<9;i++) {
				long subsensorId=subsensorCursor.getLong(subsensorCursor.getColumnIndex(DatabaseControl.KEY_ID));
				subsensorCursor.moveToNext();
				measurements[0][0]=30;
				measurements[0][1]=12;
				measurements[0][2]=9;
				measurements[0][3]=27;
				measurements[0][2]=0;
				measurements[0][3]=0;
				for (int j=0;j<(24*265*5);j++) {
					measurements[j][i]=j % 500;	//insert generated 0<data<500 
				}
			}
			db.putMeasurements(measurements, 0.1f, testplat);
					
			
		}
	}
	
	@Override
	/**
	 * provides the following methods: TODO
	 * set whether to connect to the internet or not
     * updates global database (evntl local database also)
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

	//****************************************************************************************************
	//public methods used by bound activities
	
	public boolean backupToExternal() {
		return db.backupExternal();
	}

	public  boolean restoreFromExternal() {
		try {
			//this must be equal in DatabaseControl and here
			String DB_PATH_EXTERNAL=getExternalFilesDir(null)+"/databases";

			File file= new File(DB_PATH_EXTERNAL,DatabaseControl.DATABASE_NAME);
			db=db.updateDatabase(new FileInputStream(file));
			
			//TODO process the new database ==> bind numbers, but which ones on multiple numbers ??
			//								==> numbers of interest add
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public void backupDbToWeb() throws MalformedURLException, IOException {
			InputStream database= new FileInputStream(getDatabasePath(DatabaseControl.DATABASE_NAME));
			FtpConnect.uploadDb(database);
	}
	
	public void downloadDb() throws MalformedURLException, IOException {
			//this must be equal in DatabaseControl and here
			String DB_PATH_EXTERNAL=getExternalFilesDir(null)+"/databases";

			File file= new File(DB_PATH_EXTERNAL);
			if (!file.exists()) {
				file.mkdir();
			}
			file = new File(DB_PATH_EXTERNAL,DatabaseControl.DATABASE_NAME);
			FileOutputStream fOS= new FileOutputStream(file);
			FtpConnect.downloadDb(fOS);
			
			db=db.updateDatabase(new FileInputStream(file));
	}
	

	public void addNumberOfInterest(String mobileNo) {
		if (!numbersOfInterest.contains(mobileNo)) numbersOfInterest.add(mobileNo);
		
	}

	public void bindNumber(String mobileNo, long platformId) {
		int listIndex=lastMobileNo.indexOf(mobileNo);
		if (listIndex==-1) {	//not yet in the list
			lastMobileNo.add(mobileNo);
			lastPlatformId.add(platformId);
		} else {	//already in there, replace!
			lastPlatformId.set(listIndex, platformId);
		}
	}
	
	public void deleteboundNumber(String mobileNo, long platformId) {
		int listIndex=lastMobileNo.indexOf(mobileNo);
		if (listIndex!=-1 && lastPlatformId.get(listIndex)==platformId) {	// in the list and receiving
			lastMobileNo.remove(mobileNo);
			lastPlatformId.remove(platformId);
		} 
	}
	
	public boolean platformIdIsBound(long platformId) {
		return lastPlatformId.contains(platformId);
	}
	
	//database methods, just connecting to databaseControl, called here for easy access in all activities,
	//makes sure db.open/close is done clean
	
	public boolean deletePlatform(long platformId) {
		return db.deletePLATFORM(platformId);
	}

	public Cursor getPlatform(long platformId) {
		return db.getPlatform((int) platformId);
	}
	
	public Cursor getPlatforms() {
		return db.getAllPlatforms();
	}


	public Cursor getSensorsByPlatformId(long platformId) {
		return db.getSensorsByPlatformId(platformId);
	}
		
	public Cursor getSubsensorsByPlatformId(long platformId) {
		return db.getSubsenorsByPlatform(platformId);
	}


	public Cursor getMeasuremntBySubsensor(long subsensorId) {
		return db.getMeasurement(subsensorId);
	}
	
	public Cursor getMeasuremntByInterval(long timeMin, long timeMax, long subsensorId) {
		return db.getMeasurementsInterval(timeMin, timeMax, subsensorId);
	}
	
	public boolean updatePlatform(long id, int lon, int lat, int period,String mobileNo, String descr){
		return db.updatePlatform(id, lon,lat,period, mobileNo,descr);
	}
	
	public boolean updateSensor(long id, int offX, int offY, int offZ, int platformId) {
		return db.updateSensor(id, offX, offY, offZ, platformId);
		
	}
	
	public long insertPlatform(int lat, int lon,int period, String mobileNo, String descr) {
		return db.insertPlatformDefault(lat, lon,period, mobileNo, descr);
	}
	
	public long insertPlatformDefault(int lat, int lon, int period, String mobileNo,String descr) {
		return db.insertPlatformDefault(lat, lon, period, mobileNo, descr);
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
	 * [1]:[number]+"/"+platformId that sms should be saved to+"/"+[number]+"/"+[platform that sms...]+"/"+[...]+"/"+[...]
	 * [2]:[number of interest]+"/"+[number of interest]+"/"...
	 * [3]:
	 * [4]:
	 * @param write
	 */
	public void getStateFromFile() {
		BufferedReader in;
		String [] stringStore=new String[5];
		try {
			File file= new File(getExternalFilesDir(null)+"/lastState.txt");
			if (!file.exists()) {
				saveStateToFile(); // called at the very first startup if file doesnt exist already
				return;//then exiting here
			}
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			//reading line by line
			int count=0;
			String string=null;
			while ((string=in.readLine())!=null) {
				stringStore[count] = string;
				count++;
			}
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;	
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}
		
		//working with the read data and the expected scheme
		if (stringStore[0]!=null) {
			receivedNewSms = (stringStore[0].charAt(0)!='0');	//sets false if 0, true if 1
		}
		if (stringStore[1]!=null) {
			//reading the pattern of saved mobileNo and platform ids tuples.
			boolean isId=false;	//is switched for the different types
			int i=1;
			int lastCount=0;
			while (i<stringStore[1].length()) {
				if (stringStore[1].charAt(i)=='/') {	//reading till slash found, this seperates the single values
					if (!isId) {
						lastMobileNo.add(stringStore[1].substring(lastCount, i));	//reading and setting next type to be read, char at i is excluded
						isId=true;
					} else {
						lastPlatformId.add(Long.decode(stringStore[1].substring(lastCount, i)));
						isId=false;
					}
					i++;		//extra step, since minimum one char to next '/'
					lastCount=i;	//one char behind '/'
				}
				i++;
			}
		}
		if (stringStore[2]!=null) {
			//reading the pattern of mobileNo
			int j=1;
			int lastCoun=0;
			while (j<stringStore[1].length()) {
				if (stringStore[1].charAt(j)=='/') {	//reading till slash found, this seperates the single values
					addNumberOfInterest(stringStore[1].substring(lastCoun, j));	
					j++;		//extra step, since minimum one char to next '/'
					lastCoun=j;	//one char behind '/'
				}
				j++;
			}
		}
			
		//read more stringStore lines here

	}
	
	
	//saving the state before shutdown
	public void saveStateToFile() {
		String [] stringStore=new String[5];
		//preparing data to be written
		//------------------------
		
		stringStore[0]=String.valueOf(((receivedNewSms) ? '1' : '0'))+"\n"; //puts a 1 for receivedSms==true
		//------------------------
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<lastMobileNo.size();i++) {
			sb.append(lastMobileNo.get(i));
			sb.append('/');
			sb.append(lastPlatformId.get(i));
			sb.append('/');
		}
		sb.append('\n');
		stringStore[1]=sb.toString();
		//------------------------
		//numbers of interest
		//------------------------
		sb=new StringBuilder();
		for(int i=0;i<numbersOfInterest.size();i++) {
			sb.append(numbersOfInterest.get(i));
			sb.append('/');
		}
		sb.append('\n');
		stringStore[2]=sb.toString();
		
		//writing data to file, limitted to 3string right now!
		BufferedWriter out;
		try {
			//creates file, if it does not exist now
			out = new BufferedWriter(new FileWriter(getExternalFilesDir(null)+"/lastState.txt",false)); // "false" overwrites old file
			for (int i = 0; i < 3; i++) {
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
		//interestCount counts how many of the read sms are actually sensor-messages we care about
		int interestCount = 0;
		// fatalCount counts the fatalerrors, sms that could not be decoded
		int fatalCount=0;
		//reading sms from phone
		if (!curs.moveToFirst()) {
			return 0;
		}
		
		for (int i = 0; i < curs.getCount(); i++) {
			try {
				//checking sender mobile number against the numbers of interest:
					String searchNumber = curs.getString(curs.getColumnIndex("address"));
				if (numbersOfInterest.contains(searchNumber)) {
					//copy data for decoding
					String body = curs.getString(curs.getColumnIndex("body"));		//content of sms
					//save and remove from phone
					String logString=((new SimpleDateFormat().format(curs.getLong(curs.getColumnIndex("date")))))
							+"|"+searchNumber + "|" + body;
					//delete here, so decoding does not run into the same problem after theoretical failure, different Uri necessary
				cx.getContentResolver().delete(Uri.parse("content://sms"), "_id="+curs.getString(curs.getColumnIndex("_id")), null);
					//*************************************************************************************************************************************
					//decoding and putting into the db
					int [][] sensorData = null;
					try {

						//special testcase
						if (body.equals("test.")) {
							insertTestData();
							return 20;
						}
						
						//decoding data
						sensorData = DataCompression.decode(body);
						
						
						//for logging
						logSms(logString+"|"+sensorData[0][1]+":"+sensorData[0][2]+":"+sensorData[0][3]+":"
								+sensorData[0][4]+":"+sensorData[0][5]+":"+"|ok");
					} catch (DecodeFatalException e) {
						fatalCount++;
						logSms(logString+"|"+e.toString());
						continue;	//starts with next sms
					}catch (DecodeRecoverException e) {
						//recoeverd data is saved in the exception
						sensorData = ((DecodeRecoverException) e).getDataInts();
						//for logging
						logSms(logString+"|"+sensorData[0][1]+":"+sensorData[0][2]+":"+sensorData[0][3]+":"
								+sensorData[0][4]+":"+sensorData[0][5]+":"+"|"+e.toString());
					} catch (DecodeException e) {			//unreachable
					}
					
					//************************** decoding done, putting into database ************************************************************
					long platformId=-1;
					int listIndex=lastMobileNo.indexOf(searchNumber);	//check, whether there is an existing link for this number
					if (listIndex!=-1){
						platformId=lastPlatformId.get(listIndex);
					}
					//try to find existing platform in the db with this mobileNo, if several take the latest //TODO
					if (platformId==-1) {
						 platformId=db.putPlatform(searchNumber);
					}
					if (platformId == -1) { //case, have to create new platform:
						//TODO start translucent activity, ask user for metadata, e.g. GPS and description
							int period = sensorData[0][0];   //getting period from decoded data
							platformId=db.insertPlatformDefault(0, 0, period, searchNumber, "");	//inserting without metadata, this creates the necessary subsensors
							
						}
					//we might not know about this sensor yet, mark in the bindnumbers:
					bindNumber(searchNumber, platformId);
					//subsensors and sensors must have been created for putMeasurements, make sure to do this each time a new platform is inserted
					db.putMeasurements(sensorData,0.1f, (int) platformId);			
					//***************************************************************************************************************************
					
					interestCount++;
				}
				curs.moveToNext();
			} catch (IllegalArgumentException e) {

				System.out.println("No Sms in Database.");
			}
		}
		curs.close();
		return interestCount-fatalCount;
	}

	

}

