package com.example.my.first.app;

import java.util.concurrent.CountDownLatch;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

	
public class MyConnection implements ServiceConnection {

	private boolean bound;
	CountDownLatch latch;
	DataService service;
	
	public MyConnection(CountDownLatch l) {
		latch=l;
	}
	
	public DataService getService() {
		return service;
	}

	public boolean isBound() {
		return bound;
	}

	public void onServiceConnected(ComponentName arg0, IBinder localBinder) {
		bound=true;
		DataService.LocalBinder bind = (DataService.LocalBinder) localBinder;
		service= bind.getService();
		latch.countDown();
	}

	public void onServiceDisconnected(ComponentName arg0) {
		bound=false;
	}

	public void setBound(boolean bound) {
		this.bound = bound;
	}

	
}
