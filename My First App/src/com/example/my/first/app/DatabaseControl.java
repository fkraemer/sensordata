package com.example.my.first.app;


import java.util.Calendar;

import com.example.sensor.data.DataSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;


	
public class DatabaseControl {



	public static final String KEY_ID = "id";
	
	private final static String DATABASE_TABLE_PLATFORM="platform";	// has KEY_ID
	public static final String KEY_LAT= "lat";
	public static final String KEY_LON= "lon";
	public static final String KEY_ELEV= "elev";
	public static final String KEY_PERIOD= "period";
	public static final String KEY_MOBILENO= "mobile_no";


	private final static String DATABASE_TABLE_SENSOR="sensor";	// has KEY_ID
	public static final String KEY_OFFY= "lat_offset";
	public static final String KEY_OFFX= "long_offset";
	public static final String KEY_OFFZ= "elev_offset";
	public static final String KEY_PLATFORMID= "platform_id";


	private final static String DATABASE_TABLE_SUBSENSOR="subsensor";	// has KEY_ID
	public static final String KEY_SENSORID= "sensor_id";
	public static final String KEY_PHENOMENAID= "phenomena_id";


	private final static String DATABASE_TABLE_MEASUREMENT="measurement";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_VALUE= "value";
	public static final String KEY_SUBSENSORID= "subsensor_id";


	private final static String DATABASE_TABLE_PHENOMENA="phenomena";	// has KEY_ID
	public static final String KEY_UNIT= "unit";
	public static final String KEY_MIN = "min";
	public static final String KEY_MAX = "max";
	
	public static final String ENGINE="InnoDB"; 
	
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	public DatabaseControl(Context cx) {
		dbHelper= new DatabaseHelper(cx);
	}
	
/**
***************************************************************************************************************************
 * nested class DatabaseHelper:
 * 
 *
*****************************************************************************************************************************
 */
private static class DatabaseHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME="localsensorDB";
	private final static int DATABASE_VERSION=1;
	
	private static final String PLATFORM_CREATE="CREATE  TABLE IF NOT EXISTS " + DATABASE_TABLE_PLATFORM + " ( " +
			KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+ KEY_LAT +" INTEGER  ,  "+ KEY_LON +" INTEGER  ,  "+ KEY_ELEV +
			" INTEGER  , "+ KEY_PERIOD +" INTEGER  ," + KEY_MOBILENO +" TEXT NOT NULL  )"; 
	
	private static final String SENSOR_CREATE="CREATE  TABLE IF NOT EXISTS  "+ DATABASE_TABLE_SENSOR +
			"(   "+ KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT ,  "+ KEY_OFFY +" INTEGER  , "+ KEY_OFFX +" INTEGER  , " + KEY_OFFZ +
			" INTEGER  ,  "+ KEY_PLATFORMID +" INTEGER NOT NULL ,    FOREIGN KEY ( "+ KEY_PLATFORMID +
			" )    REFERENCES "+ DATABASE_TABLE_SENSOR +" ("+ KEY_ID +" )    ON DELETE CASCADE    ON UPDATE CASCADE)"; 
	
	private static final String SUBSENSOR_CREATE="CREATE  TABLE  IF NOT EXISTS "+DATABASE_TABLE_SUBSENSOR+" ( " + 
			KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT , "+ KEY_SENSORID +" INTEGER NOT NULL , "+ KEY_PHENOMENAID +" INTEGER NOT NULL ," +
			"    FOREIGN KEY ("+ KEY_SENSORID +" )    REFERENCES "+ DATABASE_TABLE_SENSOR +" ("+ KEY_ID +" )    ON DELETE CASCADE "+
			"    ON UPDATE CASCADE    FOREIGN KEY ("+ KEY_PHENOMENAID +
			" )  REFERENCES "+DATABASE_TABLE_PHENOMENA +" ("+ KEY_ID +" )    ON DELETE CASCADE    ON UPDATE CASCADE)";

	private static final String PENOMENA_CREATE="CREATE  TABLE IF NOT EXISTS "+ DATABASE_TABLE_PHENOMENA +" ("+ KEY_ID +
			" INTEGER  PRIMARY KEY AUTOINCREMENT ,  "+KEY_UNIT+" TEXT, "+ KEY_MIN + " INT(4,1)  ," +KEY_MAX +
			" INT(4,1) )";

	private static final String MEASUREMENT_CREATE="CREATE  TABLE IF NOT EXISTS 	"+ DATABASE_TABLE_MEASUREMENT +" ( " +
			 KEY_TIMESTAMP +" DATE ,  "+ KEY_VALUE +" DECIMAL(4,1)  , "+KEY_SUBSENSORID +"  INTEGER  ," +
			"   FOREIGN KEY ("+ KEY_SUBSENSORID +" )    REFERENCES "+ DATABASE_TABLE_SUBSENSOR +" ("+ KEY_ID +" )" +
			"    ON DELETE CASCADE    ON UPDATE CASCADE PRIMARY KEY ( "+KEY_TIMESTAMP +" , "+KEY_SUBSENSORID +" ))";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

		@Override
	public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(PLATFORM_CREATE);
				db.execSQL(SENSOR_CREATE);
				db.execSQL(SUBSENSOR_CREATE);
				db.execSQL(MEASUREMENT_CREATE);
				db.execSQL(PENOMENA_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldT, int newT) {
		Log.w("dberror","upgrading from " + oldT + " to "+ newT + ". this drops all data");
		db.execSQL("DROP TABLE IF EXISTS platform"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS sensor"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS subsensor"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS measuremen"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS phenomena"); // normally copy first, drop then
		this.onCreate(db);
		}
	}	


//***********************************************************************************************************************
//*************************************************************************************************************************

	public DatabaseControl open() throws SQLException{
		db=dbHelper.getWritableDatabase();
		setPhenomenas();
		return this;
	}
	
	public void close() {
		dbHelper.close();
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
	//insert phenomenas by default
	// create inserting methods
	//create query methods. using query, rawquery or sqlitequerybuilder
	
	//helper-method for getting rows of all tables with Ids
	public Cursor getRowById(int id, String selectedTable) throws SQLException {
		Cursor myCursor = db.query(selectedTable, null,
				KEY_ID + "=" + id,null,null,null,null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	
	//looks up a platform with the passed number, if there are more than one, returns the one, with the most recent measurements
	public long putPlatform(String mobileNo) {
		Cursor curs = db.query(DATABASE_TABLE_PLATFORM, new String[] {KEY_ID}, KEY_MOBILENO+"='"+mobileNo+"'", null, null, null,null);
		long result = -1; // case cursor is empty
		if (curs.getCount()==1) {
			result= curs.getLong(0);
		} else 	if (curs.getCount()>1) {  // multiple to choose from, get most recent
			curs = db.rawQuery("SELECT "+ DATABASE_TABLE_PLATFORM+"."+KEY_ID+
					" FROM "+DATABASE_TABLE_PLATFORM+","+DATABASE_TABLE_SENSOR+","+DATABASE_TABLE_SUBSENSOR+","+DATABASE_TABLE_MEASUREMENT+","+DATABASE_TABLE_PHENOMENA+
					" ON "+","+DATABASE_TABLE_PLATFORM+"."+KEY_ID+"="+DATABASE_TABLE_SENSOR+"."+KEY_PLATFORMID+
					" AND "+DATABASE_TABLE_SENSOR+"."+KEY_ID+"="+DATABASE_TABLE_SUBSENSOR+"."+KEY_SENSORID+
					" AND "+DATABASE_TABLE_SUBSENSOR+"."+KEY_ID+"="+DATABASE_TABLE_MEASUREMENT+"."+KEY_SUBSENSORID+
					" AND "+DATABASE_TABLE_PHENOMENA+"."+KEY_UNIT+"= 'V'"+
					" AND "+DATABASE_TABLE_SUBSENSOR+"."+KEY_PHENOMENAID+"="+DATABASE_TABLE_PHENOMENA+"."+KEY_ID+
					" ORDER BY "+DATABASE_TABLE_PLATFORM+"."+KEY_ID+ " DESC LIMIT 1"
					,null);
			curs.moveToFirst();
			result= curs.getLong(0);
		}
		
		return result;
	}
	
	//this inserts a platform, 4sensors, 9 subsensors in a default manner,
	//with all Metadata set to 0 except period and mobileNo, which are the minimum required data
	// for the database to work properly
	public long insertPlatformToSubsensorWithoutMetadata(int period, String mobileNo) {
		long platformId = insertPlatform(0, 0, 0, period, mobileNo);
		for (int i=0;i<4;i++)
			{
			long sensorId=insertSensor(0, 0, 0,(int) platformId);
			insertSubsensor(1, (int) sensorId);				//moisture subsensor
			insertSubsensor(2, (int) sensorId);				//temperature subsensor
			if (i==0) insertSubsensor(3,(int) sensorId);			//voltage subsensor, becomes subsensor of sensor[0]
			}
		return platformId;
	}

	//expecting raw decoded data with the rowcount being the number of measurements and the number of columns being the number of sensors
	// the incoming data is considered to be too big by the factor of 10
	//inserts the 9 rows of Measurements for the given platform, calculates timestamps from periods 
	public void putMeasurements(int [][] data, int platformId) {
		int rowCount=data.length;
		int columnCount=data[1].length;
		
		// generating timestamps
		long[] times = new long[rowCount];
		Calendar c = Calendar.getInstance();				//not sure about use in different timezones, might change timestamps to local times
		c.set(data[0][0]+2000, data[0][1]-1, data[0][2], data[0][3], data[0][4]);
		long timestamp=c.getTimeInMillis();
		Cursor curs = getPlatform(platformId);			 //30min offset by default
		long timeOffset = curs.getLong(curs.getColumnIndex(KEY_PERIOD));		//period MUST be set at this point
		for (int i = 0; i < rowCount; i++) {
			times[i] = timestamp + timeOffset * i;
		}
		
		//get necessary Ids
		//for this cursor:expecting to be [0] moist of sensor[0], [1] temp of sensor[0] and so on and [8] the voltage of sensor[0]
		curs = db.rawQuery("SELECT "+ DATABASE_TABLE_SUBSENSOR+"."+KEY_ID+
				" FROM "+DATABASE_TABLE_PLATFORM+","+DATABASE_TABLE_SENSOR+","+DATABASE_TABLE_SUBSENSOR+","+","+
				" ON "+","+DATABASE_TABLE_PLATFORM+"."+KEY_ID+"="+DATABASE_TABLE_SENSOR+"."+KEY_PLATFORMID+
				" AND "+DATABASE_TABLE_SENSOR+"."+KEY_ID+"="+DATABASE_TABLE_SUBSENSOR+"."+KEY_SENSORID, null);
		for (int i =0; i<9; i++) {
		float [] values= new float[columnCount-1];	//1st row is anchor
			for (int j=0; j<columnCount-1;j++) {
				values[j]=data[j][i] /10;	// dividing by 10! column becomes row to be inserted into database
			}
			insertMeasurement((int) curs.getLong(0),values , times);
		}
	}
	
	
	public Cursor getMeasurementsInterval(long minTime, long maxTime, long subSensorId) {
		return db.rawQuery("SELECT "+ DATABASE_TABLE_MEASUREMENT+"."+KEY_TIMESTAMP+" , "+DATABASE_TABLE_MEASUREMENT+"."+KEY_VALUE+
				" FROM "+DATABASE_TABLE_PLATFORM+","+DATABASE_TABLE_SUBSENSOR+","+
				" ON "+","+DATABASE_TABLE_SUBSENSOR+"."+KEY_ID+"="+subSensorId+
				" WHERE "+DATABASE_TABLE_PLATFORM+"."+KEY_TIMESTAMP+">="+Long.toString(minTime)+
				" AND "+DATABASE_TABLE_PLATFORM+"."+KEY_TIMESTAMP+"<="+Long.toString(maxTime)+
				" ORDER BY"+DATABASE_TABLE_PLATFORM+"."+KEY_TIMESTAMP+" ASC", null);
	}
	
	
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//methods for insertion into database
	public long insertPlatform(int lat, int lon, int elev,int period, String mobileNo)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_LAT, lat);
		cont.put(KEY_LON, lon);
		cont.put(KEY_ELEV, elev);
		cont.put(KEY_PERIOD, period);
		cont.put(KEY_MOBILENO, mobileNo);
		return db.insert(DATABASE_TABLE_PLATFORM, null, cont);
	}
	
	public boolean updatePlatform(int id, int lat, int lon, int elev,int period, long mobileNo) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_ID, id);
		cont.put(KEY_LAT, lat);
		cont.put(KEY_LON, lon);
		cont.put(KEY_ELEV, elev);
		cont.put(KEY_PERIOD, period);
		cont.put(KEY_MOBILENO, mobileNo);
		return db.update(DATABASE_TABLE_PLATFORM, cont, KEY_ID + "+" + id, null) > 0;
	}

	public boolean deletePLATFORM(int id) {
		return db.delete(DATABASE_TABLE_PLATFORM, KEY_ID + "=" + id,null) > 0;
	}
	
	
	
	
	//returns platform-row with specified id
	public Cursor getPlatform(int id) throws SQLException {
		return getRowById(id, DATABASE_TABLE_PLATFORM);
	}
	
	
	
	public long insertSensor( int offX, int offY, int offZ, int platform_id) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		cont.put(KEY_PLATFORMID, platform_id);
		return db.insert(DATABASE_TABLE_SENSOR, null, cont);
	}

	public boolean updateSensor(int id, float offX, float offY, float offZ, int platform_id) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_ID, id);
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		cont.put(KEY_PLATFORMID, platform_id);
		return db.update(DATABASE_TABLE_SENSOR, cont, KEY_ID + "+" + id, null) > 0;
	}
	
	
	public boolean deleteSensor(int id) {
		return db.delete(DATABASE_TABLE_SENSOR, KEY_ID + "=" + id,null) > 0;
	}
	
	//returns sensor-row with specified id
	public Cursor getSensor(int id) throws SQLException {
		return getRowById(id, DATABASE_TABLE_SENSOR);
	}
	

	public long insertSubsensor( int phenomenaId, int sensorId)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_SENSORID, sensorId);
		cont.put(KEY_PHENOMENAID, phenomenaId);
		return db.insert(DATABASE_TABLE_SUBSENSOR, null, cont);
	}
	
	public boolean updateSubsensor(int id, int phenomenaId, int sensorId)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_ID, id);
		cont.put(KEY_SENSORID, sensorId);
		cont.put(KEY_PHENOMENAID, phenomenaId);
		return db.update(DATABASE_TABLE_SUBSENSOR, cont, KEY_ID + "+" + id, null) > 0;
	}
	
	
	public boolean deleteSubsensor(int id) {
		return db.delete(DATABASE_TABLE_SUBSENSOR, KEY_ID + "=" + id,null) > 0;
	}
	
	//returns subsensor-row with specified id
		public Cursor getSubsensor(int id) throws SQLException {
			return getRowById(id, DATABASE_TABLE_SUBSENSOR);
	}
		
		
	public long insertPhenomena( String unit, float min, float max)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_UNIT, unit);
		cont.put(KEY_MIN, min);
		cont.put(KEY_MAX, max);
		return db.insert(DATABASE_TABLE_PHENOMENA, null, cont);
		
	}
	
	public boolean updatePhenomena(int id, String unit, float min, float max, int sensorId)
	{
		ContentValues cont = new ContentValues();
		cont.put(KEY_ID, id);
		cont.put(KEY_UNIT, unit);
		cont.put(KEY_MIN, min);
		cont.put(KEY_MAX, max);
		return db.update(DATABASE_TABLE_PHENOMENA, cont, KEY_ID + "+" + id, null) > 0;
	}
	
	public boolean deletePhenomena(int id) {
		return db.delete(DATABASE_TABLE_PHENOMENA, KEY_ID + "=" + id,null) > 0;
	}
	
	
	//returns phenomena-row with specified id
		public Cursor getPhenomena(int id) throws SQLException {
			return getRowById(id, DATABASE_TABLE_PHENOMENA);
	}
		
	public void insertMeasurement(int subsensorId, float [] values, long [] timestamps) {
		ContentValues val;
		//long [] result = new long[values.length];
		for (int i=0; i<values.length; i++) {
			val= new ContentValues();
			val.put(KEY_SUBSENSORID, subsensorId);
			val.put(KEY_VALUE, values[i]);
			val.put(KEY_TIMESTAMP, timestamps[i]);
			try {
			//result[i]= 
				db.insert(DATABASE_TABLE_MEASUREMENT, null, val);
			} catch (SQLException e) {
				Log.w("dbwarning","something went wrong when inserting data");
				e.printStackTrace();
			}
		}
		//return result;
	}
	
	public Cursor getMeasurement() {
		return db.query(DATABASE_TABLE_MEASUREMENT,new String[] {KEY_ID,KEY_VALUE,KEY_TIMESTAMP},
				null,null,null,null,null);
	}
}

