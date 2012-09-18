package com.kastlersteinhauser.instachat;

import android.os.AsyncTask;
import android.util.Log;
import org.jeromq.ZMQ;

public class ZMQTest extends AsyncTask<Void, Void, Void> {
	private ZMQ.Context context;
	private ZMQ.Socket sub;

	@Override
	protected Void doInBackground(Void... params) {
		// Prepare our context and subscriber
		context = ZMQ.context(1);
		sub = context.socket(ZMQ.SUB);

		sub.connect("tcp://10.0.2.2:12345");
		sub.subscribe("AAPL");

		while (true) {
			// Read envelope with address
			String address = new String(sub.recv(0));
			// Read message contents
			String contents = new String(sub.recv(0));
			Log.d(this.getClass().getSimpleName(), address + " : " + contents);
		}
	}
}
