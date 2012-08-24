package com.example.sensor.data;

import java.util.Arrays;


public abstract class DataCompression {

	private final static int ANCHORLENGTH=14;
	private final static int[] anchorOffsets={0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private final static int[] anchorCodeBook={7,4,5,5,6,9,9,9,9,9,9,9,9,7};
	
	public static void main(String[] args) {
		//System.out.println(String.valueOf('0')+   "  ==>  " + String.valueOf(smsCharToValue('0'))); // test smsCharToValue
		
		//test smsToBin
		System.out.println(smsToBin("ABCDEFjhkGHhjk".toCharArray()));
		
	}
	
	public static int[][] decode(String s)
	{
		String anchor=s.substring(0, 17);
		String difs=s.substring(8, s.length()-1);
		int [] anchorvec = decodeAnchor(anchor);
		
		
		return null;
	}

	private static int[] decodeAnchor(String anchor) {
		String anchorBinString = smsToBin(anchor.toCharArray());
		return bintoAnchorVec(anchorBinString,anchorCodeBook,anchorCodeBook);
	}

	private static int[] bintoAnchorVec(String anchorBinString,
			int[] anchorcodebook2, int[] anchorcodebook3) {
		// TODO Auto-generated method stub
		return null;
	}
	

	//The chars are translated to Integers and then Binary code with constant length per char
	private static String smsToBin(char[] c) {
		final int CBITS = 6;
		char[] collect=new char[CBITS * ANCHORLENGTH];
		Arrays.fill(collect, '0');								//fill with zeros before adding binary code
		StringBuilder sb=new StringBuilder(String.valueOf(collect));
		for (int i=0; i < c.length; i++)
		{
			int intResult=-1;		//error, might have to be changed to another number TODO
			
			//translation of chars
			char p = c[i];
			if (p==';') intResult=63;
			else if (p==':') intResult= 62;
			else if ('0' <= p && p<='9') intResult= (p-48);  //convert char 0-9 to ASCII (48-57), Values 0-9
			else if ('A' <= p && p<='Z') intResult= (p-55);  //convert char A-Z to ASCII (65-90), Values 10-35
			else if ('a' <= p && p<='z') intResult= (p-61);  //convert char a-z to ASCII (97-122, Values 36-61
			// else is already -1, otherwise default action here
			
			String s=Integer.toBinaryString(intResult);
			sb.replace((i+1)*CBITS-s.length(),(i+1)*CBITS, s);				//adding with leading zeros
		}
		return sb.toString();
			
		
		
		
		
		
	}

	//private static int smsCharToValue(char c) {
		
	
	
	
	//getdata[][]
	//gettime
}
