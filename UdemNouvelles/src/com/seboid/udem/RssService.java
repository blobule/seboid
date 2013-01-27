package com.seboid.udem;

import java.util.HashMap;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;



public class RssService extends IntentService {

	//public static final String NEW_STATUS_INTENT="com.seboid.udem.newstatus";


	static final String TAG="service";
	static final int DELAY=10*60*1000; // 10 minutes
	static final int SHORT_DELAY=5*1000; // 5 sec

	static final String[] feeds = new String[] {
		"recherche",
		"enseignement",
		"campus",
		"international",
		"culture",
		"sports",
		"multimedia",
		"revue-de-presse"
	};


	public RssService() {
		super("rssservice");
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("rssservice","onstart");
		return super.onStartCommand(intent, flags, startId);
	}

	
	@Override
	public void onHandleIntent(Intent intent) {
//	Toast.makeText(this, "Handle Service!",Toast.LENGTH_SHORT).show();
		Log.d("rssservice","onHandle!");

		// commence par demander un "busy" si l'app ecoute ce signal...
		Intent in=new Intent("com.seboid.udem.BUSY");
		in.putExtra("busy",true);
		sendBroadcast(in);
		
		NotificationManager mNM;
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		DBHelper dbH=new DBHelper(this); // a quoi sert le this ici? pour du UI?

		long past=0;

		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(RssService.this);
		int nb=0;

		SQLiteDatabase db=dbH.getWritableDatabase();
		RssAPI rss=null;

		for(int j=0;j<feeds.length;j++) {
			past = (long)(System.currentTimeMillis()/1000 - Long.parseLong(preferences.getString("savetime","365"))*24*3600);

			boolean use = preferences.getBoolean(feeds[j], false);
			Log.d(TAG,"Loading "+feeds[j]+" : "+use);
			if( !use ) {
				// remove this feed completely
				int k=db.delete(DBHelper.TABLE, DBHelper.C_FEED+" = '"+feeds[j]+"'", null);
				Log.d(TAG,"deleted feed "+feeds[j]+" ("+k+" messages)");
				continue;
			}

			rss=new RssAPI("http://www.nouvelles.umontreal.ca/"+feeds[j]+"/rss.html");
			if( rss==null || rss.erreur!=null ) {
				Log.d(TAG,"rss null for feed "+feeds[j]+". skipping");
				continue;
			}

			HashMap<String,Object> hm;
			ContentValues val = new ContentValues();
			for(int i=0;i<rss.data.size();i++) {
				hm=rss.data.get(i);

				if( (Integer)hm.get("time") < past ) {
					Log.d("service","skip "+hm.get("time")+" : "+hm.get("title"));
					continue;
				}
				Log.d("service","data "+hm.get("time")+" : "+hm.get("title"));


				// ajouter a la base de donnee
				val.clear();
				val.put(DBHelper.C_ID,(Integer)(((String)hm.get("link")).hashCode()));
				val.put(DBHelper.C_TITLE,(String)hm.get("title"));
				val.put(DBHelper.C_TIME, (Integer)hm.get("time"));
				val.put(DBHelper.C_CATEGORY, (String)hm.get("category"));
				val.put(DBHelper.C_FEED, feeds[j]);
				val.put(DBHelper.C_LINK, (String)hm.get("link"));
				val.put(DBHelper.C_DESC, (String)hm.get("description"));

				try {
					db.insertOrThrow(DBHelper.TABLE, null, val);
					nb++;
				} catch ( SQLException e ) {
				}
			}

		}

		//// now remove everything that is too old...
		try {
			int k=db.delete(DBHelper.TABLE, DBHelper.C_TIME+" < "+past, null);
			Log.d(TAG,"removed "+k+" old messages");
			//publishProgress(k+" messages enlevé(s)");
		} catch ( ClassCastException e ) {
			Log.d(TAG,"impossible de parser la preference duree de vie");
		}

		// libere la memoire
		rss=null;
		db.close();
		db=null;

		// termine en enlevant le "busy" si l'app ecoute ce signal...
		in=new Intent("com.seboid.udem.BUSY");
		in.putExtra("busy",false);
		sendBroadcast(in);
		
		if( nb==0 ) showNotification(mNM,"Aucun nouveau message.");
		if( nb>0 ) showNotification(mNM,nb+(nb>1?" nouveaux messages.":" nouveau message."));

	}


	private void showNotification(NotificationManager mNM,String msg) {
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.udem, msg,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, UdeMActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this,"UdeM Nouvelles",msg, contentIntent);

		// Send the notification.
		mNM.notify(R.string.app_name, notification);
	}



}


