/**
 * 
 */
package com.ayansh.pnrprediction.application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
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
	private static final int dbVersion = 2;
	
	private static PPApplicationDB appDB;
	
	private PPApplication app;
	private SQLiteDatabase db;

	/**
	 * @param context
	 */
	private PPApplicationDB(Context c){
		
		super(c, dbName, null, dbVersion);
		app = PPApplication.getInstance();
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

		String createPNRTable = "CREATE TABLE PNR (" + 
				"PNR VARCHAR , " + // PNR No
				"TrainNo VARCHAR, "  +
				"TravelDate VARCHAR, "  +
				"TravelClass VARCHAR, "  +
				"CurrentStatus VARCHAR, "  +
				"FromStation VARCHAR, "  +
				"ToStation VARCHAR, "  +
				"CNFProbability REAL, "  +
				"RACProbability REAL, "  +
				"OptCNFProbability REAL, "  +
				"OptRACProbability REAL, "  +
				"ExpectedStatus VARCHAR, "  +
				"LastUpdate INTEGER, "  +
				"PRIMARY KEY (PNR)" +
				")";
		
		// create a new table - if not existing
		try {
			// Create Tables.
			Log.i(PPApplication.TAG,"Creating Tables for Version:" + String.valueOf(dbVersion));

			db.execSQL(createOptionsTable);
			db.execSQL(createPNRTable);

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

		switch(oldVersion){
		
		case 1:
			/*
			 * Adding a new table to save PNR details
			 */
			String createPNRTable = "CREATE TABLE PNR (" + 
					"PNR VARCHAR , " + // PNR No
					"TrainNo VARCHAR, "  +
					"TravelDate VARCHAR, "  +
					"TravelClass VARCHAR, "  +
					"CurrentStatus VARCHAR, "  +
					"FromStation VARCHAR, "  +
					"ToStation VARCHAR, "  +
					"CNFProbability REAL, "  +
					"RACProbability REAL, "  +
					"OptCNFProbability REAL, "  +
					"OptRACProbability REAL, "  +
					"ExpectedStatus VARCHAR, "  +
					"LastUpdate INTEGER, "  +
					"PRIMARY KEY (PNR)" +
					")";
			
			try {
				// Upgrading database to version 2
				Log.i(PPApplication.TAG, "Upgrading DB from version 1 to 2");
					
				db.execSQL(createPNRTable);
							
				Log.i(PPApplication.TAG, "Upgrade successfully");

			} catch (SQLException e) {
				// Oops !!
				Log.e(PPApplication.TAG, e.getMessage(), e);
			}
			
			/*
			 * Very important - No break statement here !
			 */
		
		}
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

	boolean checkPNRExists(String pnr) {
		
		String[] columns = {"PNR"};
		String selection = "PNR='" + pnr + "'";
		
		Cursor cursor = db.query("PNR", columns, selection, null, null, null, null);
		
		int count = cursor.getCount();
		cursor.close();
		
		if(count > 0){
			return true;
		}
		else{
			return false;
		}
		
	}

	@SuppressLint("SimpleDateFormat")
	ArrayList<PNR> getPNRList() {
		
		ArrayList<PNR> pnrList = new ArrayList<PNR>();
		
		String sql = "SELECT * FROM PNR";
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if(cursor.moveToFirst()){
			
			do{
				
				PNR pnr = new PNR(cursor.getString(cursor.getColumnIndex("PNR")));
				
				pnr.setTrainNo(cursor.getString(cursor.getColumnIndex("TrainNo")));
				pnr.setTravelDate(cursor.getString(cursor.getColumnIndex("TravelDate")));
				pnr.setTrainClass(cursor.getString(cursor.getColumnIndex("TravelClass")));
				pnr.setCurrentStatus(cursor.getString(cursor.getColumnIndex("CurrentStatus")));
				pnr.setFromStation(cursor.getString(cursor.getColumnIndex("FromStation")));
				pnr.setToStation(cursor.getString(cursor.getColumnIndex("ToStation")));
				pnr.setCnfProb(cursor.getFloat(cursor.getColumnIndex("CNFProbability")));
				pnr.setRacProb(cursor.getFloat(cursor.getColumnIndex("RACProbability")));
				pnr.setOptCNFProb(cursor.getFloat(cursor.getColumnIndex("OptCNFProbability")));
				pnr.setOptRACProb(cursor.getFloat(cursor.getColumnIndex("OptRACProbability")));
				pnr.setExpectedStatus(cursor.getString(cursor.getColumnIndex("ExpectedStatus")));
				pnr.setLastUpdate(cursor.getLong(cursor.getColumnIndex("LastUpdate")));
				
				pnrList.add(pnr);
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return pnrList;
		
	}

	void deletePNR(String pnr) {
		
		String where = "PNR='" + pnr + "'";
		
		db.delete("PNR", where, null);
		
	}

}
