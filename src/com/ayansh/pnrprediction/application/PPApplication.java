/**
 * 
 */
package com.ayansh.pnrprediction.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.varunverma.CommandExecuter.Command;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
 * @author Varun Verma
 *
 */
public class PPApplication {

	private static PPApplication app;
	public static final String TAG = "PNR";
	
	private Context context;
	private Command executingCommand;
	private PPApplicationDB appDB;
	
	HashMap<String,String> Options;
	
	public static PPApplication getInstance(){
		
		if(app == null){
			app = new PPApplication();
		}
		
		return app;
	}
	
	/**
	 * 
	 */
	private PPApplication() {
		
		Options = new HashMap<String,String>();
	}

	public void setContext(Context c){
		
		if(context == null){
			
			context = c;
			
			// Initialize the DB.
			appDB = PPApplicationDB.getInstance(context);
			appDB.openDBForWriting();
			appDB.loadOptions();
		}
	}
	
	public void setExecutingCommand(Command c){
		
		executingCommand = c;
		
	}
	
	public Command getExecutingCommand(){
		return executingCommand;
	}
	
	// Get all Options
	public HashMap<String, String> getOptions() {
		return Options;
	}

	public boolean isEULAAccepted() {
		
		String eula = Options.get("EULA");
		if(eula == null || eula.contentEquals("")){
			eula = "false";
		}
		return Boolean.valueOf(Options.get("EULA"));
	}

	public void setEULAResult(boolean result) {
		// Save EULA Result
		addParameter("EULA", String.valueOf(result));	
	}

	public void close() {
		appDB.close();
	}

	// Add parameter
	public boolean addParameter(String paramName, String paramValue){
		
		List<String> queries = new ArrayList<String>();
		String query = "";
		
		if(Options.containsKey(paramName)){
			// Already exists. Update it.
			query = "UPDATE Options SET ParamValue = '" + paramValue + "' WHERE ParamName = '" + paramName + "'";
		}
		else{
			// New entry. Create it
			query = "INSERT INTO Options (ParamName, ParamValue) VALUES ('" + paramName + "','" + paramValue + "')";
		}
		
		queries.add(query);
		boolean success = appDB.executeQueries(queries);
		
		if(success){
			Options.put(paramName, paramValue);
		}
		
		return success;
		
	}
	
	public boolean removeParameter(String paramName){
		
		List<String> queries = new ArrayList<String>();
		
		String query = "DELETE FROM Options WHERE ParamName = '" + paramName + "'";
		queries.add(query);
		boolean success = appDB.executeQueries(queries);
		
		if(success){
			Options.remove(paramName);
		}
		
		return success;
	}

	public void incrementCounter(String counterName) {

		String counterValue = Options.get(counterName);
		if(counterValue == null){
			counterValue = "1";
		}
		else{
			int value = Integer.valueOf(counterValue);
			value++;
			counterValue = String.valueOf(value);
		}
		
		addParameter(counterName, counterValue);
		
	}
	
	public int getOldAppVersion() {
		String versionCode = Options.get("AppVersionCode");
		if(versionCode == null || versionCode.contentEquals("")){
			versionCode = "0";
		}
		return Integer.valueOf(versionCode);
	}
	
	public void updateVersion() {
		// Update Version
		
		int version;
		try {
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			version = 0;
			Log.e(TAG, e.getMessage(), e);
		}

		addParameter("AppVersionCode", String.valueOf(version));
	}

}