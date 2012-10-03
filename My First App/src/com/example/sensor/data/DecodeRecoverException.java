package com.example.sensor.data;

public class DecodeRecoverException extends DecodeException {
	
	private DataSet data=null;
	private int [][] dataInts;
	
	public DecodeRecoverException() {
		super();
	}
	
	public DecodeRecoverException(String detailMessage) {
		super(detailMessage);
	}

	public DecodeRecoverException(String detailMessage,DataSet data) {
		super(detailMessage);
		this.data=data;
	}

	public DecodeRecoverException(String detailMessage,int[][] dataInts) {
		super(detailMessage);
		this.dataInts=dataInts;
	}

	public DataSet getData() {
		return data;
	}

	public int[][] getDataInts() {
		return dataInts;
	}

	public void setData(DataSet data) {
		this.data = data;
	}

	public void setDataInts(int[][] dataInts) {
		this.dataInts = dataInts;
	}
	
}
