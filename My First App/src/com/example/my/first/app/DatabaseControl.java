package com.example.my.first.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


	
public class DatabaseControl {



	public static final String KEY_ID = "id";
	
	private final static String DATABASE_TABLE_PLATFORM="`platform`";	// has KEY_ID
	public static final String KEY_LAT= "`lat`";
	public static final String KEY_LONG= "`long`";
	public static final String KEY_ELEV= "`elev`";
	public static final String KEY_MOBILENO= "`mobile_no`";


	private final static String DATABASE_TABLE_SENSOR="`sensor`";	// has KEY_ID
	public static final String KEY_OFFY= "`lat_offset`";
	public static final String KEY_OFFX= "`long_offset`";
	public static final String KEY_OFFZ= "`elev_offset`";
	public static final String KEY_PLATFORMID= "`platform_id`";


	private final static String DATABASE_TABLE_SUBSENSOR="`subsensor`";	// has KEY_ID
	public static final String KEY_SENSORID= "`sensor_id`";
	public static final String KEY_PHENOMENAID= "`phenomena_id`";


	private final static String DATABASE_TABLE_MEASUREMENT="`measurement`";
	public static final String KEY_TIMESTAMP = "`timestamp`";
	public static final String KEY_VALUE= "`value`";
	public static final String KEY_SUBSENSORID= "`subsensor_id`";


	private final static String DATABASE_TABLE_PHENOMENA="`phenomena`";	// has KEY_ID
	public static final String KEY_UNIT= "`unit`";
	public static final String KEY_MIN = "`min`";
	public static final String KEY_MAX = "`max`";
	
	public static final String ENGINE="InnoDB"; 
	
	
	private final Context cx;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	public DatabaseControl(Context cx) {
		this.cx=cx;
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
	
	private static final String PLATFORM_CREATE="CREATE  TABLE IF NOT EXISTS " + DATABASE_TABLE_PLATFORM + " ( +" +
			KEY_ID+" INT NOT NULL AUTO_INCREMENT ,  "+ KEY_LAT +" INT NULL ,  "+ KEY_LAT +" INT NULL ,  "+ KEY_ELEV +
			"INT NULL ," + KEY_MOBILENO +" VARCHAR(45) NULL ,  PRIMARY KEY ("+ KEY_ID +") )ENGINE = "+ENGINE; 
	
	private static final String SENSOR_CREATE="CREATE  TABLE IF NOT EXISTS "+ DATABASE_TABLE_SENSOR +
			"( +  "+ KEY_ID +" INT NOT NULL AUTO_INCREMENT ,  "+ KEY_OFFY +" INT NULL , "+ KEY_OFFX +" INT NULL ," +
			KEY_OFFZ +" INT NULL ,  "+ KEY_PLATFORMID +" INT NOT NULL ,  PRIMARY KEY ("+ KEY_ID +") ," +
			"  INDEX `fk_sensor_platform1_idx` ("+ KEY_PLATFORMID +" ASC) ,  CONSTRAINT `fk_sensor_platform1`" +
			"    FOREIGN KEY ("+ KEY_PLATFORMID +" )    REFERENCES "+ DATABASE_TABLE_SENSOR +" ("+ KEY_ID +" )    ON DELETE NO ACTION" +
			"    ON UPDATE NO ACTION)ENGINE = "+ENGINE; 
	
	private static final String SUBSENSOR_CREATE="CREATE  TABLE IF NOT EXISTS "+DATABASE_TABLE_SUBSENSOR+" ( " + 
			KEY_ID +" INT NOT NULL AUTO_INCREMENT , "+ KEY_SENSORID +" INT NOT NULL , "+ KEY_PHENOMENAID +" INT NOT NULL ," +
			"  PRIMARY KEY ("+ KEY_ID +") ,  INDEX `fk_subsensor_sensor1_idx` ("+ KEY_SENSORID +" ASC) , " +
			" INDEX `fk_subsensor_phenomena1_idx` ("+ KEY_PHENOMENAID +" ASC) ,  CONSTRAINT `fk_subsensor_sensor1`" +
			"    FOREIGN KEY ("+ KEY_SENSORID +" )    REFERENCES "+ DATABASE_TABLE_SENSOR +" ("+ KEY_ID +" )    ON DELETE NO ACTION" +
			"    ON UPDATE NO ACTION,  CONSTRAINT `fk_subsensor_phenomena1`    FOREIGN KEY ("+ KEY_PHENOMENAID +
			" )  REFERENCES "+DATABASE_TABLE_PHENOMENA +" ("+ KEY_ID +" )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = "+ENGINE;

	private static final String MEASUREMENT_CREATE="CREATE  TABLE IF NOT EXISTS "+ DATABASE_TABLE_MEASUREMENT +" ( " +
			 KEY_TIMESTAMP +" TIMESTAMP NOT NULL ,  "+ KEY_VALUE +" DECIMAL(4,1) NULL , "+KEY_SUBSENSORID +"  INT NOT NULL ," +
			"  PRIMARY KEY ("+ KEY_TIMESTAMP +", "+ KEY_SUBSENSORID +") ,  INDEX `fk_measurement_subsensor1_idx` ("+ KEY_SUBSENSORID +" ASC) ," +
			"  CONSTRAINT `fk_measurement_subsensor1`    FOREIGN KEY ("+ KEY_SUBSENSORID +" )    REFERENCES "+ DATABASE_TABLE_SUBSENSOR +" ("+ KEY_ID +" )" +
			"    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = "+ENGINE;
	
	private static final String PENOMENA_CREATE="CREATE  TABLE IF NOT EXISTS "+ DATABASE_TABLE_PHENOMENA +" (" +
			"  "+ KEY_ID +" INT NOT NULL AUTO_INCREMENT ,  "+KEY_UNIT+" VARCHAR(45) NULL , "+ KEY_MIN + " DECIMAL(4,1) NULL ," +
			KEY_MAX +" DECIMAL(4,1) NULL ,  PRIMARY KEY ("+ KEY_ID +") )ENGINE = "+ENGINE;
			
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			//*******************************************************************************************
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
		db.execSQL("DROP TABLE IF EXISTS `platform`"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS `sensor`"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS `subsensor`"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS `measurement`"); // normally copy first, drop then
		db.execSQL("DROP TABLE IF EXISTS `phenomena`"); // normally copy first, drop then
	
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
	
	public boolean insertData( int subsensorId, long [] timestamps, float [] data)
	{
		boolean flag=true;
		
		//for 
		return flag;
		
	}
	
	
	
	
	
	
	
	
	/**
	public long insertSensor( float offX, float offY, float offZ, int platform_id) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		cont.put(KEY_PLATFORMID, platform_id);
		return db.insert(DATABASE_TABLE_SENSOR, null, cont);
	}
	
	public boolean deleteSensor(long sensorId) {
		return db.delete(DATABASE_TABLE_SENSOR, KEY_ID + "=" + sensorId,null) > 0;
	}
	
	public Cursor getSensor(long ID) throws SQLException {
		Cursor myCursor = db.query(DATABASE_TABLE_SENSOR, new String[] {KEY_ID, KEY_OFFX, KEY_OFFY, KEY_OFFZ, KEY_PLATFORMID},
				KEY_ID + "=" + ID,null,null,null,null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	
	public boolean updateSensor(long ID,short id, float offX, float offY, float offZ, int platform_id) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_ID, id);
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		cont.put(KEY_PLATFORMID, platform_id);
		return db.update(DATABASE_TABLE_SENSOR, cont, KEY_ID + "+" + ID, null) > 0;
	}
	
	public long [] insertMeasurement(float [] values, long [] timestamps) {
		ContentValues val= new ContentValues();
		long [] result = new long[values.length];
		for (int i=0; i<values.length; i++) {
			val.put(KEY_VALUE, values[i]);
			val.put(KEY_TIMESTAMP, timestamps[i]);
			result[i]= db.insert(DATABASE_TABLE_MEASUREMENT, null, val);
		}
		return result;  //TODO really necessary ???
	}
	
	public boolean deleteMeasurement(long ID) {
		return db.delete(DATABASE_TABLE_MEASUREMENT, KEY_ID + "=" + ID,null) > 0;
	}
	
	public Cursor getMeasurement() {
		return db.query(DATABASE_TABLE_MEASUREMENT,new String[] {KEY_ID,KEY_VALUE,KEY_TIMESTAMP},
				null,null,null,null,null);
	} */
}

