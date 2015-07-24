package com.hdu.yuan.heartrate.object;


import  com.hdu.yuan.heartrate.object.*;

public class History {
	private int id;
	private int peopleId;
	private String data;
	
	/*************ECG数据***************/
	private String EcgData;
	
	private MeasureArg measureArg;
	private String timeOfRecord;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPeopleId() {
		return peopleId;
	}
	public void setPeopleId(int peopleId) {
		this.peopleId = peopleId;
	}
	public String getData() {
		return data;
	}
	public String getEcgData(){
		return EcgData;
	}
	public void setData(String data) {
		this.data = data;
	}
	public void setEcgData(String EcgData) {
		this.EcgData = EcgData;
	}
	public MeasureArg getMeasureArg() {
		return measureArg;
	}
	public void setMeasureArg(MeasureArg measureArg) {
		this.measureArg = measureArg;
	}
	public String getTimeOfRecord() {
		return timeOfRecord;
	}
	public void setTimeOfRecord(String timeOfRecord) {
		this.timeOfRecord = timeOfRecord;
	}

	
}
