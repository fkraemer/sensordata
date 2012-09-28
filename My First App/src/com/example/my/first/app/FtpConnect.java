package com.example.my.first.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FtpConnect {

	public static final String serverUrl="ftp-web.ohost.de";
	public static final String userName="ftp1881722";
	public static final String password="UWAsensor";

	private static URL getServerUrl() throws MalformedURLException {
		StringBuilder sb= new StringBuilder("ftp://");
		sb.append(userName);
		sb.append(":");
		sb.append(password);
		sb.append("@");
		sb.append(serverUrl);
		return new URL(sb.toString());
	}
	
	public static void  transfer(BufferedInputStream bis,BufferedOutputStream bos) throws IOException {
		try {
        byte[] buffer =new byte[1024];
	       int length=0;
	       while((length=bis.read(buffer)) > -1) {
	    	   bos.write(buffer,0,length);
	       } 
	     }
	     finally
	     {
	        if (bis != null)
	           try
	           {
	              bis.close();
	           }
	           catch (IOException ioe)
	           {
	              ioe.printStackTrace();
	           }
	        if (bos != null)
	           try
	           {
	              bos.close();
	           }
	           catch (IOException ioe)
	           {
	              ioe.printStackTrace();
	           }
	     }
	}
	
	public static void uploadDb(InputStream database) throws IOException,MalformedURLException {
	       
		URLConnection urlC = getServerUrl().openConnection();

       BufferedInputStream bis = new BufferedInputStream( database );
       BufferedOutputStream bos = new BufferedOutputStream( urlC.getOutputStream() );
       transfer(bis,bos);
	}
	
	
	
	
	public static void downloadDb(FileOutputStream database) throws IOException,MalformedURLException {

       URLConnection urlc = getServerUrl().openConnection();

       BufferedInputStream bis = new BufferedInputStream( urlc.getInputStream() );
       BufferedOutputStream bos = new BufferedOutputStream( database );
       transfer(bis, bos);
	}

}
