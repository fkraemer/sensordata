package com.example.my.first.app;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.LabeledIntent;
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

	// very simple, Y-values only, filling the temperature plot
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras =getIntent().getExtras();
		ArrayList<DataSet> selected=extras.getParcelableArrayList("selected");
		//TODO make sure right order: the most recent messages have the lower index

		setContentView(R.layout.plot);
		scroll = (LockableScrollView) findViewById(R.id.scroll);
		txt1 = (TextView)findViewById(R.id.txtview1);
		txt2 = (TextView)findViewById(R.id.txtview2);
		txt3 = (TextView)findViewById(R.id.txtview3);
		temperatureSimpleXYPlot = (XYPlot) findViewById(R.id.temperatureXYPlot);
		moistureSimpleXYPlot = (XYPlot) findViewById(R.id.moistureXYPlot);


		//set plot properties
		Format timeFormat =new Format() {

			// create a simple date format that draws on the hours
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH");

			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

				//typecast to Date, since we are just giving dates
				Date date = new Date(((Number) obj).longValue());
				return dateFormat.format(date, toAppendTo, pos);
			}


			public Object parseObject(String source, ParsePosition pos) {
				return null;

			}

		};

		
		temperatureSimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(1);
		temperatureSimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(1);
		temperatureSimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("###.#"));
		temperatureSimpleXYPlot.getGraphWidget().setDomainValueFormat(timeFormat);
		temperatureSimpleXYPlot.getTitleWidget().setWidth(300f);
		temperatureSimpleXYPlot.setRangeLabel("Temperature");
		temperatureSimpleXYPlot.setDomainLabel("Time");
		temperatureSimpleXYPlot.disableAllMarkup();
		//TODO make grid flexible, move with zoom/scroll
		temperatureSimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL,  1);
		temperatureSimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL,  60*60*1000);			//set grid line difference with 1hour difference

		//		storage.getDatabyLocalId(selected.get(0)).getTimeOffset().toMillis(true) / 3600000);

		moistureSimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
		moistureSimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
		moistureSimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("###.#"));
		moistureSimpleXYPlot.getGraphWidget().setDomainValueFormat(timeFormat);
		temperatureSimpleXYPlot.getTitleWidget().setWidth(300f);
		moistureSimpleXYPlot.setRangeLabel("Moisture");
		moistureSimpleXYPlot.setDomainLabel("Time");
		moistureSimpleXYPlot.disableAllMarkup();



		float timeOffset = selected.get(0).getTimeOffset().getTime();	//set fixed time offset
		float timeStamp = selected.get(0).getDate().getTime();
		for (int k=0;k<selected.size();k++){
			DataSet data = selected.get(k);

			for (int j = 0; j < 4; j++) {

				Float[] tempColumn = data.getTempData(j);
				Float[] moistColumn = data.getMoistData(j);
				Float[] timeColumn = new Float[tempColumn.length];
				// generating timestamps
				for (int i = 0; i < tempColumn.length; i++) {
					timeColumn[i] = timeStamp + timeOffset * i + k * timeOffset * 24;	//TODO dont hardcode measure count or change to  individual timeStamps
				}

				XYSeries tempSeries = new SimpleXYSeries(
						Arrays.asList(timeColumn), Arrays.asList(tempColumn),
						"Temperature Series");
				XYSeries moistSeries = new SimpleXYSeries(
						Arrays.asList(timeColumn), Arrays.asList(moistColumn),
						"Moisture Series");

				int colour = 0;
				switch (j) {
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

				temperatureSimpleXYPlot.addSeries(tempSeries, series1Format);
				moistureSimpleXYPlot.addSeries(moistSeries, series1Format);
			}

		}
		float ABS_Y_MIN;
		float ABS_Y_MAX;
		float MAX_Y_DISTANCE;
		float ABS_X_MIN;
		float ABS_X_MAX;
		float MAX_X_DISTANCE;	

		temperatureSimpleXYPlot.calculateMinMaxVals();
		minXY=new PointF(temperatureSimpleXYPlot.getCalculatedMinX().floatValue(),
				temperatureSimpleXYPlot.getCalculatedMinY().floatValue());
		maxXY=new PointF(temperatureSimpleXYPlot.getCalculatedMaxX().floatValue(),
				temperatureSimpleXYPlot.getCalculatedMaxY().floatValue());
		float dif=maxXY.y-minXY.y; //setting the maximum shown y-range to be a minimum range of 15
		if (dif<15) {
			dif=15-dif;
		} else dif=5;
		ABS_Y_MIN=minXY.y-dif/2;
		ABS_Y_MAX=maxXY.y+dif/2;
		MAX_Y_DISTANCE=ABS_Y_MAX - ABS_Y_MIN;
		temperatureSimpleXYPlot.setRangeBoundaries(minXY.y,maxXY.y, BoundaryMode.FIXED);

		ABS_X_MIN=minXY.x;
		ABS_X_MAX=maxXY.x;
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


		//prepare: draw 24h lines (every midnight)
		Long[] midnights = lookForMidnight(selected.get(0).getDate(), selected
				.get(0).getTimeOffset().getTime(), selected.size());
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

			temperatureSimpleXYPlot
					.addSeries(midnightSeriesTemp, series1Format);
			moistureSimpleXYPlot.addSeries(midnightSeriesMoist, series1Format);
		}
		
		moistureSimpleXYPlot.redraw();
		temperatureSimpleXYPlot.redraw();

		temperatureSimpleXYPlot.setOnTouchListener(tempTouch);
		moistureSimpleXYPlot.setOnTouchListener(moistTouch);
	}

	private Long[] lookForMidnight(Date date, long offset, int size) {
		long timeMin = date.getTime();
		long timeMax = timeMin + offset * size * 24; // TODO dont hardcode nr	of measures
		
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

}
