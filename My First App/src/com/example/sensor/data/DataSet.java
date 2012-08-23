package com.example.sensor.data;

import 	   java.util.Date;
import		java.util.Arrays;

public class DataSet {
	
	
//TODO constants
private Date date;
private long id;
private Integer[][] data;

	
//extract the data for each sensor from decoded c-code, form one array per sensor
	public DataSet(int[][] data, long date, long id)
	{	
		int rowCount = data.length;		//Count of Measurements stays flexible
		int columnCount = 9;	//fixed to 9 different SensorDataSets
		this.data = new Integer[columnCount][rowCount];  
		this.date= new Date(date);
		this.id=id;
		
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

}
