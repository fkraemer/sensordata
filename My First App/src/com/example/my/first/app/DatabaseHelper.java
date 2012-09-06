package com.example.my.first.app;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME="localsensorDB";
	private final static int DATABASE_VERSION=1;
	
	private static final String DATABASE_CREATAE="create table if not exists...."; //TODO
	
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(DATABASE_CREATAE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldT, int newT) {
		Log.w("dberror","upgrading from " + oldT + " to "+ newT + ". this drops all data");
		db.execSQL("DROP TABLE IF EXISTS items"); // normally copy first, drop then
	
	}

}
