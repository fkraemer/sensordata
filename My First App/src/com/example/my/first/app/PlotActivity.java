package com.example.my.first.app;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.androidplot.series.XYSeries;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.*;

import android.util.FloatMath;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.example.sensor.data.DataSet;
import com.example.sensor.data.DataStorage;

public class PlotActivity extends Activity{

	final private float MIN_X_DISTANCE=2.0f*60*60*1000;		//minimum of shown time range (in millis)
	final private float TEMP_MIN_Y_DISTANCE=3.0f;	//minimum of shown temperature Range
	final private float MOIST_MIN_Y_DISTANCE=3.0f;	//minimum of shown temperature Range
	
	private XYPlot temperatureSimpleXYPlot;
	private XYPlot moistureSimpleXYPlot;
	private DataStorage storage;
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
		storage = extras.getParcelable("storage");
		ArrayList<Integer> selected=extras.getIntegerArrayList("selected");
		Collections.sort(selected);
		
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
            private SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
            
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
				new DecimalFormat("#####.##"));
		temperatureSimpleXYPlot.getGraphWidget().setDomainValueFormat(timeFormat);
		temperatureSimpleXYPlot.setRangeLabel("Temperature");
		temperatureSimpleXYPlot.setDomainLabel("Time");
		temperatureSimpleXYPlot.disableAllMarkup();
		//TODO also for moisture plot, set to just as many grid lines, as hours displayed ??
		//make grid flexible, move with zoom/scroll
		temperatureSimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, selected.size()* 12);
			
		//		storage.getDatabyLocalId(selected.get(0)).getTimeOffset().toMillis(true) / 3600000);
		
		moistureSimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(1);
		moistureSimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(1);
		moistureSimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("##.#"));
		//moistureSimpleXYPlot.getGraphWidget().setDomainValueFormat(				new DecimalFormat("#####.#"));
		moistureSimpleXYPlot.getGraphWidget().setDomainValueFormat(timeFormat);
		moistureSimpleXYPlot.setRangeLabel("Moisture");
		moistureSimpleXYPlot.setDomainLabel("Time");
		moistureSimpleXYPlot.disableAllMarkup();



		
		
		
//		for (Integer k:selected) {}
		Integer k=selected.get(0);
		DataSet data=storage.getDatabyLocalId(k);
		
		//prepare: draw 24h lines
		
		
		for (int j = 0; j < 4; j++) {	
			Float[] tempColumn = data.getTempData(j);
			Float[] moistColumn = data.getMoistData(j);
			Long[] timeColumn = new Long[tempColumn.length];
			long timeStamp=data.getDate().getTime();
			long timeOffset=data.getTimeOffset().toMillis(true);
			for (int i=0;i<tempColumn.length;i++){
				timeColumn[i]=timeStamp+timeOffset*i;
			}

			XYSeries tempSeries = new SimpleXYSeries(Arrays.asList(timeColumn),Arrays.asList(tempColumn),
					 "Temperature Series");
			XYSeries moistSeries = new SimpleXYSeries( Arrays.asList(timeColumn), Arrays.asList(moistColumn),
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
		temperatureSimpleXYPlot.redraw();
		
		plotTouch tempTouch= new plotTouch(temperatureSimpleXYPlot,ABS_X_MIN,ABS_X_MAX,ABS_Y_MIN,ABS_Y_MAX,
				MAX_X_DISTANCE,MIN_X_DISTANCE,TEMP_MIN_Y_DISTANCE,MAX_Y_DISTANCE,scroll);
		
		moistureSimpleXYPlot.calculateMinMaxVals();
		minXY=new PointF(moistureSimpleXYPlot.getCalculatedMinX().floatValue(),
				moistureSimpleXYPlot.getCalculatedMinY().floatValue());
		maxXY=new PointF(moistureSimpleXYPlot.getCalculatedMaxX().floatValue(),
				moistureSimpleXYPlot.getCalculatedMaxY().floatValue());
		dif=maxXY.y-minXY.y; //setting the maximum shown y-range to be a minimum range of 15
		if (dif<150) {		//TODO fit values in
			dif=150-dif;
		} else dif=50;
		ABS_Y_MIN=(minXY.y-dif/2 < 0) ? 0 : minXY.y-dif/2; //dont allow negative values, never measured
		ABS_Y_MAX=(minXY.y-dif/2 < 0) ? dif : maxXY.y+dif/2; //upper bound keeps dif distance to lower 
		MAX_Y_DISTANCE=ABS_Y_MAX - ABS_Y_MIN;
		moistureSimpleXYPlot.setRangeBoundaries(minXY.y,maxXY.y, BoundaryMode.FIXED);
		
		//max_x_values can be reused from temperature
		moistureSimpleXYPlot.redraw();
		

		plotTouch moistTouch= new plotTouch(moistureSimpleXYPlot,ABS_X_MIN,ABS_X_MAX,ABS_Y_MIN,ABS_Y_MAX,
				MAX_X_DISTANCE,MIN_X_DISTANCE,TEMP_MIN_Y_DISTANCE,MAX_Y_DISTANCE,scroll);
		
		temperatureSimpleXYPlot.setOnTouchListener(tempTouch);
		moistureSimpleXYPlot.setOnTouchListener(moistTouch);
	}


	 
	

	
}
