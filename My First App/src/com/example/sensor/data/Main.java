package com.example.sensor.data;

import java.util.Arrays;
import java.util.List;

public class Main {


	
	
	public static void main(String[] args) {
		String input1 = "6HKQ8B00003NQJIQJC00000000000D3XrrrrWRkw11Xvy:VF00000400102W";
		String input2 = "+61431220285";
		DataStorage data = new DataStorage();
		
		DataSet neu = data.addNewDataSet(input1, Long.getLong(input2.substring(1, input2.length())));
		
		String [] dat=new String[0];
		System.out.println(String.valueOf(dat.length));
	}

	private void testDataStorage()
	{
		
	}
}
