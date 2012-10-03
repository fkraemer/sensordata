package com.example.sensor.data;

import android.annotation.TargetApi;
import java.util.Arrays;


public abstract class DataCompression {

	public final static int ANCHORCHARLENGTH=20;
	private final static int[] anchorCodeBook={12,7,4,5,5,6,9,9,9,9,9,9,9,9,7};
	public final static int ANCHORLENGTH=15;
	private final static int[] anchorOffsets={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private final static int CBITS = 6;
	private final static int HUFCODEBOOKLENGTH=27;
	private final static int[][] huff28 = { { 0,  -5,   -1,    5,   1,   10,  15,  1001,  -10,   20,  25,   -2,    2,    
			3,  -4,   -3,   11,  30,  -42,  -16,   -6,    8,   44,  60,   69,   70, 1002 },
			{ 0,   2,   12,   13,  28,   58,   59,  120,  121,  122,  123,  124,  125,  
			252, 506, 507, 508, 509, 2040, 2041, 2042, 2043, 2044, 2045, 2046, 4094, 4095  }};
	public final static int MEASURECOUNT=24;
	public final static int SENSORCOUNT=9;
	
	private static int[] binToAnchorVec(String anchorBinString,
			int[] anchorcodebook, int[] anchoroffsets) {
		int [] result= new int[ANCHORLENGTH];
		int pos=0;
		for (int i=0; i<ANCHORLENGTH;i++) {
				int newpos=pos+anchorcodebook[i];
				try {
					result[i]=Integer.parseInt(anchorBinString.substring(pos, newpos),2)+anchoroffsets[i];
				} catch (NumberFormatException e)
				{
					result[i]=Integer.MAX_VALUE;
				}
				pos=newpos;
			}
		return result;
	}
	/**
	 * 
	 * @param s: input is rachel-huffman decoded sensordata/sms-message
	 * @return: [number of measures + 1][number of sensors], where the first row [0][1-5] provides the timestamp YY-MM-DD-HH-MM 
	 */
	@TargetApi(9)
	public static int[][] decode(String s) throws DecodeException
	{		
		int[][] result = new int[MEASURECOUNT+1][SENSORCOUNT];
		int [] anchorVec = decodeAnchor(s.substring(0, ANCHORCHARLENGTH));
		DecodeRecoverException excep=null;
		
		try {
			int [][] difsVec = decodeDifs(s.substring(ANCHORCHARLENGTH, s.length()),result);
		} catch (DecodeFatalException e) {
			throw e;
		}catch (DecodeRecoverException e) {
			excep=(DecodeRecoverException) e;
			result=excep.getDataInts();
		} catch (DecodeException e) {			//unreachable
		}	

		result[0] = Arrays.copyOfRange(anchorVec,0,ANCHORLENGTH-SENSORCOUNT);	//hardcoded:leading5 ints are time stamp
		result[1] = Arrays.copyOfRange(anchorVec,ANCHORLENGTH-SENSORCOUNT,ANCHORLENGTH);	//hardcoded:leading5 ints are time stamp
		for (int i=2;i<MEASURECOUNT+1;i++)
		{
			for (int j=0;j<SENSORCOUNT;j++)
			{
			result[i][j]+=result[i-1][j];		//get absolutes	
			}
		}
		
		//TODO: case: too short exception ??
		if (excep!=null)
		{
			excep.setDataInts(result);
			throw excep; 
		}
		
		return result;
	}

	private static int[] decodeAnchor(String anchor) {
		String anchorBinString = smsToBin(anchor.toCharArray(),CBITS);
		return binToAnchorVec(anchorBinString,anchorCodeBook,anchorOffsets);
		
	}

	private static int[][] decodeDifs(String difs, int [][] result) throws DecodeException {
		String s = smsToBin(difs.toCharArray(), CBITS);
		return huffBinToDifs(s,result);
		
	}

	private static int getValueOfHuf(int val) {
		for (int i=0;i<HUFCODEBOOKLENGTH;i++)
		{
			if (huff28[1][i]==val) return huff28[0][i];
		}
		return Integer.MIN_VALUE;	// case: nothing found
	}

	private static int[][] huffBinToDifs(String s, int [][] result) throws DecodeException
	{
		int dpos=0;
		int rowCount=2;		//start inserting in first dif-row
		int columnCount=0;
		boolean recoverException=false;
		int errorCount=0;
		
		while(dpos<s.length()) {
			int count=1;
			while (true)
			{
				int code = getValueOfHuf(Integer.valueOf(s.substring(dpos, dpos+count),2));
				if (code == Integer.MIN_VALUE)	//case: not found
				{
					count++;
					if(code+count == s.length()) {
						throw new DecodeFatalException("could not detect char till end, exceeded string-length");
					}
					continue;
				} else if (code == 1001) {
					code=Integer.valueOf(s.substring(dpos+count+1, dpos+count+11),2);
					code*=(s.charAt(dpos+count) == '1') ? -1 : 1;							//set sign
				} else if (code == 1002)	//error-case
				{
					code=0;	//for now, assume no difference
					recoverException=true;
					errorCount++;
				}
				if (rowCount == MEASURECOUNT+1) {		//protects from result-ArrayOutOfBounce
					throw new DecodeRecoverException("Decode-stream too long. Cutting of "+(s.length()-dpos)+" chars.",result);
				} else 	result[rowCount][columnCount]=code;
				break;
			}
			
			dpos+=count;
			if (columnCount % 9 == 8) {
				columnCount=0;
				rowCount++;
			} else {
				columnCount++;
			}
		}
		
		
		if (recoverException) {
			throw new DecodeRecoverException("Decode-stream contains" +Integer.toString(errorCount)+ " errors. Assuming zero difs.",result);
		}
		return result;
	}

	public static void main(String[] args) {
		//6HKQ8B00003NQJIQJC 00000000000D3XrrrrWRkw11Xvy:VF00000400102W
		//int[][] neu =decode("6HKQ8B00003NQJIQJC00000000000D3XrrrrWRkw11Xvy:VF00000400102W");
	}
	

	//The chars are translated to Integers and then Binary code with 1. constant(bitCount) length per char or variable length for bitCount=-1
	private static String smsToBin(char[] c,int bitCount) {
		StringBuilder sb = null;
		if (bitCount==-1) {
			sb=new StringBuilder();		
		} else {
			char[] collect=new char[bitCount * c.length];
			Arrays.fill(collect, '0');								//fill with zeros before adding binary code
			sb=new StringBuilder(String.valueOf(collect));
		}
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
			if (bitCount==-1) {
				sb.append(s);
			} else
			sb.replace((i+1)*bitCount-s.length(),(i+1)*bitCount, s);				//adding with leading zeros
		}
		return sb.toString();
			
		
		
		
		
		
	}

		
	
	
	
	//getdata[][]
	//gettime
}
