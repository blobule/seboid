package com.seboid.udemcalendrier;

import java.util.HashMap;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

//
// ce service va chercher les events
// et fait une mise a jour de la bd
//


public class ServiceMiseAJour extends IntentService {

	//public static final String NEW_STATUS_INTENT="com.seboid.udem.newstatus";

	Handler mHandler; // pour afficher des toast a partir du handleintent qui est dans un autre thread

	static final String TAG="service";

	public ServiceMiseAJour() {
		super(TAG); // important
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler=new Handler();
	}	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onstartcommand");
		return super.onStartCommand(intent, flags, startId);
	}


	// onHandleIntent tourne dans un autre thread....
	@Override
	public void onHandleIntent(Intent intent) {
		Log.d(TAG,"onHandle!");

		if( !NetUtil.networkOK(this.getApplicationContext()) ) {
			 mHandler.post(new Runnable() {            
			        @Override
			        public void run() {
			            Toast.makeText(ServiceMiseAJour.this,"Pas d'accès au réseau", Toast.LENGTH_LONG).show();                
			        }
			    });
			return;
		}
		
		// commence par demander un "busy" si l'app ecoute ce signal...
		Intent in=new Intent("com.seboid.udem.BUSY");
		in.putExtra("busy",true);
		sendBroadcast(in);

		// extract period info..
		long now=System.currentTimeMillis();
		long start=intent.getLongExtra("start",now);
		long stop=intent.getLongExtra("stop",now+6*24*3600*1000);

		String startDay=TempsUtil.aujourdhui(start,0);
		String stopDay=TempsUtil.aujourdhui(stop,0);
		
		NotificationManager mNM;
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(ServiceMiseAJour.this);

		//long past = (long)(System.currentTimeMillis()/1000 - Long.parseLong(preferences.getString("savetime","365"))*24*3600);		

		//
		// load all event summaries
		//
		EventsAPI events=new EventsAPI("evenements",null,startDay,stopDay);
		if( events==null || events.erreur!=null ) {
			Log.d(TAG,"events null");
			// enlever le BUSY
			in=new Intent("com.seboid.udem.BUSY");
			in.putExtra("busy",false);
			sendBroadcast(in);

			return;
		}

		DBHelper dbH=new DBHelper(this);
		SQLiteDatabase db=dbH.getWritableDatabase();

		int nb=0;
		
		HashMap<String,String> hm;
		ContentValues val = new ContentValues();
		for(int i=0;i<events.hmList.size();i++) {
			hm=events.hmList.get(i);

			// passer en determinate
			in=new Intent("com.seboid.udem.BUSY");
			in.putExtra("progress",(i*10000)/(events.hmList.size()-1));
			sendBroadcast(in);
			
			//if( (Integer)hm.get("time") < past ) { }

			int id=Integer.parseInt(hm.get("id"));
			long modif=Long.parseLong(hm.get("epoch_modif"));
			
			// ajouter a la base de donnee
			val.clear();
			val.put(DBHelper.C_ID,id);
			val.put(DBHelper.C_TITRE,hm.get("titre"));
			val.put(DBHelper.C_DESCRIPTION,hm.get("description"));
			val.put(DBHelper.C_CONTACT_NOM,hm.get("contact_nom"));
			val.put(DBHelper.C_CONTACT_COURRIEL,hm.get("contact_courriel"));
			val.put(DBHelper.C_CONTACT_TEL,hm.get("contact_tel"));
			val.put(DBHelper.C_CONTACT_URL,hm.get("contact_url"));
			val.put(DBHelper.C_SERIE,hm.get("serie"));
			val.put(DBHelper.C_COUT,hm.get("cout"));
			val.put(DBHelper.C_DATE,hm.get("date"));
			val.put(DBHelper.C_HEURE_DEBUT,hm.get("heure_debut"));
			val.put(DBHelper.C_HEURE_FIN,hm.get("heure_fin"));
			val.put(DBHelper.C_DATE_MODIF,hm.get("date_modif"));
			val.put(DBHelper.C_TYPE_HORAIRE,hm.get("type_horaire"));
			val.put(DBHelper.C_VIGNETTE,hm.get("vignette"));
			val.put(DBHelper.C_IMAGE,hm.get("image"));
			val.put(DBHelper.C_EPOCH_DEBUT,Long.parseLong(hm.get("epoch_debut")));
			val.put(DBHelper.C_EPOCH_FIN,Long.parseLong(hm.get("epoch_fin")));
			val.put(DBHelper.C_EPOCH_MODIF,modif);
			val.put(DBHelper.C_IDS_LIEUX,hm.get("ids_lieux"));
			val.put(DBHelper.C_IDS_GROUPES,hm.get("ids_groupes"));
			val.put(DBHelper.C_IDS_CATEGORIES,hm.get("ids_categories"));
			val.put(DBHelper.C_IDS_SOUSCATEGORIES,hm.get("ids_souscategories"));

			//			try {
			//				Uri u=getContentResolver().insert(UdeMContentProvider.CONTENT_URI, val);
			//				if( u!=null ) nb++;
			//			} catch( SQLiteConstraintException e ) {
			//				Log.d(TAG,"already inserted");
			//			}
			try {
				db.insertOrThrow(DBHelper.TABLE_E, null, val);
				nb++;
			} catch ( SQLException e ) {
				// deja dans la base de donnee...
				// on va verifier le epoch_modif de la bd et comparer au notre...
				// si ce cursor est vide, c'est que la date de modif n'a pas changee
				Cursor c=db.query(DBHelper.TABLE_E, new String[] {DBHelper.C_ID,DBHelper.C_EPOCH_MODIF} ,
						"_id="+id+ " and "+DBHelper.C_EPOCH_MODIF+"<"+modif , null, null, null, null);
				if( c==null ) continue;
				int count=c.getCount();
				c.close();
				if( count==0 ) continue; 
				// on a trouve quelque chose... on doit donc faire un update sur cet event.
			}
			
			//
			// va chercher chaque event individuel...
			//
			EventAPI event=new EventAPI(id);
			if( events==null || events.erreur!=null ) {
				Log.d(TAG,"event "+id+" not loaded");
				continue;
			}
			// update a partir de l'evenement complet
			hm=event.base;
			
			// ajouter a la base de donnee
			val.clear();
			val.put(DBHelper.C_ID,id);
			val.put(DBHelper.C_TITRE,hm.get("titre"));
			val.put(DBHelper.C_DESCRIPTION,hm.get("description"));
			val.put(DBHelper.C_CONTACT_NOM,hm.get("contact_nom"));
			val.put(DBHelper.C_CONTACT_COURRIEL,hm.get("contact_courriel"));
			val.put(DBHelper.C_CONTACT_TEL,hm.get("contact_tel"));
			val.put(DBHelper.C_CONTACT_URL,hm.get("contact_url"));
			val.put(DBHelper.C_SERIE,hm.get("serie"));
			val.put(DBHelper.C_COUT,hm.get("cout"));
			val.put(DBHelper.C_DATE,hm.get("date"));
			val.put(DBHelper.C_HEURE_DEBUT,hm.get("heure_debut"));
			val.put(DBHelper.C_HEURE_FIN,hm.get("heure_fin"));
			val.put(DBHelper.C_DATE_MODIF,hm.get("date_modif"));
			val.put(DBHelper.C_TYPE_HORAIRE,hm.get("type_horaire"));
			val.put(DBHelper.C_VIGNETTE,hm.get("vignette"));
			val.put(DBHelper.C_IMAGE,hm.get("image"));
			val.put(DBHelper.C_EPOCH_DEBUT,Long.parseLong(hm.get("epoch_debut")));
			val.put(DBHelper.C_EPOCH_FIN,Long.parseLong(hm.get("epoch_fin")));
			val.put(DBHelper.C_EPOCH_MODIF,Long.parseLong(hm.get("epoch_modif")));
			val.put(DBHelper.C_IDS_LIEUX,hm.get("ids_lieux"));
			val.put(DBHelper.C_IDS_GROUPES,hm.get("ids_groupes"));
			val.put(DBHelper.C_IDS_CATEGORIES,hm.get("ids_categories"));
			val.put(DBHelper.C_IDS_SOUSCATEGORIES,hm.get("ids_souscategories"));
			
			try {
				db.update(DBHelper.TABLE_E, val, "_id="+id, null);
				nb++;
			} catch ( SQLException e ) {}

			// categories
			for(i=0;i<event.catList.size();i++) {
				hm=event.catList.get(i);
				val.clear();
				val.put(DBHelper.C_C_ID,hm.get("id_categorie"));
				val.put(DBHelper.C_C_DESC,hm.get("categorie_nom"));
				try {
					db.insertOrThrow(DBHelper.TABLE_C, null, val);
				} catch ( SQLException e ) {}
			}
			
			// souscategories
			for(i=0;i<event.souscatList.size();i++) {
				hm=event.souscatList.get(i);
				val.clear();
				val.put(DBHelper.C_SC_ID,hm.get("id_categorie"));
				val.put(DBHelper.C_SC_DESC,hm.get("categorie_nom"));
				try {
					db.insertOrThrow(DBHelper.TABLE_SC, null, val);
				} catch ( SQLException e ) {}
			}
			
			// groupes
			for(i=0;i<event.groupeList.size();i++) {
				hm=event.groupeList.get(i);
				val.clear();
				val.put(DBHelper.C_SC_ID,hm.get("id_groupe"));
				val.put(DBHelper.C_SC_DESC,hm.get("groupe_nom"));
				try {
					db.insertOrThrow(DBHelper.TABLE_G, null, val);
				} catch ( SQLException e ) {}
			}

			// lieux
			for(i=0;i<event.lieuList.size();i++) {
				hm=event.lieuList.get(i);
				val.clear();
				val.put(DBHelper.C_SC_ID,hm.get("id_lieu"));
				val.put(DBHelper.C_SC_DESC,hm.get("lieu_nom"));
				val.put(DBHelper.C_L_SALLE,hm.get("salle"));
				val.put(DBHelper.C_L_ADRESSE,hm.get("adresse"));
				val.put(DBHelper.C_L_ADRESSE2,hm.get("adresse2"));
				val.put(DBHelper.C_L_VILLE,hm.get("ville"));
				val.put(DBHelper.C_L_PROVINCE,hm.get("province"));
				val.put(DBHelper.C_L_PAYS,hm.get("pays"));
				val.put(DBHelper.C_L_CODEPOSTAL,hm.get("code_postal"));
				try {
				val.put(DBHelper.C_L_LATITUDE,Double.parseDouble(hm.get("latitude")));
				val.put(DBHelper.C_L_LONGITUDE,Double.parseDouble(hm.get("longitude")));
				} catch ( NumberFormatException e ) { }; // un null probablement
				try {
					db.insertOrThrow(DBHelper.TABLE_L, null, val);
				} catch ( SQLException e ) {}
			}
		}

		Log.d(TAG,"added "+nb+" events");
		

			//				try {
			//					db.update(DBHelper.TABLE, val, DBHelper.C_ID+"="+id , null);
			//				} catch ( SQLException e ) {
			//					Log.d("service","probleme de update dans la DB :-(");
			//				}


		//// now remove everything that is too old...
//		int k=getContentResolver().delete(UdeMContentProvider.CONTENT_URI, DBHelper.C_TIME+" < "+past, null);

		// efface tout ce qui est trop vieux (dans le passe, par exemple		
//		int k=db.delete(DBHelper.TABLE, DBHelper.C_TIME+" < "+past, null);
//		Log.d(TAG,"removed "+k+" old messages");

		// libere la memoire
		events=null;
		db.close();

		String info;
		if( nb==0 ) info="Aucun nouvel évenement.";
		else info=nb+(nb>1?" nouveaux évenements.":" nouvel évenement.");

//		// termine en enlevant le "busy" si l'app ecoute ce signal...
		in=new Intent("com.seboid.udem.BUSY");
		in.putExtra("busy",false);
		sendBroadcast(in);

		//
		// Verifions si le broadcastreceiver BUSY est disponible.
		// Si c'est le cas, on va faire un toast plutot qu'une notification
		//
		showNotification(mNM,info);

		//scheduleNextUpdate();
	}


	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * 
	 * ce ne trouve pas les receiver enregistres dynamiquement... seulement ceux des manifestes
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 * responded to, false otherwise.
	 */
	//	public static boolean isBroadcastReceiverAvailable(Context context, String action) {
	//		final PackageManager packageManager = context.getPackageManager();
	//		final Intent intent = new Intent(action);
	//		List<ResolveInfo> list =
	//				packageManager.queryBroadcastReceivers(intent, PackageManager.MATCH_DEFAULT_ONLY);
	//				//packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
	//		return list.size() > 0;
	//	}

	//
	// Notification
	//
	// voir http://developer.android.com/guide/topics/ui/notifiers/notifications.html
	//
	private void showNotification(NotificationManager mNM,String msg) {
		// Set the icon, scrolling text and timestamp		

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setContentTitle("UdeM | Calendrier");
		mBuilder.setContentText(msg);

		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ActivityDebugEvents.class), 0);

		mBuilder.setContentIntent(contentIntent);
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.

		mNotificationManager.notify(R.string.app_name, mBuilder.build());
	}

}


