package com.example.my.first.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


	
public class DatabaseControl {


	private final static String DATABASE_TABLE_SENSORS="description";
	public static final String KEY_SENSORID = "sensor_id";
	public static final String KEY_OFFX= "offset_x";
	public static final String KEY_OFFY= "offset_y";
	public static final String KEY_OFFZ= "offset_z";
	
	private final static String DATABASE_TABLE_VALUES="items";
	public static final String KEY_ROWID = "id";
	public static final String KEY_VALUE= "value";
	public static final String KEY_TIMESTAMP = "timestamp";

	
	private final Context cx;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	public DatabaseControl(Context cx) {
		this.cx=cx;
		dbHelper= new DatabaseHelper(cx);
	}

	public DatabaseControl open() throws SQLException{
		db=dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public long insertSensor(short id, float offX, float offY, float offZ) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_SENSORID, id);
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		return db.insert(DATABASE_TABLE_SENSORS, null, cont);
	}
	
	public boolean deleteSensor(long sensorId) {
		return db.delete(DATABASE_TABLE_SENSORS, KEY_SENSORID + "=" + sensorId,null) > 0;
	}
	
	public Cursor getSensor(long rowId) throws SQLException {
		Cursor myCursor = db.query(DATABASE_TABLE_SENSORS, new String[] {KEY_SENSORID, KEY_OFFX, KEY_OFFY, KEY_OFFZ},
				KEY_SENSORID + "=" + rowId,null,null,null,null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	
	public boolean updateSensor(long rowId,short id, float offX, float offY, float offZ) {
		ContentValues cont = new ContentValues();
		cont.put(KEY_SENSORID, id);
		cont.put(KEY_OFFX, offX);
		cont.put(KEY_OFFY, offY);
		cont.put(KEY_OFFZ, offZ);
		return db.update(DATABASE_TABLE_SENSORS, cont, KEY_SENSORID + "+" + rowId, null) > 0;
	}
	
	public long [] insertItem(float [] values, long [] timestamps) {
		ContentValues val= new ContentValues();
		long [] result = new long[values.length];
		for (int i=0; i<values.length; i++) {
			val.put(KEY_VALUE, values[i]);
			val.put(KEY_TIMESTAMP, timestamps[i]);
			result[i]= db.insert(DATABASE_TABLE_VALUES, null, val);
		}
		return result;  //TODO really necessary ???
	}
	
	public boolean deleteItem(long rowId) {
		return db.delete(DATABASE_TABLE_VALUES, KEY_ROWID + "=" + rowId,null) > 0;
	}
	
	public Cursor getItems() {
		return db.query(DATABASE_TABLE_VALUES,new String[] {KEY_ROWID,KEY_VALUE,KEY_TIMESTAMP},
				null,null,null,null,null);
	}
}
