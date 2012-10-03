package com.example.my.first.app;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import com.example.sensor.data.DataStorage;

public class MenuActivity extends Activity {

	private final static String[] NUMBERSOFINTEREST = { "+61431220285" };
	private String[] adapterFill = new String[0];
	private String[] data = new String[0];
	private ListView list;
	private String[] numbers = new String[0];
	private DataStorage storage = new DataStorage();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		
		
		//TODO Progressbar with another thread
		list = (ListView) findViewById(R.id.listView1);
	
		
		
	}
	


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
	}

	public void saveSms(String write) {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWritable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWritable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWritable = false;
		}

		String FILENAME = "smsdata";
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(getExternalFilesDir(null)+"/smsdata.txt",true));
				out.write(write + "\n");
			out.close();
		} catch (IOException e) {
			System.out.println("File/Write problems");
		}

	}

/**	@TargetApi(9)
	public int getSms(Context cx) {
		Uri smsInbox=Uri.parse("content://sms/inbox");
		Cursor curs = cx.getContentResolver().query(
				smsInbox, null, null, null, null);

		int count = curs.getCount();
		data = new String[count];
		numbers = new String[count];
		String[] searchNumbers = new String[count];
		int interestCount = 0;

		//reading sms from phone
		if (curs.moveToFirst()) {
			for (int i = 0; i < 3; i++) {  //<curs.getCount()
				try {
					searchNumbers[i] = curs.getString(curs
							.getColumnIndex("address"));
					boolean check = false;
					for (int j = 0; j < NUMBERSOFINTEREST.length; j++) {
						if (searchNumbers[i].equals(NUMBERSOFINTEREST[j])) {
							check = true;
						}
					}
					if (check) {
						//copy data for decoding
						numbers[interestCount] = searchNumbers[i];
						data[interestCount] = curs.getString(curs.getColumnIndex("body"));
						

						//save and remove from phone
						
						String id=curs.getString(curs.getColumnIndex("_id"));
						saveSms(curs.getString(curs.getColumnIndex("date"))+"/"+numbers[interestCount] + "/" + data[interestCount]);
						cx.getContentResolver().delete(Uri.parse("content://sms"), "_id=?", new String[] {id});
						
						interestCount++;
					}
					curs.moveToNext();
				} catch (IllegalArgumentException e) {

					System.out.println("No Sms in Database.");
				}
			}
		}
		curs.close();
		
		//decoding sms
		int fatalCount=0;

		DatabaseControl db = new DatabaseControl(this);
		db.open();
		
		for (int i=0; i <interestCount; i++) 
		{
			DataSet neu = null;
			
			try {
				neu = storage.addNewDataSet(DataCompression.decode(data[i]),numbers[i]);
				//database save
				
			} catch (DecodeFatalException e) {
				Toast.makeText(cx, e.toString(), Toast.LENGTH_LONG).show();
				fatalCount++;
				continue;
			}catch (DecodeRecoverException e) {
				Toast.makeText(cx, "DataSet "+i+": "+ e.toString(), Toast.LENGTH_SHORT).show();
				
				neu=storage.addNewDataSet(((DecodeRecoverException) e).getDataInts(),numbers[i]);
			} catch (DecodeException e) {			//unreachable}
			}
			
			//----------------------------------------------------------------------------------------------------------------------------------------
			//try to minimize extra effort in dataset, maybe dismiss datastorage, maybe not, since will be used rarely
			//datenaufnahme, siehe zettel, abarbeiten
			//set default phenomenas here or in databasecontrol
			//----------------------------------------------------------------------------------------------------------------------------------------
			
			
			
			}

			long dbId = db.insertPlatform(0, 0, 0, 30, "+6142536182");
			long sensorId = db.insertSensor(0, 0, 0, (int) dbId);


			long subsensorId = db.insertSubsensor((int) 2,(int) sensorId);
			long subsensorId2 = db.insertSubsensor((int) 1,(int) sensorId);
			
			db.insertMeasurement((int) subsensorId2, 
				new float[] { 4678f, 3567.7f, 678.5f, 678.8f},
				new long[] {1111111,2222222,333333,444444444});
		
		
			
			db.close();
		
		
		
		//filling adapter with successfull d0ecoded datasets
		adapterFill = new String[storage.size()];		//TODO change to ArrayList, if adding service checking for new SMS
		for (int i=0; i<storage.size(); i++)
		{
			adapterFill[i] = storage.getDatabyLocalId(i).toString();
		}
		return interestCount-fatalCount;
	}

	public void plotPlots(View view) {
		ArrayList<DataSet> selected =  new ArrayList<DataSet>();
		SparseBooleanArray checked = list.getCheckedItemPositions();
		for (int i=checked.size();i>=0;i--)
		{
			  if (checked.valueAt(i)) selected.add(storage.getDatabyLocalId(checked.keyAt(i)));
		}
		if (selected.size()>0) {
			Intent myIntent = new Intent(this, PlotActivity.class);
			myIntent.putParcelableArrayListExtra("selected", selected);
			startActivity(myIntent);
		} else {
			Toast.makeText(getApplicationContext(), "No data selected!", Toast.LENGTH_LONG).show();
		}
	}*/
}
