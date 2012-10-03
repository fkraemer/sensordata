package com.example.sensor.data;

import android.os.Parcel;

public class DataSetWithMetaData extends DataSet {

	//---------------------------------------------------------------------------
	// optional metadata
	private String description;
	private int elevation;
	private int latitude;
	private int longitude;
	private int platformId;
	
	private int[] sensorId = new int[5];
	private int[][] sensorOffsets=new int[5][3];	//in format [sensorNo][{offsetX,offsetY,offsetZ}]
	private int[] subsensorId = new int[9];			//convention [1] and [2] belong to sensorId[1], [3] and [4] to sensorId[2] and so on

	//---------------------=---=-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=--=-=-=-=-=-=-=-=-=--
	
	public DataSetWithMetaData(float[][] data, long date, String mobileNo, int localId,	int timeOffset,
			String description, int platformId, int longitude, int latitude, int elevation,
			int[] sensorId, int[][]sensorOffsets, int[] subSensorId) {
		super(data, date, mobileNo, localId, timeOffset);
	
		this.description=description;
		this.platformId=platformId;
		this.longitude=longitude;
		this.latitude=longitude;
		this.elevation=elevation;
		this.sensorId=sensorId;
		this.sensorOffsets=sensorOffsets;
		this.subsensorId=subSensorId;
		
		
	}
	
	

	public DataSetWithMetaData(Parcel in) {
		super(in);
		// TODO Auto-generated constructor stub
	}

	public String getDescription() {
		return description;
	}

	public int getElevation() {
		return elevation;
	}

	public int getLatitude() {
		return latitude;
	}

	public int getLongitude() {
		return longitude;
	}

	public int getPlatformId() {
		return platformId;
	}

	public int[] getSensorId() {
		return sensorId;
	}

	public int[] getSensorOffsets(int i) {
		return sensorOffsets[i];
	}

	public int[] getSubsensorId() {
		return subsensorId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public void setLatitude(int latitude) {
		this.latitude = latitude;
	
	}

	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}

	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}

	public void setSensorId(int[] sensorId) {
		this.sensorId = sensorId;
	}

	public void setSensorOffsets(int sensorNo,int[] sensorOffsets) {
		this.sensorOffsets[sensorNo] = sensorOffsets;
	}

	public void setSubsensorId(int[] subsensorId) {
		this.subsensorId = subsensorId;
	}

}
