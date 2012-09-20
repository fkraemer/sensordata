package com.example.my.first.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;


public class CustomCursorAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private boolean [] isBound;
    public CustomCursorAdapter(Context context, Cursor c, boolean[] isBound) {
        super(context, c);
        mContext = context;
        this.isBound=isBound;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.platform_entry, parent, false);
    }

    public void bindView(View v, Context context, Cursor c) {
    	String mobileNo = c.getString(c.getColumnIndex(DatabaseControl.KEY_MOBILENO));
    	long platformId = c.getLong(c.getColumnIndex(DatabaseControl.KEY_ID));
    	String description = c.getString(c.getColumnIndex(DatabaseControl.KEY_DESCR));
        // Next set the fields of the entry.
        ((TextView) v.findViewById(R.id.mobile_number)).setText(mobileNo);
        ((TextView) v.findViewById(R.id.id)).setText(Long.toString(platformId));
        ((TextView) v.findViewById(R.id.description)).setText(description);
        if (isBound.length>=c.getPosition() && isBound[c.getPosition()]) {	//check array size, happens rarely that the adapter updates, when isBound is not ready

        	((CheckBox) v.findViewById(R.id.isBoundBox)).setVisibility(View.VISIBLE);
        	((CheckBox) v.findViewById(R.id.isBoundBox)).setChecked(true);
        	//must be set, so list stays clickable:
        	((CheckBox) v.findViewById(R.id.isBoundBox)).setClickable(false);
        	((CheckBox) v.findViewById(R.id.isBoundBox)).setFocusable(false);
        } else {
        	((CheckBox) v.findViewById(R.id.isBoundBox)).setVisibility(View.INVISIBLE);
        }
    }
}
