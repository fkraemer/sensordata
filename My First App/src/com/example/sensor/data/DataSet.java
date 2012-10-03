package com.example.sensor.data;

import 	   java.util.Date;
import		java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

public class DataSet implements Parcelable{

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public DataSet createFromParcel(Parcel in) {
			return new DataSet(in);
		}

		public DataSet[] newArray(int size) {
			return new DataSet[size];
		}
	};
	private float[][] data;
	// TODO constants
	private Date date;
	private int localId; // necessary for handling by android-app
	private String mobileNo;


	private Date timeOffset;
	
//extract the data for each sensor from decoded c-code, form one array per sensor
	public DataSet(float[][] data, long date, String mobileNo, int localId, int timeOffset)
	{
		this.data =data;		//expects float[sensorcount][measurecount]
		
		this.date= new Date(date);
		this.mobileNo=mobileNo;
		this.localId=localId;
		this.timeOffset = new Date(timeOffset*60000);	//convert minutes to milis
	}
	
	
	public DataSet(Parcel in) {
		readFromParcel(in);
	}


	public int describeContents() {
		return 0;
	}
	public Date getDate() {
		return date;
	}


	public int getLocalId() {
		return localId;
	}



	public float[] getMeausrements(int sensorNo)
	{
		return data[sensorNo];
	}


	public String getmobileNo() {
		return mobileNo;
	}

	public Float[] getMoistData(int k)
	{
		Float[] result = new Float[data[1].length];
		if (k<4 && k>=0) {		
			//return float array, first row contains timestamp
			for (int i=0; i < data[1].length; i++) {
				try {
				result[i] =new Float( data[k][i]);
				} catch (NullPointerException e) {		//evntl NullPointer fangen
					e.printStackTrace();				
				}
			}
		} else {
			Arrays.fill(result, 0);		//out of range calls get back zeros
		}
		return result;	
	}

	public Float[] getTempData(int k)
	{
		
		Float[] result = new Float[data[1].length];
		if (k<4 && k>=0) {		
			//return float array, first row contains timestamp
			for (int i=0; i < data[1].length; i++) {
				try {
				result[i] =new Float( data[k+4][i]);
				} catch (NullPointerException e) {		//evntl NullPointer fangen
					e.printStackTrace();				
				}
			}
		}
		return result;		
	}

	public Date getTimeOffset() {
		return timeOffset;
	}
	
	private void readFromParcel(Parcel in) {
		int length=in.readInt();
		data=new float[length][in.readInt()];
		for (int i=0;i<length;i++)
		{
			data[i]=in.createFloatArray();
		}
		localId=in.readInt();
		mobileNo=in.readString();
		date=new Date(in.readLong());
		timeOffset= new Date(in.readLong());
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setLocalId(int localId) {
		this.localId = localId;
	}	


	public void setmobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}


	@Override
	public String toString() {
		//set different time format here or on construct
		return Integer.toString(localId)+")  from  +"+mobileNo+"   "+ date.toString();
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(data.length);			//saving array dimensions
		dest.writeInt(data[1].length);
		for (int i=0;i<data.length;i++)
		{
			dest.writeFloatArray(data[i]);
		}
		dest.writeInt(localId);
		dest.writeString(mobileNo);
		dest.writeLong(date.getTime());
		dest.writeLong(timeOffset.getTime());
		
		
	}
	

}
