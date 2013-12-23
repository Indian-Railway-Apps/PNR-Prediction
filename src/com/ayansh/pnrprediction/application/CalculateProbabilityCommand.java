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
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ResultObject;

/**
 * @author I041474
 *
 */
public class CalculateProbabilityCommand extends Command {

	private String trainNo, travelDate, trainClass, currentStatus;
	
	/**
	 * @param caller
	 */
	public CalculateProbabilityCommand(Invoker caller, String tNo, String tDt, String tCl, String cSt) {
		
		super(caller);
		
		trainNo = tNo;
		travelDate = tDt;
		trainClass = tCl;
		currentStatus = cSt;
		
	}

	/* (non-Javadoc)
	 * @see org.varunverma.CommandExecuter.Command#execute(org.varunverma.CommandExecuter.ResultObject)
	 */
	@Override
	protected void execute(ResultObject result) throws Exception {

		String url = "http://ayansh.com/pnr-prediction/INR-Utility.php";
		
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		nameValuePairs.add(new BasicNameValuePair("code", "Calculate-Probability"));
		nameValuePairs.add(new BasicNameValuePair("pwd", "adminhoonmain"));
		
		nameValuePairs.add(new BasicNameValuePair("train_no", trainNo));
		nameValuePairs.add(new BasicNameValuePair("tr_date", travelDate));
		nameValuePairs.add(new BasicNameValuePair("tr_class", trainClass));
		nameValuePairs.add(new BasicNameValuePair("curr_status", currentStatus));
		
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
		
		//JSONObject output = new JSONObject(builder.toString());
		
		result.getData().putString("Output", builder.toString());
	}

}
