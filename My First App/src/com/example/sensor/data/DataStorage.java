package com.example.sensor.data;

import android.annotation.TargetApi;
import      java.text.SimpleDateFormat;
import 		java.util.ArrayList;
import 		java.util.Arrays;
import		java.util.Calendar;



public class DataStorage {

	private ArrayList<DataSet> storage =new ArrayList<DataSet>();
	private int[][] raw;
	private DataSet debug;
	
	public DataStorage()
	{
		/**	raw =new int[][] { { 30, 93, 99, 86, 140, 155, 160, 135, 56 },
				{ 30, 92, 98, 86, 140, 155, 160, 135, 56 },
				{ 30, 92, 98, 86, 140, 150, 160, 135, 56 },
				{ 30, 91, 98, 85, 140, 150, 160, 140, 56 },
				{ 30, 91, 98, 85, 140, 150, 160, 150, 56 },
				{ 29, 90, 97, 84, 135, 150, 160, 165, 56 },
				{ 29, 89, 97, 84, 135, 150, 160, 175, 56 },
				{ 29, 89, 97, 84, 135, 150, 160, 175, 55 },
				{ 29, 89, 97, 83, 130, 150, 160, 170, 55 },
				{ 30, 89, 96, 83, 135, 150, 160, 165, 55 },
				{ 30, 89, 96, 82, 135, 150, 160, 165, 55 },
				{ 30, 88, 96, 82, 145, 150, 160, 160, 55 },
				{ 30, 88, 96, 81, 160, 150, 160, 155, 55 },
				{ 30, 87, 104, 81, 165, 155, 155, 150, 55 },
				{ 30, 87, 104, 81, 170, 155, 155, 140, 55 },
				{ 30, 87, 104, 80, 165, 155, 155, 140, 55 },
				{ 30, 87, 104, 80, 160, 160, 155, 135, 55 },
				{ 30, 86, 104, 92, 160, 160, 155, 135, 55 },
				{ 30, 86, 104, 92, 155, 160, 155, 130, 55 },
				{ 30, 86, 104, 91, 150, 155, 155, 130, 55 },
				{ 30, 85, 105, 91, 145, 155, 155, 125, 55 },
				{ 30, 85, 105, 90, 140, 155, 155, 125, 55 },
				{ 30, 85, 105, 91, 135, 155, 150, 125, 55 },
				{ 30, 104, 106, 90, 130, 145, 150, 125, 55 } };
			debug = new DataSet(raw,5,0); */
		
	}
	
	@TargetApi(9)
	public DataSet addNewDataSet(String rawData, long id)
	{
		int [][] result = DataCompression.decode(rawData);

		Calendar c = Calendar.getInstance();				//not sure about use in different timezones, might change timestamps to local times
		c.set(result[0][1], result[0][1], result[0][2], result[0][3], result[0][4]);
		
		DataSet element=new DataSet(Arrays.copyOfRange(result, 1, result.length), c.getTimeInMillis(), id);
		storage.add(element);
		
		return element;
	}
	
	public DataSet getData(long date, long id)
	{
		for(DataSet s: storage)
		{
			if (s.getDate().equals(date) && s.getId()==id) return s;
		}
		return null;
	}
	
	public ArrayList<DataSet> getDataById(long id)
	{
		ArrayList<DataSet> result = new ArrayList<DataSet>();
		for(DataSet s: storage)
		{
			if (s.getId()==id) 
				result.add(s);
		}
		return result;
	}
}
