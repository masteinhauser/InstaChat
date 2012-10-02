package com.kastlersteinhauser.instachat;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ZMQService extends Service {
	public static String TAG = "ZMQService";
	
	private ZMQTask task;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		doWork();
	}
	
	@Override
	public void onDestroy() {
		task.cancel(true);
		super.onDestroy();
		Toast.makeText(this, "Service destroyed...", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		doWork();
		return START_STICKY;
	}
	 
	private void doWork(){
		super.onCreate();
			
		try{
			task = new ZMQTask();
			task.execute(this);
		} catch(Exception e){
			Log.e(this.getClass().getSimpleName(), e.toString());
		}
			
		Toast.makeText(this, "Service created...", Toast.LENGTH_LONG).show();
	}
	
	public void handleMessage(String json){
		JSONObject jsonMessage = null;
		String user = "";
		String message = "";
		
		try {
			jsonMessage = (JSONObject) new JSONTokener(json).nextValue();
		} catch (JSONException e) {
			Log.d(TAG, "Unable to parse message JSON", e);
		}
		
		if(jsonMessage == null){
			Log.e(TAG, "msg was not properly parsed");
			return; // return early to bail out of processing
		}
		
		try {
			user = jsonMessage.getString("user");
		} catch (JSONException e) {
			Log.d(TAG, "Unable to parse user from msg", e);
		}
		
		try {
			message = jsonMessage.getString("message");
		} catch (JSONException e) {
			Log.d(TAG, "Unable to parse user from msg", e);
		}
		
		Intent i = new Intent("com.kastlersteinhauser.instachat.MESSAGE_RECV");
		Message msg = new Message();
		msg.setUser(user);
		msg.setMessage(message);
		
		i.putExtra("message", msg);
		sendBroadcast(i);
	}
}
