package com.example.sensor.data;

import java.util.Arrays;
import java.util.List;

public class Main {


	
	
	public static void main(String[] args) {
		String input1 = "6HKQ8B00003NQJIQJC00000000000D3XrrrrWRkw11Xvy:VF00000400102W";
		String input2 = "+61431220285".substring(1, "+61431220285".length());
		DataStorage data = new DataStorage();
		Long long1 = Long.decode(input2);
		
		DataSet neu = data.addNewDataSet(input1, long1);
		
		String  dat=neu.getDate().toString();
		System.out.println(dat);
		}

	private void testDataStorage()
	{
		
	}
}
