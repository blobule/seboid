package com.seboid.udem;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ActivityDebug extends Activity implements OnClickListener {

	
	ToggleButton serviceTog;
	Button oneShot;
	Button resetDB;
	
	IntentFilter busyFilter;
	BusyReceiver busyR;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// pour le progress bar rotatif
		// doit etre appelle avant le setContentView
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.debug);

		serviceTog=(ToggleButton)findViewById(R.id.serviceToggle);
		serviceTog.setOnClickListener(this);

		oneShot=(Button)findViewById(R.id.oneshot);
		oneShot.setOnClickListener(this);

		resetDB=(Button)findViewById(R.id.resetdb);
		resetDB.setOnClickListener(this);
		
		((Button)findViewById(R.id.rssbasic)).setOnClickListener(this);
		((Button)findViewById(R.id.rss)).setOnClickListener(this);
		((Button)findViewById(R.id.rsscat)).setOnClickListener(this);     
		((Button)findViewById(R.id.rssfeedcat)).setOnClickListener(this);

		//Toast.makeText(this, "UdeM onCreate",Toast.LENGTH_SHORT).show();

		PendingIntent pi = PendingIntent.getService(this.getApplicationContext(), 0,
				new Intent(this,ServiceRss.class)
		, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am =(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		//		am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pi);
		am.cancel(pi);
		Log.d("udem","setup alarm");
		
		// le busy receiver
		busyR=new BusyReceiver();
		busyFilter=new IntentFilter("com.seboid.udem.BUSY");
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
		boolean auto = preferences.getBoolean("autoupdate", false);

		if( auto ) {
			// setup le auto alarm notifier
			Toast.makeText(this,"Mise à jour Auto activée",Toast.LENGTH_SHORT).show();
		}
		if( !auto ) {
			Toast.makeText(this,"Mise à jour Auto désactivée",Toast.LENGTH_SHORT).show();
		}		
		serviceTog.setChecked(auto);
		oneShot.setEnabled(true);
		
		// enregistre le receiver pour l'etat busy
		registerReceiver(busyR,busyFilter);
	}

	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(busyR);
	}
	

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.rssbasic:
			startActivity(new Intent(this, ActivityUdeMNouvelles.class));
			break;
		case R.id.rss:
			startActivity(new Intent(this, ActivityUdeMListFeed.class));
			break;
		case R.id.rsscat:
			startActivity(new Intent(this, ActivityUdeMListCat.class));
			break;
		case R.id.rssfeedcat:
			startActivity(new Intent(this, ActivityUdeMListFC.class));
			break;
		case R.id.resetdb:
			resetDB();
			break;
		case R.id.oneshot:
			Toast.makeText(ActivityDebug.this, "service is "+ServiceRss.class.getName(),Toast.LENGTH_LONG).show();
			//			Intent in = new Intent(RssService.class.getName());
			Intent in = new Intent(ActivityDebug.this,ServiceRss.class);



			//in.putExtra("oneshot","true");
			ActivityDebug.this.startService(in);
			break;
			//		case R.id.serviceToggle:
			//			if(serviceRunning) {
			//				stopService(new Intent(this,RssService.class));
			//				serviceRunning=false;				
			//			}else{
			//				startService(new Intent(this,RssService.class));
			//				serviceRunning=true;	
			//			}
			//			oneShot.setEnabled(!serviceRunning);
			//			Toast.makeText(UdeMAcsetProgressBarIndeterminateVisibility(true);tivity.this,serviceRunning?"Service activé":"Service arrêté",Toast.LENGTH_LONG).show();
			//			break;
		default:
			Toast.makeText(ActivityDebug.this, "pas disponible!",Toast.LENGTH_LONG).show();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.principal, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menurefresh:			
			startService(new Intent(this,ServiceRss.class));
			break;
		case R.id.menuprefs:
			// Launch Preference activity
			startActivity(new Intent(this, ActivityPreferences.class));
			break;
		}
		return true;
	}


	//
	// un broadcast receiver pour affiche le status "busy"...
	// on veut afficher busy quand le service travaille...
	//
	class BusyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent in) {
			boolean busy = in.getExtras().getBoolean("busy");
			setProgressBarIndeterminateVisibility(busy);
		}		
	}


	// vide la db
	void resetDB() {
		DBHelper dbH=new DBHelper(this);
		dbH.resetDB();
	}




}