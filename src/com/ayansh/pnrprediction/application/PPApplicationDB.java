/**
 * 
 */
package com.ayansh.pnrprediction.application;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author I041474
 *
 */
public class PPApplicationDB extends SQLiteOpenHelper {

	private static final String dbName = "PNR-Prediction";
	private static final int dbVersion = 1;
	
	private static PPApplicationDB appDB;
	
	private PPApplication app;
	private SQLiteDatabase db;

	/**
	 * @param context
	 */
	private PPApplicationDB(Context c){
		
		super(c, dbName, null, dbVersion);
		
	}
	
	static PPApplicationDB getInstance(Context c){
		
		if(appDB == null){
			appDB = new PPApplicationDB(c);
		}
		
		return appDB;
	}
	
	static PPApplicationDB getInstance(){
		return appDB;
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String createOptionsTable = "CREATE TABLE Options ("
				+ "ParamName VARCHAR(20), " + // Parameter Name
				"ParamValue VARCHAR(20)" + // Parameter Value
				")";

		// create a new table - if not existing
		try {
			// Create Tables.
			Log.i(PPApplication.TAG,"Creating Tables for Version:" + String.valueOf(dbVersion));

			db.execSQL(createOptionsTable);

			Log.i(PPApplication.TAG, "Tables created successfully");

		} catch (SQLException e) {
			Log.e(PPApplication.TAG, e.getMessage(), e);
		}				

	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	void openDBForWriting(){
		db = appDB.getWritableDatabase();
	}
	
synchronized void loadOptions(){
		
		if(!db.isOpen()){
			return;
		}
		
		// Load Parameters
		String name, value;
		Log.v(PPApplication.TAG, "Loading application Options");
		
		Cursor cursor = db.query("Options", null, null, null, null, null, null);
		
		if(cursor.moveToFirst()){
			name = cursor.getString(cursor.getColumnIndex("ParamName"));
			value = cursor.getString(cursor.getColumnIndex("ParamValue"));
			app.Options.put(name, value);
		}
		
		while(cursor.moveToNext()){
			name = cursor.getString(cursor.getColumnIndex("ParamName"));
			value = cursor.getString(cursor.getColumnIndex("ParamValue"));
			app.Options.put(name, value);
		}
		
		cursor.close();
		
	}

	synchronized boolean executeQueries(List<String> queries) {

		Iterator<String> iterator = queries.listIterator();
		String query;

		try {
			db.beginTransaction();

			while (iterator.hasNext()) {
				query = iterator.next();
				db.execSQL(query);
			}

			db.setTransactionSuccessful();
			db.endTransaction();
			
			return true;
			
		} catch (Exception e) {
			// Do nothing! -- Track the error causing query
			Log.e(PPApplication.TAG, e.getMessage(), e);
			db.endTransaction();
			return false;
		}
	}

}
