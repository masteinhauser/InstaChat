package com.kastlersteinhauser.instachat;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class ChatList extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);

		try{
			new ZMQTest().execute();
		} catch(Exception e){
			Log.e(this.getClass().getSimpleName(), e.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_chat_list, menu);
		return true;
	}
}
