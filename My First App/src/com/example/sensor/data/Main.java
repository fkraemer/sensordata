package com.example.sensor.data;

import java.util.Arrays;
import java.util.List;

public class Main {


	
	
	public static void main(String[] args) {
		String input1 = "6H24e308203cSBXSB8ny:xkw2g1L0W1K00G0K1000000000080A000004";
		String input2 = "+61431220285";
		DataStorage data = new DataStorage();
		
		
		DataSet neu = null;
		try {
			neu = data.addNewDataSet(DataCompression.decode(input1), input2);
		} catch (DecodeFatalException e) {
			e.printStackTrace();
		}catch (DecodeRecoverException e) {
			neu=e.getData();
			System.out.println(e.toString());
			neu = data.addNewDataSet(((DecodeRecoverException) e).getDataInts(), input2);
		} catch (DecodeException e) {			//unreachable
		}
		
		
		String  dat=neu.getDate().toString();
		System.out.println(dat);
		}

	private void testDataStorage()
	{
		
	}
}
