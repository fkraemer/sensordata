package com.example.my.first.app;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
//**************************************************************************************************************************
//*************************************************************************************************************************

	public DatabaseControl open() throws SQLException{
		db=dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		dbHelper.close();
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
		
	public long [] insertMeasurement(int subsensorId, float [] values, long [] timestamps) {
		ContentValues val;
		long [] result = new long[values.length];
		for (int i=0; i<values.length; i++) {
			val= new ContentValues();
			val.put(KEY_SUBSENSORID, subsensorId);
			val.put(KEY_VALUE, values[i]);
			val.put(KEY_TIMESTAMP, timestamps[i]);
			try {
			result[i]= db.insert(DATABASE_TABLE_MEASUREMENT, null, val);
			} catch (SQLException e) {
				Log.w("dberror","tried to write value with wrong format (####.#)");
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public Cursor getMeasurement() {
		return db.query(DATABASE_TABLE_MEASUREMENT,new String[] {KEY_ID,KEY_VALUE,KEY_TIMESTAMP},
				null,null,null,null,null);
	}
}

