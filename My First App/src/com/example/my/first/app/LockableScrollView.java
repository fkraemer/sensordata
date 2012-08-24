package com.example.my.first.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;


//extends ScrollView so one can disable the scroll possibility for some time
public class LockableScrollView extends ScrollView {

	public LockableScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public LockableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public LockableScrollView(Context context) {
		super(context);
	}

	
	 private boolean scroll_enabled=true;
	 

	public void setScrollEnable(boolean enable) {
		scroll_enabled=enable;
	}
	
	public boolean isScrollable()
	{
		return scroll_enabled;
	}

	//overrides and returns false, for case: scrolling disabled
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		 
		if (!scroll_enabled) return false;
		else return (ev.getPointerCount()>1) ? super.onInterceptTouchEvent(ev) : false;		//make sure super can call getter on event
	}

	//overrides, returns false on Action_Down, thus doesnt scroll, else just returns super.methods
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!scroll_enabled) return false;
			else return super.onTouchEvent(ev);
		default:
			return super.onTouchEvent(ev);
		}
		
	}
	
	
	
	
}
