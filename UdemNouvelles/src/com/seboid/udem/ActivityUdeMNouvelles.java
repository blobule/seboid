package com.seboid.udem;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import android.widget.Toast;

//
// activite principale qui affiche la liste des articles
//

public class ActivityUdeMNouvelles extends Activity  {

	WebView web;
	ListView lv;
	MySimpleCursorAdapter adapter; // pour afficher les lignes

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
	static final String[] select = new String[] {
		DBHelper.C_ID,
		DBHelper.C_TITLE,
		DBHelper.C_CATEGORY,
		DBHelper.C_TIME,
		DBHelper.C_FAVORI,
		DBHelper.C_LU};
	static final String[] from = new String[] { DBHelper.C_TITLE,DBHelper.C_CATEGORY,DBHelper.C_TIME,DBHelper.C_FAVORI};
	static final int[] to = new int[] {R.id.rowtitle,R.id.rowcat,R.id.rowcatcount,R.id.rowfav };

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

		if( type!=null ) this.setTitle("UdeM | "+selection);

		lv=(ListView)findViewById(R.id.list);		
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				Log.d("rss","a faire: afficher la description "+id);
				//				Intent in=new Intent(ActivityUdeMNouvelles.this,ActivityUdeMWeb.class);
				Intent in=new Intent(ActivityUdeMNouvelles.this,ActivityUdeMDetail.class);
				in.putExtra("id",id);

				if( type!=null ) {
					in.putExtra("where",type+" = "+ DatabaseUtils.sqlEscapeString(selection));
					in.putExtra("title",selection);
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

		// get data from database
		cursor=db.query(DBHelper.TABLE, select ,(type!=null?(type+" = "+DatabaseUtils.sqlEscapeString(selection)):null), null, null, null, DBHelper.C_TIME+" DESC");
		startManagingCursor(cursor);

		// adapter
		adapter = new MySimpleCursorAdapter(this, R.layout.rowfav /* .row */, cursor, from, to);
		//		adapter.setViewBinder(VIEW_BINDER); // pour auto definir le rendu des champs
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
			startService(new Intent(this,ServiceRss.class));
			break;
		case R.id.menuprefs:
			// Launch Preference activity
			startActivity(new Intent(this, ActivityPreferences.class));
			break;
		case R.id.menufeed:
			startActivity(new Intent(this, ActivityUdeMListFeed.class));
			break;
		case R.id.menucat:
			startActivity(new Intent(this, ActivityUdeMListCat.class));
			break;
		}
		return true;
	}

	//
	// controle de l'affichage des champs d'une ligne
	//


	//	static final ViewBinder VIEW_BINDER = new ViewBinder() {
	//		public boolean setViewValue(View view, Cursor c, int index) {
	//			switch( view.getId() ) {
	//			case R.id.rowcatcount:
	//				long timestamp=c.getLong(index)*1000; // sec -> millisec
	//				//Log.d("time","time "+timestamp);
	//				CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
	//				((TextView)view).setText(relTime);
	//				break;
	//			case R.id.rowfav:
	//					view.setVisibility( c.getPosition()>5?View.INVISIBLE:View.VISIBLE);
	//				break;
	//			default: return false; // auto-render
	//			}
	//			return true;
	//		}
	//	};


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



	class MySimpleCursorAdapter extends SimpleCursorAdapter {


		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			// TODO Auto-generated method stub
			//super.bindView(view, context, cursor);
			TextView tv=(TextView)view.findViewById(R.id.rowcatcount);
			long timestamp=c.getLong(3)*1000; // sec -> millisec
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
			tv.setText(relTime);

			View v=view.findViewById(R.id.rowfav);
			v.setVisibility( View.INVISIBLE );
			
			tv=(TextView)view.findViewById(R.id.rowtitle);
			tv.setText(c.getString(1));
			
			boolean lu=c.getInt(5)==1;
			tv.setBackgroundColor(lu?0xff333333:0xff000000);

			tv=(TextView)view.findViewById(R.id.rowcat);
			tv.setText(c.getString(2));
			
		
		}


	}




}

