package com.example.sensor.data;

import java.util.Arrays;
import java.util.List;

public class Main {


	
	
	public static void main(String[] args) {
		DataStorage data = new DataStorage();
		Integer[] array = data.getData().getTempData(0);
		Integer[] array2 ={1,12,3,5,13};
		List<Integer> list = Arrays.asList(array);
		List<Integer> list2 = Arrays.asList(array2);

		String [] dat=new String[0];
		System.out.println(String.valueOf(dat.length));
	}

	private void testDataStorage()
	{
		
	}
}
