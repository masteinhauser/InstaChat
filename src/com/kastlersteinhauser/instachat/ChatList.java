package com.kastlersteinhauser.instachat;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ChatList extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chat_list, menu);
        return true;
    }
}
