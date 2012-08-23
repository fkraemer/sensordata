package com.example.my.first.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;


//extends ScrollView so one can disable the scroll possibility for some time
public class LockableScrollView extends ScrollView {

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
		else return super.onInterceptTouchEvent(ev);
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
