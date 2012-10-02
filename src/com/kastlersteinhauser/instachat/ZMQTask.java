package com.kastlersteinhauser.instachat;

import android.os.AsyncTask;
import android.util.Log;
import org.jeromq.ZMQ;

public class ZMQTask extends AsyncTask<ZMQService, Void, Void> {
	private ZMQ.Context context;
	private ZMQ.Socket sub;
	private ZMQService service;

	protected Void doInBackground(ZMQService... params) {
		service = params[0];
		
		// Prepare our context and subscriber
		context = ZMQ.context(1);
		sub = context.socket(ZMQ.SUB);

		sub.connect("tcp://10.0.2.2:12345");
		sub.subscribe("AAPL");

		while (!isCancelled()) {
			// Read message contents
			String contents = new String(sub.recv(0)).split(" ")[1];
			
			// Do something with the message
			//Log.d(this.getClass().getSimpleName(), contents);
			service.handleMessage(contents);
		}
		
		return null;
	}
}
