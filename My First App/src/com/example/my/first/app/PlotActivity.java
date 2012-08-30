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
import com.androidplot.xy.*;

import android.util.FloatMath;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.example.sensor.data.DataSet;
import com.example.sensor.data.DataStorage;

public class PlotActivity extends Activity implements OnTouchListener {

	final private float MIN_X_DISTANCE=2.0f;		//minimum of shown time range
	final private float MIN_Y_DISTANCE=3.0f;	//minimum of shown temperature Range
	
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
	private float ABS_Y_MIN;
	private float ABS_Y_MAX;
	private float MAX_Y_DISTANCE;
	private float ABS_X_MIN;
	private float ABS_X_MAX;
	private float MAX_X_DISTANCE;
	

	// very simple, Y-values only, filling the temperature plot
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras =getIntent().getExtras();
		storage = extras.getParcelable("storage");
		ArrayList<Integer> selected=extras.getIntegerArrayList("selected");
		
		setContentView(R.layout.plot);
		scroll = (LockableScrollView) findViewById(R.id.scroll);
		txt1 = (TextView)findViewById(R.id.txtview1);
		txt2 = (TextView)findViewById(R.id.txtview2);
		txt3 = (TextView)findViewById(R.id.txtview3);
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
		temperatureSimpleXYPlot.setRangeLabel("");
		temperatureSimpleXYPlot.setDomainLabel("");
		temperatureSimpleXYPlot.disableAllMarkup();
		moistureSimpleXYPlot.disableAllMarkup();

		
//		for (Integer k:selected) {}
		Integer k=0;
		DataSet temp=storage.getDatabyLocalId(k);
		
		for (int j = 0; j < 4; j++) {	
			Integer[] column = temp.getTempData(j);
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
	/**	//filling the moisture plot
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
        <TextView 
            android:id="@+id/txtview"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
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
		float dif=maxXY.y-minXY.y; //setting the maximum shown y-range to be a minimum range of 15
		if (dif<15) {
			dif=15-dif;
		} else dif=5;
		ABS_Y_MIN=minXY.y-dif/2;
		ABS_Y_MAX=maxXY.y+dif/2;
		MAX_Y_DISTANCE=ABS_Y_MAX - ABS_Y_MIN;
		minXY.y+=10;		//TODO debug
		maxXY.y-=10;
		temperatureSimpleXYPlot.setRangeBoundaries(minXY.y,maxXY.y, BoundaryMode.FIXED);
		
		ABS_X_MIN=minXY.x;
		ABS_X_MAX=maxXY.x;
		MAX_X_DISTANCE=((ABS_X_MAX-ABS_X_MIN)<48.0f) ? (ABS_X_MAX-ABS_X_MIN) : 48.0f;
		minXY.x+=5;		//TODO debug
		maxXY.x-=5;
		temperatureSimpleXYPlot.setDomainBoundaries(minXY.x,maxXY.x, BoundaryMode.FIXED);	//start with most recent (max 48)hours
		temperatureSimpleXYPlot.redraw();

		
		//TODO, put Plot in subclass, begrenzungsmethoden, getter/setter, for touch stuff
	}

	static final int NONE = 0;
	static final int ONE_FINGER_DRAG = 1;
	static final int TWO_FINGERS_DRAG = 2;
	int mode = NONE;
 
	PointF firstFinger;
	PointF midPoint;
	float lastScrollingX;
	float lastScrollingY;
	float distBetweenFingers;
	float lastZooming;
	 
	

	public boolean onTouch(View arg0, MotionEvent event) {
		txt1.setText("x:  "+String.valueOf(event.getX(0))+"  y: "+String.valueOf(event.getY(0)));
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: // Start gesture
			firstFinger = new PointF(event.getX(), event.getY());
			mode = ONE_FINGER_DRAG;
			break;
		case MotionEvent.ACTION_UP: 
			scroll.setScrollEnable(true);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				
				public void run() {
					while(Math.abs(lastScrollingX)>1f || Math.abs(lastScrollingY)>1f || Math.abs(lastZooming-1)<1.01){ 
					lastScrollingX*=.9;
					lastScrollingY*=.9;
					scroll(lastScrollingX,lastScrollingY);
					lastZooming+=(1-lastZooming)*.2;
					zoom(lastZooming);
					setBoundaries();
					try {
						temperatureSimpleXYPlot.postRedraw();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					scroll.setScrollEnable(true);						
					}
					// the thread lives until the scrolling and zooming are imperceptible
				}
				
			}, 0);	

			distBetweenFingers=spacing(event);
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN: // second finger
			distBetweenFingers = spacing(event);
			// the distance check is done to avoid false alarms
			if (distBetweenFingers > 5f) {
				mode = TWO_FINGERS_DRAG;
			}
			break;
			
					case MotionEvent.ACTION_MOVE:
			scroll.setScrollEnable(false);
			if (mode == ONE_FINGER_DRAG) {
				PointF oldFirstFinger=firstFinger;
				firstFinger=new PointF(event.getX(), event.getY());
				lastScrollingX=oldFirstFinger.x-firstFinger.x;		//x-difference
				lastScrollingY=oldFirstFinger.y-firstFinger.y;		//y-difference
				scroll(lastScrollingX,lastScrollingY);
				lastZooming=(firstFinger.y-oldFirstFinger.y)/temperatureSimpleXYPlot.getHeight();
			/**if (lastZooming<0)
					lastZooming=1/(1-lastZooming);
				else
					lastZooming+=1;
				zoom(lastZooming);*/
				setBoundaries();
				temperatureSimpleXYPlot.redraw();
 
			} else if (mode == TWO_FINGERS_DRAG) {
				float oldDist =distBetweenFingers; 

				distBetweenFingers=spacing(event);
				lastZooming=oldDist/distBetweenFingers;
				zoom(lastZooming);

				setBoundaries();
				temperatureSimpleXYPlot.redraw();
			}
			break;
		} 
		setBoundaries();
		return true;
	}
 
	private void zoom(float scale) {
		// set x-axis borders, limit to maximum borders
		float domainSpan = maxXY.x	- minXY.x;
		float domainMidPoint= maxXY.x - domainSpan/ 2.0f;	//setMid() ,must have been called recently:
		//float factorX =minXY.x + midPoint.x / temperatureSimpleXYPlot.getWidth() * domainSpan;
		//txt1.setText(String.valueOf(factorX) +"    "+ String.valueOf(temperatureSimpleXYPlot.getWidth()));
		
		float domainOffset = domainSpan * scale / 2.0f;		
		float[] k=forceBorders(minXY.x,maxXY.x,domainMidPoint- domainOffset,domainMidPoint+ domainOffset,Direction.X);
		minXY.x=k[0];
		maxXY.x=k[1];
		//set y-axis borders, keep in maximumborders
		float rangeSpan = maxXY.y - minXY.y;
		float rangeOffset = rangeSpan * scale / 2.0f;
		float rangeMidPoint= maxXY.y - rangeSpan/ 2.0f;
		k=forceBorders(minXY.y,maxXY.y,rangeMidPoint- rangeOffset,rangeMidPoint+ rangeOffset,Direction.Y);
		minXY.y=k[0];
	    maxXY.y=k[1];
	}

	//push window
	private void scroll(float panX, float panY) {
		float domainSpan = maxXY.x	- minXY.x;
		float step = domainSpan / temperatureSimpleXYPlot.getWidth();
		float offset = panX * step;
		
		float[] k=forceBorders(minXY.x, maxXY.x, minXY.x+offset, maxXY.x+offset, Direction.X);
		minXY.x=k[0];
	    maxXY.x=k[1];
	    
	    float rangeSpan = minXY.y - maxXY.y;
		step = rangeSpan / temperatureSimpleXYPlot.getHeight();
		offset = panY * step;
		
		k=forceBorders(minXY.y, maxXY.y, minXY.y+offset, maxXY.y+offset, Direction.Y);
		minXY.y=k[0];
	    maxXY.y=k[1];
		
	}
	
	//distance between fingers
	private float spacing(MotionEvent event) 
	{
		if (event.getPointerCount() > 1) 		//make sure there are 2touches, otherwise exceptions
		{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
		}
		return 0;
	}
	
/**	private void setMid(MotionEvent ev)			// planned to zoom centered around the touch point
	{
		if (ev.getPointerCount() > 1) 		//case: 2touches
		{
			midPoint = new PointF(0.5f * ev.getX(0) + 0.5f * ev.getX(1),
					0.5f * ev.getY(0) + 0.5f * ev.getY(1));
		} else
			midPoint = new PointF(ev.getX(0),ev.getY(0));
		
	}*/
	
	private void setBoundaries() 
	{
		temperatureSimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
		temperatureSimpleXYPlot.setRangeBoundaries(minXY.y, maxXY.y, BoundaryMode.FIXED);
		//txt3.setText("X: "+String.valueOf(minXY.x) + "  - "+String.valueOf(maxXY.x)+
		//		"Y: "+String.valueOf(minXY.y) + "  - "+String.valueOf(maxXY.y));
	}
	
	private  float[] forceBorders(float oldMin, float oldMax, float newMin, float newMax, Direction dir) {
		float lowerBorder=newMin;
		float upperBorder=newMax;
		float minDistance;
		float maxDistance;
		float minValue;
		float maxValue;
		if (dir==Direction.X) {
			minDistance=MIN_X_DISTANCE;
			maxDistance=MAX_X_DISTANCE;
			minValue=ABS_X_MIN;
			maxValue=ABS_X_MAX;
		} else {
			minDistance=MIN_Y_DISTANCE;
			maxDistance=MAX_Y_DISTANCE;
			minValue=ABS_Y_MIN;
			maxValue=ABS_Y_MAX;
		}
		StringBuilder sb= new StringBuilder();
		//case: lower border under minimum, covers cases zoom out, scroll out
		if (newMin<minValue) {
			lowerBorder=minValue;
			upperBorder=minValue+(newMax-newMin);	//realize distance-change, thereby applicable for scroll and zoom
			sb.append("  force: unterschritten");
		}
		
		//case: upper border over maximum, 
		if (newMax>maxValue) {
			upperBorder=maxValue;
			lowerBorder=maxValue-(newMax-newMin);
			sb.append("  force: ueberschritten");
		}
		//case: range to small/ and case: range to big
		if (Math.abs(newMax-newMin) < minDistance || Math.abs(newMax-newMin) > maxDistance) {
			lowerBorder=oldMin;
			upperBorder=oldMax;
			sb.append("  range problem");
		}
		//txt2.setText(sb.toString());
		return new float[]{lowerBorder,upperBorder};
	}
	
	  
}
