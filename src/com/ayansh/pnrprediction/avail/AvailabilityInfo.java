package com.ayansh.pnrprediction.avail;

import org.json.JSONException;
import org.json.JSONObject;

public class AvailabilityInfo {
	
	public String TrainNo, JourneyDate, Class, LookupTimeStamp, fromStation, toStation;
	public String Availability, grossAvType, netAvType;
	public int grossAvCount, netAvCount; 

	protected AvailabilityInfo clone(){
		
		AvailabilityInfo ai = new AvailabilityInfo();
		
		ai.TrainNo = TrainNo;
		ai.JourneyDate = JourneyDate;
		ai.Class = Class;
		ai.LookupTimeStamp = LookupTimeStamp;
		ai.fromStation = fromStation;
		ai.toStation = toStation;
		
		return ai;
	}
	
	public void setAvailability(String avail){
		
		Availability = avail;
		String cnt = "0";
		int index;
		
		String grossAvail = "";
		String netAvail = "";
		
		avail = Availability.replaceAll(" ", "");
		
		if(avail.contains("/")){
			String[] availability = avail.split("/");
			
			if(availability.length == 2){
				grossAvail = availability[0];
				netAvail = availability[1];
			}
		}
		else{
			grossAvail = avail;
			netAvail = avail;
		}
		
		if(grossAvail.contains("WL")){
			grossAvType = "WL";
			index = grossAvail.indexOf("WL");
			cnt = grossAvail.substring(index + 2);
			if (cnt.contentEquals("")) {
				grossAvCount = 0;
			} else {
				if (cnt.contentEquals("")) {
					grossAvCount = 0;
				} else {
					grossAvCount = Integer.valueOf(cnt);
				}
			}
		}
		
		if(grossAvail.contains("RAC")){
			grossAvType = "RAC";
			index = grossAvail.indexOf("RAC");
			cnt = grossAvail.substring(index + 3);
			grossAvCount = Integer.valueOf(cnt);
		}
		
		if(grossAvail.contains("AVAILABLE")){
			grossAvType = "CNF";
			index = grossAvail.indexOf("AVAILABLE");
			cnt = grossAvail.substring(index + 9);
			grossAvCount = Integer.valueOf(cnt);
		}
		
		if(grossAvail.contains("REGRET")){
			grossAvType = "REG";
			grossAvCount = 0;
		}
		
		if (netAvail.contains("WL")) {
			netAvType = "WL";
			index = netAvail.indexOf("WL");
			cnt = netAvail.substring(index + 2);
			netAvCount = Integer.valueOf(cnt);
		}
		
		if (netAvail.contains("RAC")) {
			netAvType = "RAC";
			index = netAvail.indexOf("RAC");
			cnt = netAvail.substring(index + 3);
			netAvCount = Integer.valueOf(cnt);
		}
		
		if(netAvail.contains("AVAILABLE")){
			netAvType = "CNF";
			index = netAvail.indexOf("AVAILABLE");
			cnt = netAvail.substring(index+9);
			if (cnt.contentEquals("")) {
				netAvCount = 0;
			}
			else{
				netAvCount = Integer.valueOf(cnt);
			}
			
		}
		
	}

	public JSONObject jsonify() throws JSONException {

		JSONObject json = new JSONObject();
		
		json.put("TrainNo", TrainNo);
		json.put("JourneyDate", JourneyDate);
		json.put("JClass", Class);
		json.put("LookupTimeStamp", LookupTimeStamp);
		json.put("GrossAvType", grossAvType);
		json.put("GrossAvCount", grossAvCount);
		json.put("NetAvType", netAvType);
		json.put("NetAvCount", netAvCount);
		json.put("Availability", Availability);
		
		return json;
		
	}
	
}