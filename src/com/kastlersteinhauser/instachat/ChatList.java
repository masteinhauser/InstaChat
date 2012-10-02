package com.kastlersteinhauser.instachat;

import java.util.ArrayList;

import edu.cvtc.android.lesson4.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class ChatList extends Activity {
	private ArrayList<Message> messages;
	
	private Intent zmqService;
	private ReceiveMessages recv = null;
	private boolean recvIsRegistered = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);
		
		zmqService = new Intent(this, ZMQService.class);
		startService(zmqService);
		
		recv = new ReceiveMessages(this);
		
		messages = new ArrayList<Message>();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_chat_list, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(!recvIsRegistered){
			registerReceiver(recv, new IntentFilter("com.kastlersteinhauser.instachat.MESSAGE_RECV"));
			recvIsRegistered = true;
		}
	}
	
	@Override
	public void onPause() {
		if(recvIsRegistered){
			unregisterReceiver(recv);
			recvIsRegistered = false;
		}
		
		stopService(zmqService);
		super.onPause();
	}
	
	public class ReceiveMessages extends BroadcastReceiver {
		private Context context;
		
		public ReceiveMessages(Context context){
			this.context = context;
		}
		
		@Override
        public void onReceive(Context context, Intent intent) {
			Message message = (Message) intent.getParcelableExtra("message");
			
			Toast.makeText(this.context, message.getUser() + ": " + message.getMessage(), Toast.LENGTH_SHORT).show();
			messages.add(message);
		}
	}
	
	/**
	 * Method used to encapsulate the code that initializes and sets the Layout
	 * for this Activity.
	 */
	protected void initLayout() {
		// Activity inherits from the Context class.
		// LinearLayout constructor needs a Context, so we can pass it 'this'
		// LinearLayout used for Add Joke button and EditText
		LinearLayout localLinear = new LinearLayout(this);

		// LinearLayout defaults to Horizontal, we need Vertical
		localLinear.setOrientation(LinearLayout.VERTICAL);

		// LinearLayout used for the JokeList
		m_vwJokeLayout = new LinearLayout(this);

		// LinearLayout defaults to Horizontal, we need Vertical
		m_vwJokeLayout.setOrientation(LinearLayout.VERTICAL);

		// Used to contain and layout the Add Joke button and text box
		LinearLayout addJokeLayout = new LinearLayout(this);

		// Add Joke button
		m_vwJokeButton = new Button(this);

		// Set button text
		m_vwJokeButton.setText(R.string.button_add_joke);

		// Add button to layout
		addJokeLayout.addView(m_vwJokeButton);

		// Add Joke text box
		m_vwJokeEditText = new EditText(this);

		// Set specific params for the text box to fill the parent
		m_vwJokeEditText.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));

		// Add Joke text box to layout
		addJokeLayout.addView(m_vwJokeEditText);

		// Add the Add Joke layout to the root layout
		localLinear.addView(addJokeLayout);

		// Just like LinearLayout, ScrollView constructor takes a Context.
		// ScrollView is a FrameLayout that becomes scrollable when child views
		// are larger than screen area.
		ScrollView sv = new ScrollView(this);

		// Add our LinearLayout to the ScrollView
		sv.addView(m_vwJokeLayout);

		// Finally, add the ScrollView to the root layout
		localLinear.addView(sv);

		// We want to actually display the ScrollView to the user
		// Since our layout is hierarchical, we must pass in the root of the
		// layouts.
		this.setContentView(localLinear);
	}
}
