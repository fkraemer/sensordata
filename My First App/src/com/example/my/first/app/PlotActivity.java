package com.example.my.first.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.androidplot.series.XYSeries;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.widget.TitleWidget;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.*;

import android.util.FloatMath;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import java.sql.Time;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import com.example.my.first.app.ChooseTimeActivity.getTimeTask;
import com.example.sensor.data.DataSet;
import com.example.sensor.data.DataStorage;

@TargetApi(9)
public class PlotActivity extends Activity {

	final private float MIN_X_DISTANCE = 2.0f * 60 * 60 * 1000; // minimum of shown time range (in millis)
	final private float TEMP_MIN_Y_DISTANCE = 3.0f; // minimum of shown temperature Range
	final private float MOIST_MIN_Y_DISTANCE = 3.0f; // minimum of shown temperature Range

	private XYPlot temperatureSimpleXYPlot;
	private XYPlot moistureSimpleXYPlot;
	private TextView txt1;
	private TextView txt2;
	private TextView txt3;
	private LockableScrollView scroll;
	private Integer[] column;
	private PointF minXY;
	private PointF maxXY;


	private DataService dataService;
	private Cursor curs=null;
	private final CountDownLatch latch = new CountDownLatch(1);
	private MyConnection mConnect = new MyConnection(latch);
	private Context cx;
	boolean wasSetOnce=false;
	private long platformId; 
	private long timeMin;
	private long timeMax;

	private static final int TEMP_COUNT=4;
	private static final int MOIST_COUNT=4;
	private ArrayList<Float>[] tempLists=new ArrayList[TEMP_COUNT];
	private ArrayList<Float>[] moistLists=new ArrayList[MOIST_COUNT];
	private ArrayList<Float> batList=new ArrayList<Float>();
	private ArrayList<Long> timeList=new ArrayList<Long>();
	
	// very simple, Y-values only, filling the temperature plot
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras =getIntent().getExtras();
		platformId=extras.getLong("platformId");
		timeMin=extras.getLong("minTime");
		timeMax=extras.getLong("maxTime");

		setContentView(R.layout.plot);
		scroll = (LockableScrollView) findViewById(R.id.scroll);
		txt1 = (TextView)findViewById(R.id.txtview1);
		txt2 = (TextView)findViewById(R.id.txtview2);
		txt3 = (TextView)findViewById(R.id.txtview3);
		for (int i=0;i<TEMP_COUNT;i++) {
		//maybe do later, then save some resources by assuring size up front
			tempLists[i]=new ArrayList<Float>();
		}
		for (int i=0;i<MOIST_COUNT;i++) {
			//maybe do later, then save some resources by assuring size up front
				moistLists[i]=new ArrayList<Float>();
			}
		temperatureSimpleXYPlot = (XYPlot) findViewById(R.id.temperatureXYPlot);
		moistureSimpleXYPlot = (XYPlot) findViewById(R.id.moistureXYPlot);


		//set plot properties
		Format timeFormat =new Format() {
			// create a simple date format that draws the hours
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH");

			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

				//typecast to Date, since we are just giving dates
				Date date = new Date(((Number) obj).longValue());
				return dateFormat.format(date, toAppendTo, pos);
			}
			@Override
			public Object parseObject(String string, ParsePosition position) {
				return null;
			}
		};
		
		//setting up the graphs
		temperatureSimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(1);
		temperatureSimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(1);
		temperatureSimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("###.#"));
		temperatureSimpleXYPlot.getGraphWidget().setDomainValueFormat(timeFormat);
		temperatureSimpleXYPlot.getTitleWidget().setWidth(300f);
		temperatureSimpleXYPlot.setRangeLabel("Temperature");
		temperatureSimpleXYPlot.setDomainLabel("Time");
		temperatureSimpleXYPlot.disableAllMarkup();
		temperatureSimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL,  1);
		temperatureSimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL,  60*60*1000);			//set grid line difference with 1hour difference

		moistureSimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
		moistureSimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
		moistureSimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("###.#"));
		moistureSimpleXYPlot.getGraphWidget().setDomainValueFormat(timeFormat);
		temperatureSimpleXYPlot.getTitleWidget().setWidth(300f);
		moistureSimpleXYPlot.setRangeLabel("Moisture");
		moistureSimpleXYPlot.setDomainLabel("Time");
		moistureSimpleXYPlot.disableAllMarkup();
		}
	
		protected void onStart() {
	        super.onStart();
	        // Bind to LocalService, happens in UI-thread, watch time delays !!
	        Intent intent =  new Intent(this, DataService.class);
			bindService(intent, mConnect,0);
	        if (!wasSetOnce) {
	        	new getPlotsTask().execute(null,null,null);
	        	wasSetOnce=true;
	        }
	        
	    }

		class getPlotsTask extends AsyncTask<Void, Void, Void> {


			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dataService=mConnect.getService();
				
				//get the 9 sensordatastreams, put them into the lists
				Cursor subsensors=dataService.getSubsensorsByPlatformId(platformId);
				for (int k=0;k<MOIST_COUNT;k++){
					long subsensorId = subsensors.getLong(subsensors.getColumnIndex(DatabaseControl.KEY_ID));
					Cursor measurementsC =dataService.getMeasuremntByInterval(timeMin, timeMax, subsensorId);
					for (int i=0;i<measurementsC.getCount();i++) {
						moistLists[k].add(measurementsC.getFloat(measurementsC.getColumnIndex(DatabaseControl.KEY_VALUE)));
						if (k==0) 
							timeList.add(measurementsC.getLong(measurementsC.getColumnIndex(DatabaseControl.KEY_TIMESTAMP)));
						measurementsC.moveToNext();
					}
					subsensors.moveToNext();
				}
				for (int k=0;k<TEMP_COUNT;k++){
					long subsensorId = subsensors.getLong(subsensors.getColumnIndex(DatabaseControl.KEY_ID));
					Cursor measurementsC =dataService.getMeasuremntByInterval(timeMin, timeMax, subsensorId);
					for (int i=0;i<measurementsC.getCount();i++) {
						tempLists[k].add(measurementsC.getFloat(measurementsC.getColumnIndex(DatabaseControl.KEY_VALUE)));
						measurementsC.moveToNext();
					}
					subsensors.moveToNext();
				}
				long subsensorId = subsensors.getLong(subsensors.getColumnIndex(DatabaseControl.KEY_ID));
				Cursor measurementsC =dataService.getMeasuremntByInterval(timeMin, timeMax, subsensorId);
				for (int i=0;i<measurementsC.getCount();i++) {
					batList.add(measurementsC.getFloat(measurementsC.getColumnIndex(DatabaseControl.KEY_VALUE)));
					measurementsC.moveToNext();
				}
				return null;
			}
			//option to update progressbar via this thread
				
			//-------------------------------------------------------------------------------------------------------------------------------
			protected void onPostExecute(Void result) {
				//setting up the lines
				for (int k=0;k<MOIST_COUNT;k++){
					XYSeries moistSeries = new SimpleXYSeries(moistLists[k],timeList,"Moisture Series");
				//	moistureSimpleXYPlot.addSeries(moistSeries, getFormat(k));
				}
				for (int k=0;k<TEMP_COUNT;k++){
					XYSeries tempSeries = new SimpleXYSeries(tempLists[k],timeList,"Temperature Series");
					temperatureSimpleXYPlot.addSeries(tempSeries, getFormat(k));
				}
				//TODO visualize battery
				
				setupTouch();
			//	insertMidnightLines();

				moistureSimpleXYPlot.redraw();
				temperatureSimpleXYPlot.redraw();

			}

		}
		
		private void setupTouch() {
			float ABS_Y_MIN;
			float ABS_Y_MAX;
			float MAX_Y_DISTANCE;
			float ABS_X_MIN;
			float ABS_X_MAX;
			float MAX_X_DISTANCE;	
	
			//getting the max/min points
			temperatureSimpleXYPlot.calculateMinMaxVals();
			minXY=new PointF(temperatureSimpleXYPlot.getCalculatedMinX().floatValue(),
					temperatureSimpleXYPlot.getCalculatedMinY().floatValue());
			maxXY=new PointF(temperatureSimpleXYPlot.getCalculatedMaxX().floatValue(),
					temperatureSimpleXYPlot.getCalculatedMaxY().floatValue());
			float dif=maxXY.y-minXY.y; //setting the maximum shown y-range to be a minimum range of 15
			if (dif<15) {
				dif=15-dif;
			} else dif=5;	//otherwise just add 2.5 on both sides
			ABS_Y_MIN=minXY.y-dif/2;
			ABS_Y_MAX=maxXY.y+dif/2;
			MAX_Y_DISTANCE=ABS_Y_MAX - ABS_Y_MIN;
			temperatureSimpleXYPlot.setRangeBoundaries(minXY.y,maxXY.y, BoundaryMode.FIXED);
			//saetting the absolute borders of the plot
			ABS_X_MIN=minXY.x;
			ABS_X_MAX=maxXY.x;	//shown time(y-axis) becomes a maximum of 48hours
			MAX_X_DISTANCE=((ABS_X_MAX-ABS_X_MIN)<48.0f*60*60*1000) ? (ABS_X_MAX-ABS_X_MIN) : 48.0f*60*60*1000;
			temperatureSimpleXYPlot.setDomainBoundaries(minXY.x,maxXY.x, BoundaryMode.FIXED);	//start with most recent (max 48)hours
	
			plotTouch tempTouch= new plotTouch(temperatureSimpleXYPlot,ABS_X_MIN,ABS_X_MAX,ABS_Y_MIN,ABS_Y_MAX,
					MAX_X_DISTANCE,MIN_X_DISTANCE,TEMP_MIN_Y_DISTANCE,MAX_Y_DISTANCE,scroll);
	
			moistureSimpleXYPlot.calculateMinMaxVals();
			minXY=new PointF(moistureSimpleXYPlot.getCalculatedMinX().floatValue(),
					moistureSimpleXYPlot.getCalculatedMinY().floatValue());
			maxXY=new PointF(moistureSimpleXYPlot.getCalculatedMaxX().floatValue(),
					moistureSimpleXYPlot.getCalculatedMaxY().floatValue());
			dif=maxXY.y-minXY.y; //setting the maximum shown y-range to be a minimum range of 15
			if (dif<150) {		//TODO fidatat values in
				dif=150-dif;
			} else dif=50;
			ABS_Y_MIN=(minXY.y-dif/2 < 0) ? 0 : minXY.y-dif/2; //dont allow negative values, never measured
			ABS_Y_MAX=(minXY.y-dif/2 < 0) ? dif : maxXY.y+dif/2; //upper bound keeps dif distance to lower 
			MAX_Y_DISTANCE=ABS_Y_MAX - ABS_Y_MIN;
			
			//max_x_values can be reused from temperature
			moistureSimpleXYPlot.setRangeBoundaries(minXY.y,maxXY.y, BoundaryMode.FIXED);
			
			plotTouch moistTouch= new plotTouch(moistureSimpleXYPlot,ABS_X_MIN,ABS_X_MAX,ABS_Y_MIN,ABS_Y_MAX,
					MAX_X_DISTANCE,MIN_X_DISTANCE,MOIST_MIN_Y_DISTANCE,MAX_Y_DISTANCE,scroll);
			

			temperatureSimpleXYPlot.setOnTouchListener(tempTouch);
		//	moistureSimpleXYPlot.setOnTouchListener(moistTouch);
		}

	private void insertMidnightLines() { 
		//prepare: draw 24h lines (every midnight)
		Long[] midnights = lookForMidnight();
		// filling plots with midnight borders
		for (int i = 0; i < midnights.length; i++) {
			XYSeries midnightSeriesTemp = new SimpleXYSeries(
					Arrays.asList(new Long[] { midnights[i], midnights[i] }),
					Arrays.asList(new Float[] { -55f, 100f }), "");
			XYSeries midnightSeriesMoist = new SimpleXYSeries(
					Arrays.asList(new Long[] { midnights[i], midnights[i] }),
					Arrays.asList(new Float[] { -5f, 1005f }), "");

			LineAndPointFormatter series1Format = new LineAndPointFormatter(
					Color.RED, // line
					Color.RED, // point color
					null); // fill Color
			Paint paint = series1Format.getLinePaint();
			paint.setStrokeWidth(1);
			series1Format.setLinePaint(paint);

			temperatureSimpleXYPlot.addSeries(midnightSeriesTemp, series1Format);
			moistureSimpleXYPlot.addSeries(midnightSeriesMoist, series1Format);
		}
		
		
	}

	private Long[] lookForMidnight() {		
		// compute number of days:
		int difDays = (int) (timeMax - timeMin) / (24 * 60 * 60 * 1000);
		Long[] result = new Long[difDays+1];
		for (int i = 0; i < difDays+1; i++) {
			Calendar c = Calendar.getInstance();
			Date process = new Date(timeMin + i * 24 * 60 * 60 * 1000);
			c.setTime(process);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);// accuracy of one minute is enough
			long possibleMidnight = c.getTimeInMillis();
			if (possibleMidnight<timeMax) result[i]=possibleMidnight;
		}
		return result;
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
	    	new getPlotsTask().execute(null,null,null);
	    	wasSetOnce=true;
		}
	}

	private LineAndPointFormatter getFormat(int i) {

		int colour = 0;
		switch (i) {
		case 0:
			colour = Color.WHITE;
			break;
		case 1:
			colour = Color.GRAY;
			break;
		case 2:
			colour = Color.RED;
			break;
		case 3:
			colour = Color.CYAN;
			break;
		}

		LineAndPointFormatter series1Format = new LineAndPointFormatter(
				Color.GREEN, // line
				colour, // point color
				null); // fill Color
		Paint paint = series1Format.getLinePaint();
		paint.setStrokeWidth(3);
		series1Format.setLinePaint(paint);
		return series1Format;

	}
}
