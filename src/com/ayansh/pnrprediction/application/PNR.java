package com.ayansh.pnrprediction.application;

public class PNR {

	private String pnr, trainNo, travelDate, trainClass, currentStatus, fromStation, toStation, expectedStatus;
	private float cnfProb, racProb, optCNFProb, optRACProb;
	private long lastUpdate;
	
	public PNR(String pnr){
		
		this.pnr = pnr;
		
	}
	
	public String getPnr() {
		return pnr;
	}
	public void setPnr(String pnr) {
		this.pnr = pnr;
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
	public String getTrainClass() {
		return trainClass;
	}
	public void setTrainClass(String trainClass) {
		this.trainClass = trainClass;
	}
	public String getCurrentStatus() {
		return currentStatus;
	}
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}
	public String getFromStation() {
		return fromStation;
	}
	public void setFromStation(String fromStation) {
		this.fromStation = fromStation;
	}
	public String getToStation() {
		return toStation;
	}
	public void setToStation(String toStation) {
		this.toStation = toStation;
	}
	public float getCnfProb() {
		return cnfProb;
	}
	public void setCnfProb(float cnfProb) {
		this.cnfProb = cnfProb;
	}
	public float getRacProb() {
		return racProb;
	}
	public void setRacProb(float racProb) {
		this.racProb = racProb;
	}
	public float getOptCNFProb() {
		return optCNFProb;
	}
	public void setOptCNFProb(float optCNFProb) {
		this.optCNFProb = optCNFProb;
	}
	public float getOptRACProb() {
		return optRACProb;
	}
	public void setOptRACProb(float optRACProb) {
		this.optRACProb = optRACProb;
	}
	public String getExpectedStatus() {
		return expectedStatus;
	}
	public void setExpectedStatus(String expectedStatus) {
		this.expectedStatus = expectedStatus;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
}