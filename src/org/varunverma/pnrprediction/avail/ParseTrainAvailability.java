package org.varunverma.pnrprediction.avail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ParseTrainAvailability extends DefaultHandler {

	private boolean reading;
	String data;
	String class1, class2;
	Date date;
	int index, row;
	public List<AvailabilityInfo> availInfo;
	private AvailabilityInfo ai1, ai2;
	private String trainNo, reqClass, from, to;
	
	public ParseTrainAvailability(){
		row = 0;
		availInfo = new ArrayList<AvailabilityInfo>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes){
		
		if(qName.equals("TD")){
			reading = true;
			data = "";
			
			if(row == 0){
				ai1 = new AvailabilityInfo();
				ai1.TrainNo = trainNo;
				ai1.fromStation = from;
				ai1.toStation = to;
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
				sdf.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
				ai1.LookupTimeStamp = sdf.format(date);

			}
			
			row++;
			
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		
		if(qName.equals("TD")){
			
			switch(row){
			
			case 1:
				// Seq No
				break;
				
			case 2:
				// Date
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
				try {
					date = sdf.parse(data);
				} catch (ParseException e) {
				}
				sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
				ai1.JourneyDate = sdf.format(date);
				if(ai1.JourneyDate == null || ai1.JourneyDate.contentEquals("")){
				}
				break;
				
			case 3:
				// Class 1
				class1 = data;
				ai1.Class = reqClass;
				ai1.setAvailability(class1);
				availInfo.add(ai1);
				break;
				
			case 4:
				// Class 2
				class2 = data;
				ai2 = ai1.clone();
				
				if(reqClass.contentEquals("1A")){
					ai2.Class = "2A";
				}
				
				if(reqClass.contentEquals("3A")){
					ai2.Class = "SL";
				}
				
				ai2.setAvailability(class2);
				availInfo.add(ai2);
				row = 0;
				break;
			
			}
			
		}
		
	}
	
	@Override
	public void characters(char[] ch, int start, int length){
		
		if (reading){
			
			int i = 0;
			int index = start;
			do{
				data = data + ch[index];
				i = i+1;
				index = index + 1;
			}while(i<length);
		}
	}
	
	public void setAdditionalInfo(String t, String c, String f, String to){
		trainNo = t;
		reqClass = c;
		from = f;
		this.to = to;
	}
}