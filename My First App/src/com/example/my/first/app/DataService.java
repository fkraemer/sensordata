package com.example.my.first.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.example.sensor.data.DataCompression;
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
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

/**
 * The DataService acts as a backbone of the whole app.
 * It checks for new SMS periodically, decodes and saves.
 * interacts with the online database, also saving the data there.
 * It queries the local and online database, saves the data to be
 * plotted locally and provides it to the plotActivity when bind by it. 
 */
/**
 * @author flo
 *
 */
public class DataService extends Service {

	//small nested subclass to implement 
		public class LocalBinder extends Binder {
		    DataService getService() {
		        // Return this instance of DataService so clients can call public methods
		        return DataService.this;
		    }
		}
		
	    private Thread checkSmsThread;
		private Context cx;
		private DatabaseControl db;
		//these two ArrayLists connect the mobile numbers with the platform id of the database, where there shall be saved next time
		private ArrayList<String> lastMobileNo =new ArrayList<String>();
		private ArrayList<Long> lastPlatformId =new ArrayList<Long>();
		//this arraylist contains the numbers, that are taken into account when looking for sms, so "normal" non-sensordata sms stay untouched
		private ArrayList<String> numbersOfInterest= new ArrayList<String>();
		
		private final IBinder mBinder = new LocalBinder();
		private NotificationManager mNotificationManager;
		
		
		private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
		
		

	private boolean receivedNewSms=false; //flag, that update to global db is necessary, false for very first start, is updated then

	//checks whether the given number is yet checked for in the inbox
	public void addNumberOfInterest(String mobileNo) {
		if (!numbersOfInterest.contains(mobileNo)) numbersOfInterest.add(mobileNo);
		
	}
	
	//this makes the given platform to be the currently receiving platform for the given mobile number
	public void bindNumber(String mobileNo, long platformId) {
		int listIndex=lastMobileNo.indexOf(mobileNo);
		if (listIndex==-1) {	//not yet in the list
			lastMobileNo.add(mobileNo);
			lastPlatformId.add(platformId);
		} else {	//already in there, replace!
			lastPlatformId.set(listIndex, platformId);
		}
	}
	
	//removes the specific platform from the currently receiving list, if in there
	public void deleteboundNumber(String mobileNo, long platformId) {
		int listIndex=lastMobileNo.indexOf(mobileNo);
		if (listIndex!=-1 && lastPlatformId.get(listIndex)==platformId) {	// in the list and receiving
			lastMobileNo.remove(mobileNo);
			lastPlatformId.remove(platformId);
		} 
	}
	
	//****************************************************************************************************

	//****************************************************************************************************
	//public methods used by bound activities

	/**
	 * uploads the current database to an ftp server, which is specified in the {@link FtpConnect} class
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * 
	 */
	public void backupDbToWeb() throws MalformedURLException, IOException {
		rwl.readLock().lock();
		try {
			//get the databse from the context
			InputStream database= new FileInputStream(getDatabasePath(DatabaseControl.DATABASE_NAME));
			//getting the unique device id
			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	
		    final String tmDevice, tmSerial, androidId;
		    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	
	    	Calendar c= Calendar.getInstance(); //used to give the files unique filenames
	    	String uniqueIdentifier="_"+androidId+"_"+Long.toString(c.getTimeInMillis());
			
			FtpConnect.uploadDb(database,uniqueIdentifier);
		} finally {
			rwl.readLock().unlock();
		}
	}

	
	/** this links to {@link DatabaseControl} backupExternal method
	 * @return 
	 */
	public boolean backupToExternal() {
		rwl.readLock().lock();
		try {
			return db.backupExternal();
		}finally {
			rwl.readLock().unlock();
		}
	}
	
	/**Deletes the platform from the database and due to its cascade mode also all the depending data
	 * 
	 * @param platformId the platform to be deleted
	 * @return whether the platform could be deleted
	 */
	public boolean deletePlatform(long platformId) {
		try {
			rwl.writeLock().lock();
			return db.deletePLATFORM(platformId);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	
	/**This tries to download the latest db from a ftp specified in {@link FtpConnect}
	 * This is first downloaded to the external storage and only if this was done succeful 
	 * a copy is made to the assets, which represents the actually used database.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
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
			
			try {
				rwl.writeLock().lock();
				//actually using the new database now
				db=db.updateDatabase(new FileInputStream(file));
			} finally {
				rwl.writeLock().unlock();
			}
	}
			
	
	
	/** 
	 * @param timeMin 
	 * @param timeMax
	 * @param subsensorId
	 * @return all measurements for the given interval and subsensor, where the upper and lower boundary are included
	 */
	public Cursor getMeasuremntByInterval(long timeMin, long timeMax, long subsensorId) {
		try {
			rwl.readLock().lock();
			return db.getMeasurementsInterval(timeMin, timeMax, subsensorId);
		} finally {
			rwl.readLock().unlock();
		}
	}
	
	public Cursor getMeasurementBySubsensor(long subsensorId) {
		try {
			rwl.readLock().lock();
			return db.getMeasurement(subsensorId);
		} finally {
			rwl.readLock().unlock();
		}
	}
	

	public Cursor getPlatform(long platformId) {
		try {
			rwl.readLock().lock();
			return db.getPlatform((int) platformId);
		} finally {
			rwl.readLock().unlock();
		}
	}

	public Cursor getPlatforms() {
		try {
			rwl.readLock().lock();
			return db.getAllPlatforms();
		} finally {
			rwl.readLock().unlock();
		}
	}
	
	public Cursor getSensorsByPlatformId(long platformId) {
		try {
			rwl.readLock().lock();
			return db.getSensorsByPlatformId(platformId);
		} finally {
			rwl.readLock().unlock();
		}
	}
	

	public Cursor getSubsensorsByPlatformId(long platformId) {
		try {
			rwl.readLock().lock();
			return db.getSubsenorsByPlatform(platformId);
		} finally {
			rwl.readLock().unlock();
		}
	}
	
	public long insertPlatform(int lat, int lon,int period, String mobileNo, String descr) {
		try {
			rwl.readLock().lock();
			return db.insertPlatformDefault(lat, lon,period, mobileNo, descr);
		} finally {
			rwl.readLock().unlock();
		}
	}


	public long insertPlatformDefault(int lat, int lon, int period, String mobileNo,String descr) {
		try {
			rwl.readLock().lock();
			return db.insertPlatformDefault(lat, lon, period, mobileNo, descr);
		} finally {
			rwl.readLock().unlock();
		}
	}
	

	
	
	public boolean updatePlatform(long id, int lon, int lat, int period,String mobileNo, String descr){
		try {
			rwl.writeLock().lock();
			return db.updatePlatform(id, lon,lat,period, mobileNo,descr);
		} finally {
			rwl.writeLock().unlock();
		}
	}
	
	public boolean updateSensor(long id, int offX, int offY, int offZ, int platformId) {
		try {
			rwl.writeLock().lock();
			return db.updateSensor(id, offX, offY, offZ, platformId);
		} finally {
			rwl.writeLock().unlock();
		}
		
	}

	//helper methods:
	//****************************************************************************************************
	
	
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
					//lock before writing
					rwl.writeLock().lock();
					try {
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
					} finally {
						rwl.writeLock().unlock();
					}
				}
				curs.moveToNext();
			} catch (IllegalArgumentException e) {

				System.out.println("No Sms in Database.");
			}
		}
		curs.close();
		return interestCount-fatalCount;
	}
	
	//database methods, just connecting to databaseControl, called here for easy access in all activities,
	//makes sure db.open/close is done clean
	
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
		//Cursor curs = db.getAllPlatforms();
		//Toast.makeText(cx, "platforms: "+curs.getPosition()+" count: "+curs.getCount(), Toast.LENGTH_SHORT).show();
		//curs = db.getMeasurement();
		//Toast.makeText(cx, "number of measurements: "+curs.getCount(), Toast.LENGTH_LONG).show();
	//	Toast.makeText(cx, "Inhalt: "+curs.getLong(curs.getColumnIndex("id")), Toast.LENGTH_LONG).show();
		
		//starting periodical check for new SMS
		checkSmsThread = new Thread() {
			@Override
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
	
	public boolean platformIdIsBound(long platformId) {
		return lastPlatformId.contains(platformId);
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

	

}

