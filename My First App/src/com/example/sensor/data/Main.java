package com.example.sensor.data;

import java.util.Arrays;
import java.util.List;

public class Main {


	
	
	public static void main(String[] args) {
		String input1 = "6H24e308203cSBXSB8ny:xkw2g1L0W1K00G0K1000000000080A0000040000";
		String input2 = "+61431220285".substring(1, "+61431220285".length());
		DataStorage data = new DataStorage();
		
		Long long1 = Long.decode(input2);
		
		DataSet neu = null;
		try {
			neu = data.addNewDataSet(input1, long1);
		} catch (DecodeFatalException e) {
			e.printStackTrace();
		}catch (DecodeRecoverException e) {
			neu=e.getData();
			System.out.println(e.toString());
		} catch (DecodeException e) {			//unreachable
		}
		
		
		String  dat=neu.getDate().toString();
		System.out.println(dat);
		}

	private void testDataStorage()
	{
		
	}
}
