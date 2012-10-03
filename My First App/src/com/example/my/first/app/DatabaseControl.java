 package com.example.my.first.app;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


	
public class DatabaseControl {



	/**
	***************************************************************************************************************************
	 * nested class DatabaseHelper:
	 * 
	 *
	*****************************************************************************************************************************
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
	
		private final static int DATABASE_VERSION=1;
		
		private static final String MEASUREMENT_CREATE="CREATE  TABLE IF NOT EXISTS 	"+ DATABASE_TABLE_MEASUREMENT +" ( " +
				 KEY_TIMESTAMP +" DATE ,  "+ KEY_VALUE +" DECIMAL(4,1)  , "+KEY_SUBSENSORID +"  INTEGER  ," +
				"   FOREIGN KEY ("+ KEY_SUBSENSORID +" )    REFERENCES "+ DATABASE_TABLE_SUBSENSOR +" ("+ KEY_ID +" )" +
				"    ON DELETE CASCADE    ON UPDATE CASCADE PRIMARY KEY ( "+KEY_TIMESTAMP +" , "+KEY_SUBSENSORID +" ))"; 
		
		private static final String PENOMENA_CREATE="CREATE  TABLE IF NOT EXISTS "+ DATABASE_TABLE_PHENOMENA +" ("+ KEY_ID +
				" INTEGER  PRIMARY KEY AUTOINCREMENT ,  "+KEY_UNIT+" TEXT, "+ KEY_MIN + " INT(4,1)  ," +KEY_MAX +
				" INT(4,1) )"; 
		
		private static final String PLATFORM_CREATE="CREATE  TABLE IF NOT EXISTS " + DATABASE_TABLE_PLATFORM + " ( " +
				KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+ KEY_LAT +" INTEGER  ,  "+ KEY_LON +" INTEGER  ,  "+
				KEY_PERIOD +" INTEGER  ," + KEY_MOBILENO +" TEXT NOT NULL, "+ KEY_DESCR +" TEXT NOT NULL )";
	
		private static final String SENSOR_CREATE="CREATE  TABLE IF NOT EXISTS  "+ DATABASE_TABLE_SENSOR +
				"(   "+ KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT ,  "+ KEY_OFFY +" INTEGER  , "+ KEY_OFFX +" INTEGER  , " + KEY_OFFZ +
				" INTEGER  ,  "+ KEY_PLATFORMID +" INTEGER NOT NULL ,    FOREIGN KEY ( "+ KEY_PLATFORMID +
				" )    REFERENCES "+ DATABASE_TABLE_SENSOR +" ("+ KEY_ID +" )    ON DELETE CASCADE    ON UPDATE CASCADE)";
	
		private static final String SUBSENSOR_CREATE="CREATE  TABLE  IF NOT EXISTS "+DATABASE_TABLE_SUBSENSOR+" ( " + 
				KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT , "+ KEY_SENSORID +" INTEGER NOT NULL , "+ KEY_PHENOMENAID +" INTEGER NOT NULL ," +
				"    FOREIGN KEY ("+ KEY_SENSORID +" )    REFERENCES "+ DATABASE_TABLE_SENSOR +" ("+ KEY_ID +" )    ON DELETE CASCADE "+
				"    ON UPDATE CASCADE    FOREIGN KEY ("+ KEY_PHENOMENAID +
				" )  REFERENCES "+DATABASE_TABLE_PHENOMENA +" ("+ KEY_ID +" )    ON DELETE CASCADE    ON UPDATE CASCADE)";
		
		private Context context;
		private String DB_PATH_EXTERNAL;
		private String DB_PATH_LOCAL;
		private boolean isOpen;
		
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			DB_PATH_EXTERNAL=context.getExternalFilesDir(null)+"/databases";
			//DB_PATH_LOCAL = "/data/data/" + context.getPackageName() + "/databases";
			this.context=context;
		}
		
		public boolean backupExternal() {
			try {
				InputStream inputStream= new FileInputStream(context.getDatabasePath(DATABASE_NAME));
				File file= new File(DB_PATH_EXTERNAL);
				if (!file.exists()) {
					file.mkdir();
				}
				file = new File(DB_PATH_EXTERNAL,DATABASE_NAME);
				OutputStream outputStream=new FileOutputStream(file);	
				CopyDB(inputStream,outputStream);
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		private boolean checkDataBaseExternal(){
			File file= new File(DB_PATH_EXTERNAL,DATABASE_NAME);
			return file.exists();
			}
	
		
	
		@Override
		public synchronized void close() {
			isOpen=false;
			super.close();
		}
	
		private  void CopyDB(InputStream inputStream, OutputStream outputStream) throws IOException {	
			//---copy 1K bytes at a time---	
			byte[] buffer = new byte[1024];	
			int length;	
			while ((length = inputStream.read(buffer)) > 0) {	
				outputStream.write(buffer, 0, length);	
			}		
			outputStream.flush();
			outputStream.close();
			inputStream.close();	
		}
		
		@Override
		public SQLiteDatabase getWritableDatabase() {
			File file = context.getDatabasePath(DATABASE_NAME);
			if (!file.exists() && checkDataBaseExternal()) {		//acts on e.g. first startup
				//now copy from external backed up data base
				try {
					File file2= new File(DB_PATH_EXTERNAL,DATABASE_NAME);
					return updateDatabase(new FileInputStream(file2));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			isOpen=true;
			return super.getWritableDatabase();
		}
		
		@Override
	public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(PLATFORM_CREATE);
				db.execSQL(SENSOR_CREATE);
				db.execSQL(SUBSENSOR_CREATE);
				db.execSQL(MEASUREMENT_CREATE);
				db.execSQL(PENOMENA_CREATE);
				if (!db.isReadOnly()) {
		            // Enable foreign key constraints
		            db.execSQL("PRAGMA foreign_keys=ON;");
		        }
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldT, int newT) {
				Log.w("dberror","upgrading from " + oldT + " to "+ newT + ". this drops all data");
				db.execSQL("DROP TABLE IF EXISTS " +DATABASE_TABLE_PLATFORM); // normally copy first, drop then
				db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SENSOR); // normally copy first, drop then
				db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SUBSENSOR); // normally copy first, drop then
				db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_PHENOMENA); // normally copy first, drop then
				db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_MEASUREMENT); // normally copy first, drop then
				this.onCreate(db);
				}
	
		public SQLiteDatabase updateDatabase(InputStream inp) {
			if (isOpen) {	//for updates while running
				close();
			}
			try {
				File file= context.getDatabasePath(DATABASE_NAME);
				CopyDB( inp,new FileOutputStream(file));
			} catch (IOException e) {
				e.printStackTrace();
			}	
			isOpen=true;
			return super.getWritableDatabase();
		}
		}
	
	public final static String DATABASE_NAME="localsensorDB";
	
	private final static String DATABASE_TABLE_MEASUREMENT="measurement";
	private final static String DATABASE_TABLE_PHENOMENA="phenomena";	// has KEY_ID
	private final static String DATABASE_TABLE_PLATFORM="platform";	// has KEY_ID
	private final static String DATABASE_TABLE_SENSOR="sensor";	// has KEY_ID
	private final static String DATABASE_TABLE_SUBSENSOR="subsensor";	// has KEY_ID
	public static final String KEY_DESCR= "description";


	public static final String KEY_ID = "_id";
	public static final String KEY_LAT= "lat";
	public static final String KEY_LON= "lon";
	public static final String KEY_MAX = "max";
	public static final String KEY_MIN = "min";


	public static final String KEY_MOBILENO= "mobile_no";
	public static final String KEY_OFFX= "long_offset";
	public static final String KEY_OFFY= "lat_offset";


	public static final String KEY_OFFZ= "elev_offset";
	public static final String KEY_PERIOD= "period";
	public static final String KEY_PHENOMENAID= "phenomena_id";
	public static final String KEY_PLATFORMID= "platform_id";


	public static final String KEY_SENSORID= "sensor_id";
	public static final String KEY_SUBSENSORID= "subsensor_id";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_UNIT= "unit";
	
	
	
	public static final String KEY_VALUE= "value";
	private SQLiteDatabase db;
	
	private DatabaseHelper dbHelper;
	
public DatabaseControl(Context cx) {
	dbHelper= new DatabaseHelper(cx);
}	


//***********************************************************************************************************************
//*************************************************************************************************************************

	public boolean backupExternal()  {
		return dbHelper.backupExternal();
	}
	
	public void close() {
		dbHelper.close();
	}
		
	
	private boolean deletePhenomena(long id) {
		return db.delete(DATABASE_TABLE_PHENOMENA, KEY_ID + "=" + id,null) > 0;
	}
	
	public boolean deletePLATFORM(long platformId) {
		return db.delete(DATABASE_TABLE_PLATFORM, KEY_ID + "=" + platformId,null) > 0;
	}
	
	private boolean deleteSensor(long id) {
		return db.delete(DATABASE_TABLE_SENSOR, KEY_ID + "=" + id,null) > 0;
	}
	
	private boolean deleteSubsensor(long id) {
		return db.delete(DATABASE_TABLE_SUBSENSOR, KEY_ID + "=" + id,null) > 0;
	}
	
	public Cursor getAllPlatforms() throws SQLException {
		Cursor curs = db.query(DATABASE_TABLE_PLATFORM, null, null, null,null,null,KEY_MOBILENO + " ASC , "+KEY_ID+" ASC");
		curs.moveToFirst();
		return curs;
	}
	
	public Cursor getMeasurement(long subsensorId) {
		Cursor curs = db.query(DATABASE_TABLE_MEASUREMENT,null,
				KEY_SUBSENSORID+" = "+subsensorId,null,null,null,KEY_TIMESTAMP+" ASC",null);
		curs.moveToFirst();
		return curs;
	
	}

	public Cursor getMeasurementsInterval(long minTime, long maxTime, long subsensorId) {
		Cursor curs= db.query(DATABASE_TABLE_MEASUREMENT,new String[]{KEY_TIMESTAMP,KEY_VALUE},
				KEY_SUBSENSORID+" = "+subsensorId+ " AND "+KEY_TIMESTAMP+">="+Long.toString(minTime)+
				" AND "+KEY_TIMESTAMP+"<="+Long.toString(maxTime),null, null,null,KEY_TIMESTAMP+" ASC",null);
		curs.moveToFirst();
		return curs;
	}
	
	//returns phenomena-row with specified id
	public Cursor getPhenomena(long id) throws SQLException {
			return getRowById(id, DATABASE_TABLE_PHENOMENA);
	}
	
	//returns platform-row with specified id
	public Cursor getPlatform(long id) throws SQLException {
		return getRowById(id, DATABASE_TABLE_PLATFORM);
	}
	
	//helper-method for getting rows of all tables with Ids
	private Cursor getRowById(long id, String selectedTable) throws SQLException {
		Cursor myCursor = db.query(selectedTable, null,
				KEY_ID + "=" + id,null,null,null,null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	
	//returns sensor-row with specified id
	public Cursor getSensor(long id) throws SQLException {
		return getRowById(id, DATABASE_TABLE_SENSOR);
	}
	
	
	//getSensorsByPlatformId returns all Sensors of one platform ordered by Id (same order as inserted before)
	public Cursor getSensorsByPlatformId(long platformId) {
		Cursor curs = db.query(DATABASE_TABLE_SENSOR, null, KEY_PLATFORMID+" = "+platformId, null,null,null,KEY_ID+" ASC");
		curs.moveToFirst();
		return curs;
	}
	
	public Cursor getSubsenorsByPlatform(long platformId) {
		Cursor curs = db.rawQuery("SELECT "+ DATABASE_TABLE_SUBSENSOR+"."+KEY_ID+
				" FROM "+DATABASE_TABLE_PLATFORM+","+DATABASE_TABLE_SENSOR+","+DATABASE_TABLE_SUBSENSOR+
				" ON "+DATABASE_TABLE_PLATFORM+"."+KEY_ID+"="+DATABASE_TABLE_SENSOR+"."+KEY_PLATFORMID+
				" AND "+DATABASE_TABLE_SENSOR+"."+KEY_ID+"="+DATABASE_TABLE_SUBSENSOR+"."+KEY_SENSORID+
				" ORDER BY "+DATABASE_TABLE_SUBSENSOR+"."+KEY_SENSORID+" ASC", null);
		curs.moveToFirst();
		return curs;
	}

	//returns subsensor-row with specified id
	public Cursor getSubsensor(long id) throws SQLException {
			return getRowById(id, DATABASE_TABLE_SUBSENSOR);
	}
		
	private void insertMeasurement(long subsensorId, float [] values, long [] timestamps) {
		ContentValues val;
		for (int i=0; i<values.length; i++) {
			val= new ContentValues();
			val.put(KEY_SUBSENSORID, subsensorId);
			val.put(KEY_VALUE, values[i]);
			val.put(KEY_TIMESTAMP, timestamps[i]);
			try {
				db.insert(DATABASE_TABLE_MEASUREMENT, null, val);
			} catch (SQLException e) {
				Log.w("dbwarning","something went wrong when inserting data");
				e.printStackTrace();
			}
		}
	}
	
	private long insertPhenomena( String unit, float min, float max)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_UNIT, unit);
		cont.put(KEY_MIN, min);
		cont.put(KEY_MAX, max);
		return db.insert(DATABASE_TABLE_PHENOMENA, null, cont);
		
	}

	//--------------------------------------------------------------------------------------------------------------------------------
	//methods for insertion into database
	private long insertPlatform(int lat, int lon,int period, String mobileNo, String descr)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_LAT, lat);
		cont.put(KEY_LON, lon);
		cont.put(KEY_PERIOD, period);
		cont.put(KEY_MOBILENO, mobileNo);
		cont.put(KEY_DESCR, descr);
		return db.insert(DATABASE_TABLE_PLATFORM, null, cont);
	}
	
	//this inserts a platform, 4sensors, 9 subsensors in a default manner
	public long insertPlatformDefault(int lat, int lon, int period, String mobileNo,String descr) {
		long platformId = insertPlatform(lat, lon, period, mobileNo,descr);
		for (int i=0;i<4;i++)
			{
			long sensorId=insertSensor(0, 0, 0,(int) platformId);
			insertSubsensor(1, (int) sensorId);				//moisture subsensor
			if (i==3) insertSubsensor(3,(int) sensorId);			//voltage subsensor, becomes subsensor of sensor[3]
			}
		for (int i=0;i<4;i++)
			{
			long sensorId=insertSensor(0, 0, 0,(int) platformId);
			insertSubsensor(2, (int) sensorId);				//temperature subsensor
			if (i==3) insertSubsensor(3,(int) sensorId);//voltage subsensor, becomes subsensor of sensor[3]	
			}
		return platformId;
	}
	
	private long insertSensor( int offX, int offY, int offZ, long platform_id) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		cont.put(KEY_PLATFORMID, platform_id);
		return db.insert(DATABASE_TABLE_SENSOR, null, cont);
	}
	
	private long insertSubsensor( long phenomenaId, long sensorId)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_SENSORID, sensorId);
		cont.put(KEY_PHENOMENAID, phenomenaId);
		return db.insert(DATABASE_TABLE_SUBSENSOR, null, cont);
	}
	
	public DatabaseControl open() throws SQLException{
		db=dbHelper.getWritableDatabase();
		setPhenomenas();
		return this;
	}
	
	
	//expecting raw decoded data with the rowcount being the number of measurements and the number of columns being the number of sensors
	// the incoming data will be resized with "factor"
	//inserts the 9 rows of Measurements for the given platform, calculates timestamps from periods 
	public void putMeasurements(int [][] data, float factor, long platformId) {
		int rowCount=data.length;
		int columnCount=data[1].length;
		
		// generating timestamps
		long[] times = new long[rowCount];
		Calendar c = Calendar.getInstance();				//not sure about use in different timezones, might change timestamps to local times
		c.set(data[0][1]+2000, data[0][2]-1, data[0][3], data[0][4], data[0][5]);
		long timestamp=c.getTimeInMillis();
		long timeOffset = data[0][0];			//offset in minutes
		for (int i = 0; i < rowCount-1; i++) {
			times[i] = timestamp + timeOffset * i*60*1000;		//timestamps are in millis
		}
		
		Cursor curs=getSubsenorsByPlatform(platformId);
		for (int i =0; i<columnCount; i++) {
		float [] values= new float[rowCount-1];	//1st row is anchor
			for (int j=1; j<rowCount;j++) {
				values[j-1]=data[j][i] * factor;	// resizing with factor, column becomes row to be inserted into database
			}
			insertMeasurement((int) curs.getLong(0),values,times);
			curs.moveToNext();
		}
	}
	
	//looks up a platform with the passed number, if there are more than one, returns the one, with the most recent measurements
	public long putPlatform(String mobileNo) {
		Cursor curs = db.query(DATABASE_TABLE_PLATFORM, new String[] {KEY_ID}, KEY_MOBILENO+"='"+mobileNo+"'", null, null, null,null);
		long result = -1; // case if cursor is empty
		if (!curs.moveToFirst()) return result;
		if (curs.getCount()==1) {
			try {
			result= curs.getLong(0);
		} catch (CursorIndexOutOfBoundsException e) {
			e.printStackTrace();
			return result;		//-1
		}
		} else 	if (curs.getCount()>2) {  // multiple to choose from, get most recent
			curs = db.rawQuery("SELECT "+ DATABASE_TABLE_PLATFORM+"."+KEY_ID+
					" FROM "+DATABASE_TABLE_PLATFORM+","+DATABASE_TABLE_SENSOR+","+DATABASE_TABLE_SUBSENSOR+","+DATABASE_TABLE_MEASUREMENT+","+DATABASE_TABLE_PHENOMENA+
					" ON "+DATABASE_TABLE_PLATFORM+"."+KEY_ID+"="+DATABASE_TABLE_SENSOR+"."+KEY_PLATFORMID+
					" AND "+DATABASE_TABLE_SENSOR+"."+KEY_ID+"="+DATABASE_TABLE_SUBSENSOR+"."+KEY_SENSORID+
					" AND "+DATABASE_TABLE_SUBSENSOR+"."+KEY_ID+"="+DATABASE_TABLE_MEASUREMENT+"."+KEY_SUBSENSORID+
					" AND "+DATABASE_TABLE_PHENOMENA+"."+KEY_UNIT+"= 'V'"+
					" AND "+DATABASE_TABLE_SUBSENSOR+"."+KEY_PHENOMENAID+"="+DATABASE_TABLE_PHENOMENA+"."+KEY_ID+
					" ORDER BY "+DATABASE_TABLE_PLATFORM+"."+KEY_ID+ " DESC LIMIT 1"
					,null);
			curs.moveToFirst();
			try {
			result= curs.getLong(0);
			} catch (CursorIndexOutOfBoundsException e) {
				e.printStackTrace();
				return result;		//-1
			}
		}
		
		return result;
	}
			
	//makes sure Phenomenas are set, Moisture is the first, Temperature 2nd, 3rd voltage
	private void setPhenomenas() {
		Cursor curs = db.query(DATABASE_TABLE_PHENOMENA, new String[] { KEY_UNIT }, null, null, null, null, null);
		if (curs.getCount()==0) {
			insertPhenomena("\u0025", 0, 100);			//id:1
			insertPhenomena("\u00B0 C", -50, 80);		//id:2
			insertPhenomena("V", 0, 8);					//id:3
		}
	}
	//TODO  test creating database, fill with some test data
	
	public DatabaseControl updateDatabase(InputStream inp) {
		db=dbHelper.updateDatabase(inp);
		return this;
	}
	
	public boolean updatePhenomena(long id, String unit, float min, float max, long sensorId)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_ID, id);
		cont.put(KEY_UNIT, unit);
		cont.put(KEY_MIN, min);
		cont.put(KEY_MAX, max);
		return db.update(DATABASE_TABLE_PHENOMENA, cont, KEY_ID + "=" + id, null) > 0;
	}
	
	public boolean updatePlatform(long id, int lon, int lat,int period, String mobileNo, String descr) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_LAT, lat);
		cont.put(KEY_LON, lon);
		cont.put(KEY_PERIOD, period);
		cont.put(KEY_MOBILENO, mobileNo);
		cont.put(KEY_DESCR, descr);
		return db.update(DATABASE_TABLE_PLATFORM, cont, KEY_ID + "=" + id, null) > 0;
	}
		
	public boolean updateSensor(long id, int offX, int offY, int offZ, long platform_id) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		cont.put(KEY_PLATFORMID, platform_id);
		return db.update(DATABASE_TABLE_SENSOR, cont, KEY_ID + "=" + id, null) > 0;
	}
	
	public boolean updateSubsensor(long id, long phenomenaId, long sensorId)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_SENSORID, sensorId);
		cont.put(KEY_PHENOMENAID, phenomenaId);
		return db.update(DATABASE_TABLE_SUBSENSOR, cont, KEY_ID + "=" + id, null) > 0;
	}
}

