package com.example.my.first.app;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.*;

import android.util.FloatMath;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.example.sensor.data.DataStorage;

public class PlotActivity extends Activity implements OnTouchListener {

	private XYPlot temperatureSimpleXYPlot;
	private XYPlot moistureSimpleXYPlot;
	private TextView txt;
	private LockableScrollView scroll;
	private Integer[] column;
	private PointF minXY;
	private PointF maxXY;
	private DataStorage data;

	// very simple, Y-values only, filling the temperature plot
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.plot);
		data = new DataStorage();
		scroll = (LockableScrollView) findViewById(R.id.scroll);
		txt = (TextView)findViewById(R.id.txtview);
		temperatureSimpleXYPlot = (XYPlot) findViewById(R.id.temperatureXYPlot);
		moistureSimpleXYPlot = (XYPlot) findViewById(R.id.moistureXYPlot);
		
		temperatureSimpleXYPlot.setOnTouchListener(this);
		
		//set plot properties
		
		temperatureSimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(1);
		temperatureSimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(1);
		temperatureSimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("#####.##"));
		temperatureSimpleXYPlot.getGraphWidget().setDomainValueFormat(
				new DecimalFormat("#####.##"));
		temperatureSimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
		temperatureSimpleXYPlot.setRangeLabel("");
		temperatureSimpleXYPlot.setDomainLabel("");
		temperatureSimpleXYPlot.disableAllMarkup();
		moistureSimpleXYPlot.disableAllMarkup();
		
		
		

		for (int j = 0; j < 4; j++) {	
			Integer[] column = data.getData().getTempData(j);
			//Integer[] column= {1,7,23,5,9,12,3};			//for debug
			
			XYSeries series1 = new SimpleXYSeries(Arrays.asList(column),
					SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series 1");

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

			temperatureSimpleXYPlot.addSeries(series1, series1Format);

		}
	/*	//filling the moisture plot
		for (int j = 0; j < 4; j++) {	
			Integer[] column = data.getData().getMoistData(j);			
			XYSeries series1 = new SimpleXYSeries(Arrays.asList(column),
					SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series 1");

			int colour = 0;
			switch (j) {
			case 0:
				colour = Color.WHITE;
				break;
			case 1:
				colour = Color.GRAY;
				break;
			case 2:
				colour = Color.BLACK;
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

			moistureSimpleXYPlot.addSeries(series1, series1Format);

		}  */
		
		
		temperatureSimpleXYPlot.calculateMinMaxVals();
		minXY=new PointF(temperatureSimpleXYPlot.getCalculatedMinX().floatValue(),
				temperatureSimpleXYPlot.getCalculatedMinY().floatValue());
		maxXY=new PointF(temperatureSimpleXYPlot.getCalculatedMaxX().floatValue(),
				temperatureSimpleXYPlot.getCalculatedMaxY().floatValue());
		float dif=maxXY.y-minXY.y; //setting min heigth of 15, otherwise increase boundaries.
		if (dif<15) {
			dif=15-dif;
		} else dif=5;
		final float ABS_Y_MIN=minXY.y-dif/2;
		final float ABS_Y_MAX=maxXY.y+dif/2;
		temperatureSimpleXYPlot.setRangeBoundaries(ABS_Y_MIN,ABS_Y_MAX, BoundaryMode.FIXED);
		temperatureSimpleXYPlot.redraw();
		
		//TODO, put Plot in subclass, begrenzungsmethoden, getter/setter, for touch stuff
	}

	static final int NONE = 0;
	static final int ONE_FINGER_DRAG = 1;
	static final int TWO_FINGERS_DRAG = 2;
	int mode = NONE;
 
	PointF firstFinger;
	float lastScrolling;
	float distBetweenFingers;
	float lastZooming;
	 
	

	public boolean onTouch(View arg0, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: // Start gesture
			firstFinger = new PointF(event.getX(), event.getY());
			mode = ONE_FINGER_DRAG;
			break;
		case MotionEvent.ACTION_UP: 
			scroll.setScrollEnable(true);
			txt.setText("");
			break;
		case MotionEvent.ACTION_POINTER_UP:
			scroll.setScrollEnable(true);
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				
				public void run() {
					while(Math.abs(lastScrolling)>1f || Math.abs(lastZooming-1)<1.01){ 
					lastScrolling*=.8;
					scroll(lastScrolling);
					lastZooming+=(1-lastZooming)*.2;
					zoom(lastZooming);
					temperatureSimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
					try {
						temperatureSimpleXYPlot.postRedraw();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// the thread lives until the scrolling and zooming are imperceptible
				}
				}
			}, 0);
			
			
		case MotionEvent.ACTION_POINTER_DOWN: // second finger
			distBetweenFingers = spacing(event);
			// the distance check is done to avoid false alarms
			if (distBetweenFingers > 5f) {
				mode = TWO_FINGERS_DRAG;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == ONE_FINGER_DRAG) {
				PointF oldFirstFinger=firstFinger;
				firstFinger=new PointF(event.getX(), event.getY());
				lastScrolling=oldFirstFinger.x-firstFinger.x;		//x-difference
				scroll(lastScrolling);
				lastZooming=(firstFinger.y-oldFirstFinger.y)/temperatureSimpleXYPlot.getHeight();
				if (lastZooming<0)
					lastZooming=1/(1-lastZooming);
				else
					lastZooming+=1;
				zoom(lastZooming);
				temperatureSimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
				txt.setText("Move:  "+String.valueOf(minXY.x)+"   "+String.valueOf(maxXY.x));
				temperatureSimpleXYPlot.redraw();
 
			} else if (mode == TWO_FINGERS_DRAG) {
				scroll.setScrollEnable(false);
				float oldDist =distBetweenFingers; 
				distBetweenFingers=spacing(event);
				lastZooming=oldDist/distBetweenFingers;
				zoom(lastZooming);
				txt.setText(String.valueOf(distBetweenFingers));
				temperatureSimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
				txt.setText("Zoom:  "+String.valueOf(minXY.x)+"   "+String.valueOf(maxXY.x));
				temperatureSimpleXYPlot.redraw();
			}
			break;
		}
		return true;
	}
 
	private void zoom(float scale) {
		float domainSpan = maxXY.x	- minXY.x;
		float domainMidPoint = maxXY.x		- domainSpan / 2.0f;
		float offset = domainSpan * scale / 2.0f;
		minXY.x=domainMidPoint- offset;
		maxXY.x=domainMidPoint+offset;
	}
 
	//push window
	private void scroll(float pan) {
		float domainSpan = maxXY.x	- minXY.x;
		float step = domainSpan / temperatureSimpleXYPlot.getWidth();
		float offset = pan * step;
		minXY.x+= offset;
		maxXY.x+= offset;
	}
 
	private float spacing(MotionEvent event) 
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
		
		

	
	  
}
