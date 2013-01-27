package com.seboid.udem;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

//
// activite principale qui affiche la liste des articles
//

public class UdeMRssListActivity extends Activity  {

	WebView web;
	ListView lv;
	SimpleCursorAdapter adapter; // pour afficher les lignes

	// busy affichage
	IntentFilter busyFilter;
	BusyReceiver busyR;

	DBHelper dbH;
	SQLiteDatabase db;
	Cursor cursor;

	// quel feed on affiche?
	// directment des params de demarrage...
	String type; // on peut mettre ici "feed" ou "category"
	String selection; // pour choisir ce qui sera afficher. sql: type=selection. Donc feed ou category
	String nice; // titre a afficher (peut etre null)

	// Mapping pour l'affiche. from contient les id des elements d'une rangees dans le mapping
	// to contient les id des elements d'interface
	// le select sert a choisir les rangees. C' est == from[] + ID
	static final String[] select = new String[] { DBHelper.C_ID,DBHelper.C_TITLE,DBHelper.C_CATEGORY,DBHelper.C_TIME,DBHelper.C_FAVORI};
	static final String[] from = new String[] { DBHelper.C_TITLE,DBHelper.C_CATEGORY,DBHelper.C_TIME,DBHelper.C_FAVORI};
	static final int[] to = new int[] {R.id.rowtitle,R.id.rowcat,R.id.rowdate,R.id.rowfav };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// pour le progress bar rotatif
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.rss);

		// le titre en gros
		//		TextView titreView=(TextView)findViewById(R.id.rsstitre);

		// attend des parametres au demarrage: feed et nice
		Intent sender=getIntent();
		type = sender.getStringExtra("type");
		selection = sender.getStringExtra("selection");
		//nice = sender.getStringExtra("nice");

		if( type==null ) selection=null;
		if( selection==null ) type=null;
		//		if( type==null ) nice="Toutes les nouvelles";
		//		if( nice==null ) nice=selection;

		//		titreView.setText(nice);

		lv=(ListView)findViewById(R.id.list);		
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				//Log.d("rss","a faire: afficher la description "+id);
				Intent in=new Intent(UdeMRssListActivity.this,UdeMWebActivity.class);
				in.putExtra("id",id);

				if( type!=null ) {
					in.putExtra("where",type+" = '"+selection+"'");
					//in.putExtra("pos", position);
					//cursor=db.query(DBHelper.TABLE, select ,(type!=null?(type+" = '"+selection+"'"):null), null, null, null, DBHelper.C_TIME+" DESC");	
				}
				startActivity(in);
				//				displayWeb(dbH.getWebContent(db, id));
				//				displayWeb((String)rss.data.get(position).get("description"),
				//						(String)rss.data.get(position).get("title"),
				//						(String)rss.data.get(position).get("link"));
			}
		});

		// access a la database
		dbH=new DBHelper(this);
		db=dbH.getReadableDatabase();

		// le busy receiver
		busyR=new BusyReceiver();
		busyFilter=new IntentFilter("com.seboid.udem.BUSY");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// ferme le database
		db.close();
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.d("rss","resume! type="+type+" selection="+selection);

		// get data from database
		cursor=db.query(DBHelper.TABLE, select ,(type!=null?(type+" = '"+selection+"'"):null), null, null, null, DBHelper.C_TIME+" DESC");
		startManagingCursor(cursor);

		// adapter
		adapter = new SimpleCursorAdapter(this, R.layout.rowfav /* .row */, cursor, from, to);
		adapter.setViewBinder(VIEW_BINDER); // pour auto definir le rendu des champs
		lv.setAdapter(adapter);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.principal, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menurefresh:			
			startService(new Intent(this,RssService.class));
			break;
		case R.id.menuprefs:
			// Launch Preference activity
			startActivity(new Intent(this, myPreferencesActivity.class));
			break;
		case R.id.menufeed:
			startActivity(new Intent(this, UdeMRssFeedActivity.class));
			break;
		case R.id.menucat:
			startActivity(new Intent(this, UdeMRssCatActivity.class));
			break;
		}
		return true;
	}

	//
	// controle de l'affichage des champs d'une ligne
	//

	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		public boolean setViewValue(View view, Cursor c, int index) {
			switch( view.getId() ) {
			case R.id.rowdate:
				long timestamp=c.getLong(index)*1000; // sec -> millisec
				//Log.d("time","time "+timestamp);
				CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
				((TextView)view).setText(relTime);
				break;
			case R.id.rowfav:
					view.setVisibility( c.getPosition()>5?View.INVISIBLE:View.VISIBLE);
				break;
			default: return false; // auto-render
			}
			return true;
		}
	};


	//
	// un broadcast receiver pour affiche le status "busy"...
	// on veut afficher busy quand le service travaille...
	//
	class BusyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent in) {
			boolean busy = in.getExtras().getBoolean("busy");
			setProgressBarIndeterminateVisibility(busy);
			if( !busy ) cursor.requery(); // on vient de finir une mise a jour...
		}		
	}






}

