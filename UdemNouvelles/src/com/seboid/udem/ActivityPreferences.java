package com.seboid.udem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.widget.Toast;

public class ActivityPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp,String key) {
		//Toast.makeText(ActivityPreferences.this,"pref changed!! ("+key+")" ,Toast.LENGTH_LONG).show();
		if( key.equals("autoupdate") || key.equals("intervallemobile") || key.equals("intervallewifi") ) {
				updateAlarm();
		}
	}

	public void updateAlarm() {
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Boolean auto=sp.getBoolean("autoupdate", false);
		if( auto ) {
			// debute le service auto update!
			long interval;
			int net=network(); // 0=mobile, 1=wifi, -1=no net

			if( net==0 ) interval=Integer.parseInt(sp.getString("intervallemobile","-1"))*60*1000;
			else if( net==1 ) interval=Integer.parseInt(sp.getString("intervallewifi","-1"))*60*1000;
			else interval=-1;

			// check network status ici
			scheduleUpdating(interval);
			//long interval=sp.getInt("intervallemobile",-1);
		}else{
			// arrete le service auto update;
			scheduleUpdating(-1);
		}
	}


	// interval <0 ==> enleve l'alarme
	private void scheduleUpdating(long interval)
	{
		Intent intent = new Intent(this, ServiceRss.class);
		PendingIntent pendingIntent =
				PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		long currentTimeMillis = System.currentTimeMillis();
		//	    long nextUpdateTimeMillis = currentTimeMillis + 20 * DateUtils.MINUTE_IN_MILLIS;

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
		if( interval>0 ) {
			alarmManager.setInexactRepeating(AlarmManager.RTC, currentTimeMillis, interval, pendingIntent);
			Toast.makeText(ActivityPreferences.this,"alarm set to "+interval/60/1000+"min" ,Toast.LENGTH_LONG).show();
		}else{
			alarmManager.cancel(pendingIntent);
			Toast.makeText(ActivityPreferences.this,"alarm cancel" ,Toast.LENGTH_LONG).show();
		}
	}


	// -1 = no network, 0=mobile, 1=wifi
	private int network() {		  
		ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean mobile=conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
		boolean wifi=conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

		//Toast.makeText(ActivityPreferences.this,"net mobile="+mobile+" : "+wifi, Toast.LENGTH_LONG).show();

		if( wifi ) return(1);
		if( mobile ) return(0);
		return(-1);
	}


}
