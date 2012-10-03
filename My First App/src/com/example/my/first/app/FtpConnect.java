package com.example.my.first.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FtpConnect {

	public static final String password="UWAsensor";
	public static final String serverUrl="ftp-web.ohost.de";
	public static final String userName="ftp1881722";

	public static void downloadDb(FileOutputStream database) throws IOException,MalformedURLException {
       try {
	    	FTPClient con = openFTP();
			con.retrieveFile(DatabaseControl.DATABASE_NAME, database);
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
	    con.enterLocalPassiveMode();
		return con;
		
	}
	
	
	
	
	public static void uploadDb(InputStream bis) throws IOException,MalformedURLException {
	    try {
	    	FTPClient con = openFTP();
			con.storeFile(DatabaseControl.DATABASE_NAME, bis);
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
