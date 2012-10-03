package com.example.my.first.app;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author flo
 *
 */
public class DatabaseActivity extends Activity {

	class DownloadTask extends AsyncTask<Void, Void, Exception> {

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
		protected void onCancelled() {
			//TODO, dont interrupt replacing the internal database! rely on professional users for now...
			super.onCancelled();
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
		protected void onPreExecute() {
			showDialog(DIALOG_WORKING_ID);
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
		
		@Override
		protected void onPostExecute(Void result) {
			//activate buttons
			uploadButton.setEnabled(true);
			downloadButton.setEnabled(true);
			backupExternal.setEnabled(true);
			getExternal.setEnabled(true);
		}
	}
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		Tasks (API 11 cant do network related processes in the main thread)
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	class UploadTask extends AsyncTask<Void, Void, Exception> {

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
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
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
		protected void onPreExecute() {
			showDialog(DIALOG_WORKING_ID);
		}

		
	}
	private static final int DIALOG_FAILURE_ID = 1;
	private static final int DIALOG_SUCCESS_ID = 2;
	private static final int DIALOG_WORKING_ID = 0;
	private Button backupExternal;
	private AsyncTask currentTask;
	private Context cx;
	
	private DataService dataService;
	private Button downloadButton;
	private String failureMessage="";
	private Button getExternal;
	private final CountDownLatch latch = new CountDownLatch(1);
	private MyConnection mConnect = new MyConnection(latch);
	
	private String successMessage="";

	
	private TextView text;
	
	private Button uploadButton;

	boolean wasSetOnce=false;
	
    public void backupExternal(View view) {
		if (dataService.backupToExternal()) {
			successMessage="Successfully backed up database to SD.";
			showDialog(DIALOG_SUCCESS_ID);
		} else {
			failureMessage="Problems occured backing up to SD.";
			showDialog(DIALOG_FAILURE_ID);
		}
		
	}

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		Button responses
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	public void backupWeb(View view) {
		currentTask = new UploadTask().execute(null,null,null);
	}

	public void download(View view) {
		currentTask = new DownloadTask().execute(null,null,null);
	}
	
	@Override
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

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		Dialogs
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case (DIALOG_WORKING_ID):
			ProgressDialog dial = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		dial.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					currentTask.cancel(true);					
				}
			});
			dial.setCancelable(false);
			dial.setTitle("Working");
			dialog=dial;
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
	

	@Override
	protected void onResume() {
		super.onResume();
		//updating the data on resume
		if (!wasSetOnce) {
			new getTimeTask().execute(null,null,null);
			wasSetOnce=true;
        }
	}

	@Override
	@SuppressWarnings("deprecation") // both just since API11
	protected void onStart() {
        super.onStart();
        if (!wasSetOnce) {
        	new getTimeTask().execute(null,null,null);
        	wasSetOnce=true;
        }
    }
	
	
	@Override
	protected void onStop() {
        super.onStop();
        // Unbind from the dataservice
        if (mConnect.isBound()) {
            unbindService(mConnect);
        }
        wasSetOnce=false;	//flag to execute listupdate on next startup
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

}