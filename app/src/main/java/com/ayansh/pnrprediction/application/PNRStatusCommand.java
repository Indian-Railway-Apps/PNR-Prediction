/**
 * Licensed to Varun Verma 
 */
/**
 * @author Varun Verma
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ResultObject;

public class PNRStatusCommand extends Command {

	private String pnr;

	public PNRStatusCommand(Invoker caller, String pnr) {

		super(caller);
		this.pnr = pnr;
	}

	@Override
	protected void execute(ResultObject result) throws Exception {

		String response_string = "";
		String[] result1, result2;

		if (pnr.length() < 10) {
			throw new Exception("Wrong PNR Number!");
		}
		
		String url = getPNREnquiryURL();

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		// Try to Post the PNR
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("lccp_pnrno1", pnr));
		nameValuePairs.add(new BasicNameValuePair("lccp_cap_val", "51213"));
		nameValuePairs.add(new BasicNameValuePair("lccp_capinp_val", "51213"));
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		httppost.setHeader("Referer","http://www.indianrail.gov.in/pnr_Enq.html");

		// Execute HTTP Post Request
		HttpResponse response = httpclient.execute(httppost);

		if (response != null) {
			// Read

			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			boolean read1 = false, read2 = false;
			String line = null, response_string1 = "", response_string2 = "";
			while ((line = reader.readLine()) != null) {

				response_string = response_string.concat(line);
				response_string = response_string.concat("\n");

				if (line.contains("PNR Number :")) {
					read1 = true;
				}
				if (line.contains("<FORM NAME=\"RouteInfo\" METHOD=\"POST\"")) {
					read1 = false;
				}
				if (line.contains("Get Schedule")) {
					read2 = true;
					line = "";
				}
				if (line.contains("Charting Status")) {
					read2 = false;
				}
				if (read1) {
					response_string1 = response_string1.concat(line);
					response_string1 = response_string1.concat("\n");
				}
				if (read2) {
					if (line.contains("<font size=1>")) {
						line = "";
					}
					if (line.contains("<TABLE width=")) {
						line = "<table border=" + '\"' + "1" + '\"' + ">";
					}
					if (line.contains("<td width=")) {
						line = line.replaceFirst(line.substring(3, 15), "");
					}
					response_string2 = response_string2.concat(line);
					response_string2 = response_string2.concat("\n");
				}
			}

			in.close();

			String identifier = "<TD class=\"table_border_both\">";
			result1 = response_string1.split(identifier, 10);
			result2 = response_string2.split(identifier, 10);

			int index = 1;
			int size = result1.length;
			while (index < size) {
				result1[index] = result1[index].replaceAll("</TD>", "");
				result1[index] = result1[index].replaceAll("\n", "");
				result1[index] = result1[index].trim();
				index++;
			}
			index = 1;
			size = result2.length;
			while (index < size) {
				result2[index] = result2[index].replaceAll("</TD>", "");
				result2[index] = result2[index].replaceAll("\n", "");
				result2[index] = result2[index].replaceAll("<TR>", "");
				result2[index] = result2[index].replaceAll("</TR>", "");
				result2[index] = result2[index].replaceAll("<B>", "");
				result2[index] = result2[index].replaceAll("</B>", "");
				result2[index] = result2[index].trim();
				index++;
			}

			String train_number = result1[1].replaceAll("\\*", "");
			String from = result1[4];
			String to = result1[5];
			//String boarding = result1[7];
			String date = result1[3];
			date = date.replaceAll(" ", "");
			
			String currentStatus = result2[3];
			String travelClass = result1[8];
			travelClass = travelClass.replaceAll(" ", "");
			travelClass = travelClass.substring(0, 2);
			
			// remove all spaces
			currentStatus = currentStatus.replaceAll(" ", "");
			
			if(currentStatus.contains("W/L")){
				currentStatus = currentStatus.replace("W/L", "WL");
			}
			
			result.getData().putString("TrainNo", train_number);
			result.getData().putString("FromStation", from);
			result.getData().putString("ToStation", to);
			result.getData().putString("TravelDate", date);
			result.getData().putString("CurrentStatus", currentStatus);
			result.getData().putString("TravelClass", travelClass);

		}

	}

	private String getPNREnquiryURL() throws Exception {
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://ayansh.com/pnr-prediction/get_pnr_enquiry_url.php");
		
		// Execute HTTP Post Request
		HttpResponse response = httpclient.execute(httpget);

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
		
		return builder.toString();
		
	}

}