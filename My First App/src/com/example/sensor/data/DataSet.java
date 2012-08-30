package com.example.sensor.data;

import 	   java.util.Date;
import		java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View.MeasureSpec;

public class DataSet implements Parcelable{
	

//TODO constants
private Date date;
private int localId;	//necessary for handling by android-app
private long id;
private int[][] data;

public static final Parcelable.Creator CREATOR =
new Parcelable.Creator() {
    public DataSet createFromParcel(Parcel in) {
        return new DataSet(in);
    }

    public DataSet[] newArray(int size) {
        return new DataSet[size];
    }
};

	
//extract the data for each sensor from decoded c-code, form one array per sensor
	public DataSet(int[][] data, long date, long id, int localId)
	{	
		this.data = data;//new Integer[columnCount][rowCount];  
		this.date= new Date(date);
		this.id=id;
		this.localId=localId;
		
		
	}
	
	
	public DataSet(Parcel in) {
		readFromParcel(in);
	}


	@Override
	public String toString() {
		//set different time format here or on construct
		return Integer.toString(localId)+")  from  +"+Long.toString(id)+"   "+ date.toString();
	}


	public int getLocalId() {
		return localId;
	}


	public void setLocalId(int localId) {
		this.localId = localId;
	}


	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Integer[] getTempData(int k)
	{
		Integer[] result = new Integer[data.length-1];
		if (k<4 && k>=0) {		
			//return Integer array
			for (int i=1; i < data.length; i++) {
				try {
				result[i-1] = new Integer(data[i][k]);
				} catch (NullPointerException e) {		//evntl NullPointer fangen
					e.printStackTrace();				
				}
			}
		}
		
		return result;		
	}
	
	public Integer[] getMoistData(int k)
	{
		Integer[] result = new Integer[data.length-1];
		if (k<4 && k>=0) {		
			//return Integer array
			for (int i=1; i < data.length; i++) {
				try {
				result[i-1] = new Integer(data[i][k+4]);
				} catch (NullPointerException e) {		//evntl NullPointer fangen
					e.printStackTrace();				
				}
			}
		} else {
			Arrays.fill(result, 0);		//out of range calls get back zeros
		}
		return result;		
	}


	public int describeContents() {
		return 0;
	}


	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(data.length);
		for (int i=0;i<data.length;i++)
		{
			dest.writeIntArray(data[i]);
		}
		dest.writeInt(localId);
		dest.writeLong(id);
		dest.writeLong(date.getTime());
		
	}
	
	private void readFromParcel(Parcel in) {
		int length=in.readInt();
		for (int i=0;i<length;i++)
		{
			data[i]=in.createIntArray();
		}
		localId=in.readInt();
		id=in.readLong();
		date=new Date(in.readLong());
	}
	

}
