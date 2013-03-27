package com.seboid.udemcalendrier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ActivityDebug extends Activity implements OnClickListener {

	Button eventsB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		
		eventsB=(Button)findViewById(R.id.buttonEvents);
		eventsB.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch( v.getId() ) {
		case R.id.buttonEvents:
			Toast.makeText(this,"events",Toast.LENGTH_LONG).show();
			Intent in = new Intent(this,ActivityDebugEvents.class);
			startActivity(in);
			break;
		}
	}

}
