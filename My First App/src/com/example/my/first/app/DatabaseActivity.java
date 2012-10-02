package com.example.my.first.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.net.ftp.FTPClient;

import com.example.my.first.app.ChooseTimeActivity.getTimeTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.drm.ProcessedData;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DatabaseActivity extends Activity {

	private static final int DIALOG_WORKING_ID = 0;
	private static final int DIALOG_FAILURE_ID = 1;
	private static final int DIALOG_SUCCESS_ID = 2;
	private DataService dataService;
	private final CountDownLatch latch = new CountDownLatch(1);
	private MyConnection mConnect = new MyConnection(latch);
	private Context cx;
	boolean wasSetOnce=false;
	private TextView text;
	
	private Button uploadButton;
	private Button downloadButton;
	private Button backupExternal;
	private Button getExternal;
	private String failureMessage="";
	private String successMessage="";

	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.database);
		cx=getApplicationContext();
		text = (TextView) findViewById(R.id.textViewDatabase);
		//disable the buttons till dataservice is connected
		(uploadButton = (Button) findViewById(R.id.backupWeb)).setEnabled(false);
		(downloadButton = (Button) findViewById(R.id.download)).setEnabled(false);
		(backupExternal = (Button) findViewById(R.id.backupExternal)).setEnabled(false);
		(getExternal = (Button) findViewById(R.id.restoreExternal)).setEnabled(false);
		
		//getting the platform to work on
		Bundle extras = getIntent().getExtras();
			
        // Bind to LocalService, happens in UI-thread, watch time delays !!
        Intent intent =  new Intent(this, DataService.class);
		bindService(intent, mConnect,0);
	}
	
	@SuppressWarnings("deprecation") // both just since API11
	protected void onStart() {
        super.onStart();
        if (!wasSetOnce) {
        	new getTimeTask().execute(null,null,null);
        	wasSetOnce=true;
        }
    }

	class getTimeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			dataService=mConnect.getService();
			
			
			//could be used to retrieve dynamic information from the database
			
			return null;
		}
		
		protected void onPostExecute(Void result) {
			//activate buttons
			uploadButton.setEnabled(true);
			downloadButton.setEnabled(true);
			backupExternal.setEnabled(true);
			getExternal.setEnabled(true);
		}
	}
	
    protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (mConnect.isBound()) {
            unbindService(mConnect);
        }
        wasSetOnce=false;	//flag to execute listupdate on next startup
    }

	protected void onResume() {
		super.onResume();
		//updating the data on resume
		if (!wasSetOnce) {
			new getTimeTask().execute(null,null,null);
			wasSetOnce=true;
        }
	}

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		Button responses
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	public void backupWeb(View view) {
		new UploadTask().execute(null,null,null);
	}
	
	public void download(View view) {
		new DownloadTask().execute(null,null,null);
	}

	public void backupExternal(View view) {
		if (dataService.backupToExternal()) {
			successMessage="Successfully backed up database to SD.";
			showDialog(DIALOG_SUCCESS_ID);
		} else {
			failureMessage="Problems occured backing up to SD.";
			showDialog(DIALOG_FAILURE_ID);
		}
		
	}
	

	public void restoreExternal(View view) {
		if (dataService.restoreFromExternal()) {
			successMessage="Successfully retrieved database from SD.";
			showDialog(DIALOG_SUCCESS_ID);
		} else {
			failureMessage="Retrieving database not possible.";
			showDialog(DIALOG_FAILURE_ID);
		}
	}

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		Tasks (API 11 cant do network related processes in the main thread)
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class UploadTask extends AsyncTask<Void, Void, Exception> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WORKING_ID);
		}
		
		@Override
		protected Exception doInBackground(Void... params) { 
			try {
				dataService.backupDbToWeb();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e;
			}
			return null; 
		}

		@Override
		protected void onPostExecute(Exception result) {
			if (result==null) {
				successMessage="Successfully uploaded the database.";
				showDialog(DIALOG_SUCCESS_ID);
			} else {
				failureMessage=result.getLocalizedMessage();
				showDialog(DIALOG_FAILURE_ID);
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		
	}
	
	
	class DownloadTask extends AsyncTask<Void, Void, Exception> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WORKING_ID);
		}
		
		@Override
		protected Exception doInBackground(Void... params) { 
			try {
				dataService.downloadDb();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e;
			}
			return null; 
		}

		@Override
		protected void onPostExecute(Exception result) {
			if (result==null) {
				successMessage="Successfully downloaded the database.";
				showDialog(DIALOG_SUCCESS_ID);
			} else {
				//setup displayed message
				failureMessage=result.getLocalizedMessage();
				showDialog(DIALOG_FAILURE_ID);
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		
	}

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		Dialogs
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case (DIALOG_WORKING_ID):
			dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setTitle("Working");
		break;
		case (DIALOG_FAILURE_ID):
			builder.setMessage(failureMessage)
				.setTitle("Failure")
				.setNeutralButton("Ok", new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
					}
				});
			dialog=builder.create();
		break;
		case (DIALOG_SUCCESS_ID): 
			 builder.setTitle("Success")
			 	.setMessage(successMessage)
			 	.setNeutralButton("Ok", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DatabaseActivity.this.finish();
					}
				}); 
			dialog=builder.create();
		break;
		}
		return dialog;		
	}

}