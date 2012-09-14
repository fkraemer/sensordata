package com.example.my.first.app;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

	
public class MyConnection implements ServiceConnection {

	private boolean bound;
	DataService service;
	
	public void onServiceConnected(ComponentName arg0, IBinder localBinder) {
		bound=true;
		DataService.LocalBinder bind = (DataService.LocalBinder) localBinder;
		service= bind.getService();
	}

	public void onServiceDisconnected(ComponentName arg0) {
		bound=false;
	}

	public boolean isBound() {
		return bound;
	}

	public DataService getService() {
		return service;
	}

	public void setBound(boolean bound) {
		this.bound = bound;
	}

	
}
