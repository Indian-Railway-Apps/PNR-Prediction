/**
 * 
 */
package org.varunverma.pnrprediction.avail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author varun
 * 
 */
public class QueryItem {

	private String trainNo, travelDate, jClass, sourceCode, destinationCode;
	private List<AvailabilityInfo> availInfo;
	
	public QueryItem(){
		trainNo = travelDate = jClass = sourceCode = destinationCode = "";
		availInfo = new ArrayList<AvailabilityInfo>();
	}
	
	public String getTrainNo() {
		return trainNo;
	}

	public void setTrainNo(String trainNo) {
		this.trainNo = trainNo;
	}

	public String getTravelDate() {
		return travelDate;
	}

	public void setTravelDate(String travelDate) {
		this.travelDate = travelDate;
	}

	public String getjClass() {
		return jClass;
	}

	public void setjClass(String jClass) {
		this.jClass = jClass;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getDestinationCode() {
		return destinationCode;
	}

	public void setDestinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
	}
	
	public List<AvailabilityInfo> getAvailabilityInfo(){
		return availInfo;
	}

	public void queryStatus() throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {

		// Query the Status
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost("http://www.indianrail.gov.in/cgi_bin/inet_accavl_cgi.cgi");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		
		String[] date = travelDate.split("-");
		String day = date[2];
		String month = date[1];
		String year = date[0];
		
		nameValuePairs.add(new BasicNameValuePair("lccp_trnno", trainNo));
    	nameValuePairs.add(new BasicNameValuePair("lccp_day", day));
    	nameValuePairs.add(new BasicNameValuePair("lccp_month", month));
    	nameValuePairs.add(new BasicNameValuePair("lccp_year", year));
    	nameValuePairs.add(new BasicNameValuePair("lccp_srccode", sourceCode));
    	nameValuePairs.add(new BasicNameValuePair("lccp_dstncode", destinationCode));
    	nameValuePairs.add(new BasicNameValuePair("lccp_class1", jClass));
    	nameValuePairs.add(new BasicNameValuePair("lccp_quota", "GN"));
    	nameValuePairs.add(new BasicNameValuePair("submit", "Get Availability"));
    	
    	nameValuePairs.add(new BasicNameValuePair("lccp_classopt", "ZZ"));
    	nameValuePairs.add(new BasicNameValuePair("lccp_class2", "ZZ"));
    	nameValuePairs.add(new BasicNameValuePair("lccp_class3", "ZZ"));
    	nameValuePairs.add(new BasicNameValuePair("lccp_class4", "ZZ"));
    	nameValuePairs.add(new BasicNameValuePair("lccp_class5", "ZZ"));
    	nameValuePairs.add(new BasicNameValuePair("lccp_class6", "ZZ"));
    	nameValuePairs.add(new BasicNameValuePair("lccp_class7", "ZZ"));
    	
    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		httppost.addHeader("Referer", "http://www.indianrail.gov.in/seat_Avail.html");
		
		// Execute HTTP Post Request
		HttpResponse response = httpclient.execute(httppost);

		if (response != null) {

			// Read
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			boolean read = false;
			String line = null, response_string = "", html_content = "";
			int to;
			while ((line = reader.readLine()) != null) {

				html_content = html_content.concat(line);
				html_content = html_content.concat("\n");

				if (line.contains("S.No.")) {
					read = true;
					to = line.indexOf(">");
					line = line.replaceFirst(line.substring(3, to), "");
					line = "<tbody>" + line;
				}

				if (line.contains("</tbody>")) {
					response_string = response_string + "</tbody>" + "\n";
					read = false;
				}

				if (line.contains("ALIGN = ")) {
					to = line.indexOf(">");
					line = line.replaceFirst(line.substring(3, to), "");
				}

				if (line.contains("class=")) {
					to = line.indexOf(">");
					line = line.replaceFirst(line.substring(3, to), "");
				}

				if (line.contentEquals("<TR>") || line.contentEquals("</TR>")) {
					line = "";
				}

				if (read) {
					response_string = response_string + line + "\n";
				}
			}

			in.close();

			ParseTrainAvailability my_handler = new ParseTrainAvailability();
			my_handler.setAdditionalInfo(trainNo, jClass, sourceCode, destinationCode);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			InputSource is = new InputSource(new StringReader(response_string));
			saxParser.parse(is, my_handler);
			
			availInfo.addAll(my_handler.availInfo);

		}

	}

}