package com.example.sensor.data;

import 	   java.util.Date;
import		java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

public class DataSet implements Parcelable{
	

//TODO constants
private Date date;
private int localId;	//necessary for handling by android-app
private long id;
private Integer[][] data;

	
//extract the data for each sensor from decoded c-code, form one array per sensor
	public DataSet(int[][] data, long date, long id, int localId)
	{	
		int rowCount = data.length;		//Count of Measurements stays flexible
		int columnCount = 9;	//fixed to 9 different SensorDataSets
		this.data = new Integer[columnCount][rowCount];  
		this.date= new Date(date);
		this.id=id;
		this.localId=localId;
		
		for (int j = 0; j < columnCount; j++) {		//copy columns into data.rows
			Integer[] column = new Integer[rowCount];
			for (int i=0; i < rowCount; i++) {
				try {
				this.data[j][i] = new Integer(data[i][j]);
				} catch (NullPointerException e) {		//evntl NullPointer fangen
					e.printStackTrace();				//implement logging ?
				}	
			}
		}
	}
	
	
	@Override
	public String toString() {
		//set different time format here or on construct
		return Integer.toString(localId)+")  from  +"+Long.toString(id)+"   "+ date.toString();
	}


	public int getLocalId() {
		return localId;
	}


	public void setLocalId(int localId) {
		this.localId = localId;
	}


	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Integer[] getTempData(int i)
	{
	if (i<4 && i>=0) {
		return data[i];		
	}
	return null;
	}
	
	public Integer[] getMoistData(int i)
	{
	if (i<4 && i>=0) {
		return data[i+4];		
	}
	return null;
	}


	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int[][] copy= Arrays.cop
		dest.writeIntArray(data);
		
	}

}
