/**
 * 
 */
package org.varunverma.pnrprediction.avail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

/**
 * @author varun
 *
 */
public class FetchAvailabilityCommand extends Command {

	/**
	 * @param caller
	 */
	public FetchAvailabilityCommand(Invoker caller) {
		super(caller);
	}

	/* (non-Javadoc)
	 * @see org.varunverma.CommandExecuter.Command#execute(org.varunverma.CommandExecuter.ResultObject)
	 */
	@Override
	protected void execute(ResultObject result) throws Exception {
		
		// Fetch Pending Queries
		List<QueryItem> pendingList = getPendingQueryItems("Full");
		
		if(pendingList.isEmpty()){
			
			pendingList = getPendingQueryItems("Correction");
			
		}

		// Loop and Query the status
		Iterator<QueryItem> i = pendingList.iterator();

		while (i.hasNext()) {
			
			QueryItem qi = i.next();
			
			// Continue with next one if this fails...
			try{
				
				ProgressInfo pi = new ProgressInfo("Fetching avail for " + qi.toString());
				publishProgress(pi);
				
				// Query the Status
				qi.queryStatus();
				
			}catch(Exception e){
				
				String message = "Error while fetching avail for " + qi.toString();
				ProgressInfo pi = new ProgressInfo(message);
				publishProgress(pi);
				
			}

			
			Thread.sleep(10 * 1000);

		}
		
		// Save Status
		saveStatus(pendingList);
		
	}
	
	private List<QueryItem> getPendingQueryItems(String mode) throws Exception{
		
	
		List<QueryItem> list = new ArrayList<QueryItem>();
		
		String queryURL = "http://pnr.varunverma.org/GetPendingItems.php";
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(queryURL);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		nameValuePairs.add(new BasicNameValuePair("input", mode));
		
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		//Execute HTTP Post Request 
    	HttpResponse response = httpclient.execute(httppost);
    	
    	InputStream is;
		InputStreamReader isr;
		
		// Open Stream for Reading.
		is = response.getEntity().getContent();

		// Get Input Stream Reader.
		isr = new InputStreamReader(is);

		BufferedReader reader = new BufferedReader(isr);

		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		
		JSONArray pendingQueryItems = new JSONArray(builder.toString());
		
		for(int i=0; i< pendingQueryItems.length(); i++){
			
			JSONObject qi = pendingQueryItems.getJSONObject(i);
			
			QueryItem queryItem = new QueryItem();
			
			queryItem.setTrainNo(qi.getString("TrainNo"));
			queryItem.setjClass(qi.getString("Class"));
			queryItem.setTravelDate(qi.getString("TravelDate"));
			queryItem.setSourceCode(qi.getString("SourceCode"));
			queryItem.setDestinationCode(qi.getString("DestinationCode"));
			
			list.add(queryItem);
			
		}

		return list;
	}
	
	
	private void saveStatus(List<QueryItem> queryItems) throws Exception{
		
		ProgressInfo pi = new ProgressInfo("Saving Status");
		publishProgress(pi);
		
		JSONArray availInfo = new JSONArray();
		
		Iterator<QueryItem> i = queryItems.iterator();
		
		while(i.hasNext()){
			
			Iterator<AvailabilityInfo> iterator = i.next().getAvailabilityInfo().iterator();
			
			while(iterator.hasNext()){
				availInfo.put(iterator.next().jsonify());
			}
			
		}
		

		String saveURL = "http://pnr.varunverma.org/SaveAvailability.php";

		HttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost(saveURL);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		String availData = availInfo.toString();
		nameValuePairs.add(new BasicNameValuePair("avail_data", availData));
		
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		//Execute HTTP Post Request 
    	HttpResponse response = httpclient.execute(httppost);
    	
    	InputStream is;
		InputStreamReader isr;
		
		// Open Stream for Reading.
		is = response.getEntity().getContent();

		// Get Input Stream Reader.
		isr = new InputStreamReader(is);

		BufferedReader reader = new BufferedReader(isr);

		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		
		if(builder.toString().contains("Success")){
			// Success
		}
		else{
			// Failure
		}
		
	}

}
