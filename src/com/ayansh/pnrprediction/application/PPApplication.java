/**
 * 
 */
package com.ayansh.pnrprediction.application;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.varunverma.CommandExecuter.Command;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * @author Varun Verma
 * 
 */
public class PPApplication {

	private static PPApplication app;
	public static final String TAG = "PNR";
	public final static String SenderId = "492119277184";

	private Context context;
	private Command executingCommand;
	private PPApplicationDB appDB;
	private ArrayList<PNR> pnrList;

	HashMap<String, String> Options;

	public static PPApplication getInstance() {

		if (app == null) {
			app = new PPApplication();
		}

		return app;
	}

	/**
	 * 
	 */
	private PPApplication() {

		Options = new HashMap<String, String>();
	}

	public void setContext(Context c) {

		if (context == null) {

			context = c;

			// Initialize the DB.
			appDB = PPApplicationDB.getInstance(context);
			appDB.openDBForWriting();
			appDB.loadOptions();
		}
	}

	public void setExecutingCommand(Command c) {

		executingCommand = c;

	}

	public Context getContext() {
		return context;
	}

	public Command getExecutingCommand() {
		return executingCommand;
	}

	// Get all Options
	public HashMap<String, String> getOptions() {
		return Options;
	}

	public boolean isEULAAccepted() {

		String eula = Options.get("EULA");
		if (eula == null || eula.contentEquals("")) {
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
		context = null;
	}

	// Add parameter
	public boolean addParameter(String paramName, String paramValue) {

		List<String> queries = new ArrayList<String>();
		String query = "";

		if (Options.containsKey(paramName)) {
			// Already exists. Update it.
			query = "UPDATE Options SET ParamValue = '" + paramValue
					+ "' WHERE ParamName = '" + paramName + "'";
		} else {
			// New entry. Create it
			query = "INSERT INTO Options (ParamName, ParamValue) VALUES ('"
					+ paramName + "','" + paramValue + "')";
		}

		queries.add(query);
		boolean success = appDB.executeQueries(queries);

		if (success) {
			Options.put(paramName, paramValue);
		}

		return success;

	}

	public boolean removeParameter(String paramName) {

		List<String> queries = new ArrayList<String>();

		String query = "DELETE FROM Options WHERE ParamName = '" + paramName
				+ "'";
		queries.add(query);
		boolean success = appDB.executeQueries(queries);

		if (success) {
			Options.remove(paramName);
		}

		return success;
	}

	public int getOldAppVersion() {
		String versionCode = Options.get("AppVersionCode");
		if (versionCode == null || versionCode.contentEquals("")) {
			versionCode = "0";
		}
		return Integer.valueOf(versionCode);
	}

	public void updateVersion() {
		// Update Version

		int version;
		try {
			version = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			version = 0;
			Log.e(TAG, e.getMessage(), e);
		}

		addParameter("AppVersionCode", String.valueOf(version));
	}

	public void registerAppForGCM() {

		// Register this app
		String regId1 = Options.get("RegistrationId");
		String regId2 = GCMRegistrar.getRegistrationId(context);

		if (regId1 == null || regId1.contentEquals("") || regId2 == null
				|| regId2.contentEquals("")) {
			// Application is not registered
			Log.v(TAG, "Registering app with GCM");

			// Remove parameters
			removeParameter("RegistrationId");
			removeParameter("RegistrationStatus");

			// Register
			GCMRegistrar.register(context, SenderId);
		}

	}

	public List<PNR> getPNRList(boolean reload) {
		
		if(reload){
			
			return getPNRList();
			
		}
		else{
			
			return pnrList;
			
		}
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private List<PNR> getPNRList() {

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

		ArrayList<PNR> list = appDB.getPNRList();

		pnrList = new ArrayList<PNR>();

		for (int i = 0; i < list.size(); i++) {

			PNR pnr = list.get(i);

			try {

				Date travelDate = sdf.parse(pnr.getTravelDate());
				long t = travelDate.getTime() + 24*60*60*1000;
				travelDate.setTime(t);
				
				Date now = new Date();

				if (now.compareTo(travelDate) < 0) {
					// This is in future. Keep this.
					pnrList.add(pnr);
				} else {
					// Remove this
					appDB.deletePNR(pnr.getPnr());
				}

			} catch (Exception e) {

			}

		}
		
		// Order by Travel Date
		Collections.sort(pnrList, PNR.SortByTravelDate);
		
		return pnrList;

	}

}