/**
 * 
 */
package com.ayansh.pnrprediction.application;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ResultObject;

import android.accounts.Account;
import android.accounts.AccountManager;

/**
 * @author I041474
 *
 */
public class CalculateProbabilityCommand extends Command {

	private String PNR, trainNo, travelDate, trainClass, currentStatus, fromStation, toStation;
	
	/**
	 * @param caller
	 */
	public CalculateProbabilityCommand(Invoker caller, String pnr, String tNo, String tDt, String tCl, String cSt, String fs, String ts) {
		
		super(caller);
		
		PNR = pnr;
		trainNo = tNo;
		travelDate = tDt;
		trainClass = tCl;
		currentStatus = cSt;
		fromStation = fs;
		toStation = ts;
	}

	/* (non-Javadoc)
	 * @see org.varunverma.CommandExecuter.Command#execute(org.varunverma.CommandExecuter.ResultObject)
	 */
	@Override
	protected void execute(ResultObject result) throws Exception {

		PPApplication app = PPApplication.getInstance();
		
		String url = "http://ayansh.com/pnr-prediction/INR-Utility.php";
		
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		httppost.setHeader("Referer", "FromTheAndroidApp");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		// Get Account ID
		String accountId = app.getOptions().get("EMail");
		
		if(accountId == null){
			Account[] accounts = AccountManager.get(app.getContext()).getAccountsByType("com.google");
			accountId = accounts[0].name;
			app.addParameter("EMail", accountId);
		}
		
		nameValuePairs.add(new BasicNameValuePair("code", "Calculate-Probability-Java"));
		nameValuePairs.add(new BasicNameValuePair("pwd", "adminhoonmain"));
		nameValuePairs.add(new BasicNameValuePair("accountid", accountId));
		
		JSONObject input = new JSONObject();
		input.put("PNR", PNR);
		input.put("TrainNo", trainNo);
		input.put("TravelDate", travelDate);
		input.put("TravelClass", trainClass);
		input.put("CurrentStatus", currentStatus);
		input.put("FromStation", fromStation);
		input.put("ToStation", toStation);
		
		nameValuePairs.add(new BasicNameValuePair("input", input.toString()));
		
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		// Execute HTTP Post Request
		HttpResponse response = httpclient.execute(httppost);

		// Open Stream for Reading.
		InputStream is = response.getEntity().getContent();

		// Get Input Stream Reader.
		InputStreamReader isr = new InputStreamReader(is);

		BufferedReader reader = new BufferedReader(isr);

		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		
		JSONObject output = new JSONObject(builder.toString());
		
		result.getData().putInt("ResultCode", output.getInt("ResultCode"));
		result.getData().putString("RAC", output.getString("RACProbability"));
		result.getData().putString("CNF", output.getString("CNFProbability"));
		result.getData().putString("Message", output.getString("Message"));
	}

}