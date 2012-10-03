package com.example.my.first.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FtpConnect {

	public static final String password="W5ne+wrk";
	public static final String serverUrl="ftp.csse.uwa.edu.au";
	public static final String userName="wsnetwork";
	public static final String subfolder="sensordata/applicationData/databases/";
	
	public static void downloadDb(FileOutputStream database) throws IOException,MalformedURLException {
       try {
	    	FTPClient con = openFTP();

	    	FTPFile[] files = con.listFiles();
	    	if (files==null || files.length==0) { //check whether no files have been found, unsure what listFiles would return in that case
	    		throw new IOException("no database found on the server");
	    	}
	    	Date[] dates= new Date[files.length];
	    	for (int i=0; i<files.length;i++) {
	    		dates[i]=files[i].getTimestamp().getTime();
	    	}
	    	Date maximumDate=dates[0];
	    	int maximumCount=0;
	    	for (int i=1; i<files.length;i++) {
	    		if (dates[i].after(maximumDate)) {
	    			maximumDate=dates[i];
	    			maximumCount=i;
	    		}
	    	}
	    	String remote=files[maximumCount].getName();
			con.retrieveFile(remote, database);
			con.logout();
		} finally {
	        if (database != null)
	           try
	           {
	              database.close();
	           }
	           catch (IOException ioe)
	           {
	              ioe.printStackTrace();
	           }
	     }
	}
	
	private static URL getServerUrl() throws MalformedURLException {
		StringBuilder sb= new StringBuilder("ftp://");
		sb.append(userName);
		sb.append(":");
		sb.append(password);
		sb.append("@");
		sb.append(serverUrl);
		return new URL(sb.toString());
	}
	
	public static FTPClient  openFTP() throws SocketException, IOException {
		FTPClient con =new FTPClient();
	    con.connect(serverUrl,21);
	    con.login(userName, password);
	    con.setFileType(FTP.BINARY_FILE_TYPE);
	    
	    //change to or create subfolder directory if it does not exist
	    String[] directories = subfolder.split("/");
	    for (String dir : directories ) {
	        if (!dir.isEmpty() && con.changeWorkingDirectory(dir)) {
	        	continue;
	        } else {
	          if (!con.makeDirectory(dir)) {	//try to create it
	            throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + con.getReplyString()+"'");
	          } //then try to change to it
	          if (!con.changeWorkingDirectory(dir)) {
	            throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + con.getReplyString()+"'");
	          }
	        }
	    }
	        
	  

	    con.enterLocalPassiveMode();
		return con;
		
	}
	
	
	
	
	public static void uploadDb(InputStream bis, String uniqueIdentifier) throws IOException,MalformedURLException {
	    try {
	    	FTPClient con = openFTP();
	    	con.storeFile(DatabaseControl.DATABASE_NAME+uniqueIdentifier, bis);
			con.logout();
		} finally {
	        if (bis != null)
	           try
	           {
	              bis.close();
	           }
	           catch (IOException ioe)
	           {
	              ioe.printStackTrace();
	           }
	     }
	}

}
