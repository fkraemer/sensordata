package com.example.sensor.data;

public class Main {


	//training data:
	//1347532527789/+61431220285/0y6IrunLB0n7JNUhIQxO;VW27m0vu0Xypn0Uy0DVVpm0tvyG7:83;41xn0FpFzUV41;dn0VW00000000000000000
	//1347435862509/+61431220285/026Inpm50810BmTRhTR40000001e000000000000000000000000000
	//1347611729451/+61431220285/0y6IvenCA0k7RNTQ:VxOp::;03dNVe:7rlNY1bu0Y:;dW43pvl438000000000000000000000000000000
	//1347572126696/+61431220285/0y6IuGnLAGj7QRK2HGxKpm1Lu0Wy0J0mog6PL0000000000000000000000000000000
	
	public static void main(String[] args) {
		
		try {
			int [][] sensorData = DataCompression.decode("0y6IuGnLAGj7QRK2HGxKpm1Lu0Wy0J0mog6PL0000000000000000000000000000000");
		} catch (DecodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		}

}
