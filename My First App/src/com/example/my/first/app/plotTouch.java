package com.example.my.first.app;

import java.util.Timer;
import java.util.TimerTask;

import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;

import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class plotTouch implements OnTouchListener {
	

	private float ABS_Y_MIN;
	private float ABS_Y_MAX;
	private float MAX_Y_DISTANCE;
	private float ABS_X_MIN;
	private float ABS_X_MAX;
	private float MAX_X_DISTANCE;	
	private float MIN_X_DISTANCE;		//minimum of shown time range
	private float MIN_Y_DISTANCE;


	private static final int NONE = 0;
	private static final int ONE_FINGER_DRAG = 1;
	private static final int TWO_FINGERS_DRAG = 2;		//zooms/scrolls a bit longer 
	protected static final float SCROLL_PARAM = 0.6f;	//default 0.6,(1>x>0.1) higher, the longer
	protected static final float ZOOM_PARAM = 0.3f;		//default 0.3(0.1<x<1) smaller, the longer
	private int mode = NONE;
 
	private PointF firstFinger;
	private PointF midPoint;
	private PointF minXY=new PointF();
	private PointF maxXY=new PointF();
	private float lastScrollingX;
	private float lastScrollingY;
	private float distBetweenFingers;
	private float lastZooming;
	private LockableScrollView scroll;
	private XYPlot plot;
	


	public plotTouch(XYPlot plot,float ABS_X_MIN, float ABS_X_MAX, float ABS_Y_MIN,
			float ABS_Y_MAX, float MAX_X_DISTANCE, float MIN_X_DISTANCE,
			float MIN_Y_DISTANCE, float MAX_Y_DISTANCE,LockableScrollView scroll) {
		this.plot=plot;
		this.ABS_X_MAX=ABS_X_MAX;
		this.ABS_X_MIN=ABS_X_MIN;
		this.ABS_Y_MAX=ABS_Y_MAX;
		this.ABS_Y_MIN=ABS_Y_MIN;
		this.MAX_X_DISTANCE=MAX_X_DISTANCE;
		this.MIN_X_DISTANCE=MIN_X_DISTANCE;
		this.MAX_Y_DISTANCE=MAX_Y_DISTANCE;
		this.MIN_Y_DISTANCE=MIN_Y_DISTANCE;
		this.scroll=scroll;
		
		//startup-view:

		minXY.x=ABS_X_MAX-MAX_X_DISTANCE;	//set the upper MAX_X_DISTANCE values to be displayed 
		maxXY.x=ABS_X_MAX;
		minXY.y=ABS_Y_MIN;
		maxXY.y=ABS_Y_MAX;
		
		setBoundaries();
	}

	
	public boolean onTouch(View arg0, MotionEvent event) {
		//double valY = ValPixConverter.pixToVal(event.getY(0), minXY.y, maxXY.y, plot.getHeight(), true);
		//double valX = ValPixConverter.pixToVal(event.getX(0), minXY.x, maxXY.x, plot.getWidth(), false);
		//if (!(valX<maxXY.x && valX>minXY.x && valY<maxXY.y && valY>minXY.y)) return false;
				
		
		
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: // Start gesture
			firstFinger = new PointF(event.getX(), event.getY());
			mode = ONE_FINGER_DRAG;
			break;
		case MotionEvent.ACTION_UP: 
			scroll.setScrollEnable(true);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			scroll.setScrollEnable(true);	
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				
				public void run() {
					//lastzooming: values between 0 and 2
					while(Math.abs(lastScrollingX)>1f || Math.abs(lastScrollingY)>1f || Math.abs(lastZooming-1)<1.01){ 
					lastScrollingX*=SCROLL_PARAM;
					lastScrollingY*=SCROLL_PARAM;
					scroll(lastScrollingX,lastScrollingY);
					lastZooming+=(1-lastZooming)*ZOOM_PARAM;
					zoom(lastZooming);
					try {
						plot.postRedraw();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
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
				lastZooming=1;
				PointF oldFirstFinger=firstFinger;
				firstFinger=new PointF(event.getX(), event.getY());
				lastScrollingX=oldFirstFinger.x-firstFinger.x;		//x-difference
				lastScrollingY=oldFirstFinger.y-firstFinger.y;		//y-difference
				scroll(lastScrollingX,lastScrollingY);
 
			} else if (mode == TWO_FINGERS_DRAG) {
				lastScrollingX=0;
				lastScrollingY=0;
				float oldDist =distBetweenFingers; 

				distBetweenFingers=spacing(event);
				if (distBetweenFingers!=0f) {
					lastZooming=oldDist/distBetweenFingers;
				zoom(lastZooming);
				}
			}
			plot.redraw();
			break;
		} 
		return true;
	}
 
	private void zoom(float scale) {
		// set x-axis borders, limit to maximum borders
		float domainSpan = maxXY.x	- minXY.x;
		float domainMidPoint= maxXY.x - domainSpan/ 2.0f;	//setMid() ,must have been called recently:
		//float factorX =minXY.x + midPoint.x / plot.getWidth() * domainSpan;
		//txt1.setText(String.valueOf(factorX) +"    "+ String.valueOf(plot.getWidth()));
		
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
		setBoundaries();
	}

	//push window
	private void scroll(float panX, float panY) {
		float domainSpan = maxXY.x	- minXY.x;
		float step = domainSpan / plot.getWidth();
		float offset = panX * step;
		
		float[] k=forceBorders(minXY.x, maxXY.x, minXY.x+offset, maxXY.x+offset, Direction.X);
		
		plot.setUserDomainOrigin((plot.getDomainOrigin()).longValue() + k[0] - minXY.x);
		minXY.x=k[0];
	    maxXY.x=k[1];
	    
	    float rangeSpan = minXY.y - maxXY.y;
		step = rangeSpan / plot.getHeight();
		offset = panY * step;
		
		k=forceBorders(minXY.y, maxXY.y, minXY.y+offset, maxXY.y+offset, Direction.Y);
		minXY.y=k[0];
	    maxXY.y=k[1];
		plot.setUserRangeOrigin(( plot.getRangeOrigin().longValue()) + ((Float)(k[0]-minXY.y)).longValue() );

		setBoundaries();
		
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
		plot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
		plot.setRangeBoundaries(minXY.y, maxXY.y, BoundaryMode.FIXED);
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
		return new float[]{lowerBorder,upperBorder};
	}
	
	  

}
