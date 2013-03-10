package com.seboid.udem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;


//
// activite principale qui affiche la liste des articles
//
// on va utiliser un loadermanager, pour etre moderne...
//

public class ActivityUdeMNouvelles extends FragmentActivity implements
 LoaderManager.LoaderCallbacks<Cursor> {

	// The loader's unique id. Loader ids are specific to the Activity or
	// Fragment in which they reside.
	private static final int LOADER_ID = 1;


	MySimpleCursorAdapter adapter; // pour afficher les lignes

	// busy affichage
	IntentFilter busyFilter;
	BusyReceiver busyR;


	//
	// elements d'interface
	//
	ListView lv;

	//
	// position dans le listview (0 initialement)
	//
	int lastPos=0;

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
	static final String[] from = new String[] {
		DBHelper.C_TITLE,
		DBHelper.C_CATEGORY,
		DBHelper.C_TIME,
		DBHelper.C_FAVORI};
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
				// cette activite va nous retourner le id ou on est rendu...
				Intent in=new Intent(ActivityUdeMNouvelles.this,ActivityUdeMDetail.class);
				in.putExtra("id",id);

				if( type!=null ) {
					in.putExtra("where",type+" = "+ DatabaseUtils.sqlEscapeString(selection));
					in.putExtra("title",selection);
					//in.putExtra("pos", position);
					//cursor=db.query(DBHelper.TABLE, select ,(type!=null?(type+" = '"+selection+"'"):null), null, null, null, DBHelper.C_TIME+" DESC");	
				}
				//startActivity(in);
				startActivityForResult(in,12345);

				//				displayWeb(dbH.getWebContent(db, id));
				//				displayWeb((String)rss.data.get(position).get("description"),
				//						(String)rss.data.get(position).get("title"),
				//						(String)rss.data.get(position).get("link"));
			}
		});

		// Initialize the adapter. Note that we pass a "null" Cursor as the
		// third argument. We will pass the adapter a Cursor only when the
		// data has finished loading for the first time (i.e. when the
		// LoaderManager delivers the data to onLoadFinished). Also note
		// that we have passed the "0" flag as the last argument. This
		// prevents the adapter from registering a ContentObserver for the
		// Cursor (the CursorLoader will do this for us!).
		adapter = new MySimpleCursorAdapter(this, R.layout.rowfav /* .row */, from, to);		
		lv.setAdapter(adapter);

		// The Activity (which implements the LoaderCallbacks<Cursor>
		// interface) is the callbacks object through which we will interact
		// with the LoaderManager. The LoaderManager uses this object to
		// instantiate the Loader and to notify the client when data is made
		// available/unavailable.

		// Initialize the Loader with id "0" and callbacks "mCallbacks".
		// If the loader doesn't already exist, one is created. Otherwise,
		// the already created Loader is reused. In either case, the
		// LoaderManager will manage the Loader across the Activity/Fragment
		// lifecycle, will receive any new loads once they have completed,
		// and will report this new data back to the "mCallbacks" object.
		getSupportLoaderManager().initLoader(LOADER_ID, null,  (android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>) this);


		
		// le busy receiver
		busyR=new BusyReceiver();
		busyFilter=new IntentFilter("com.seboid.udem.BUSY");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	@Override
	protected void onResume() {
		super.onResume();

		// get data from database
//		cursor=db.query(DBHelper.TABLE, select ,(type!=null?(type+" = "+DatabaseUtils.sqlEscapeString(selection)):null), null, null, null, DBHelper.C_TIME+" DESC");
//		startManagingCursor(cursor);
		
		// enregistre le receiver pour l'etat busy
		registerReceiver(busyR,busyFilter);

		Log.d("nouvelles","making visible pos="+lastPos);
		ensureVisible(lv,lastPos);
		
		// on doit relire parce que les "lu" sont modifies...
		getSupportLoaderManager().getLoader(LOADER_ID).forceLoad();
	}

	@Override
	protected void onPause() {
		// on se rappellera ou on est...
		// comme ca quand on revient...
		//lastPos=lv.getFirstVisiblePosition();

		Log.d("nouvelles","onpause pos="+lastPos);

		super.onPause();
		unregisterReceiver(busyR);
	}


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 12345) {
			if(resultCode == RESULT_OK){      
				//data.getStringExtra("result");          
				// ou on etait rendu???
				lastPos=data.getIntExtra("pos",0);
				Log.d("nouvelles","got result pos="+lastPos);
				//ensureVisible(lv,lastPos);
			}
			//if (resultCode == RESULT_CANCELED) { }
		}
	}

	public static void ensureVisible(ListView listView, int pos)
	{
		if (listView == null) return;
		if(pos < 0 || pos >= listView.getCount()) return;

		int first = listView.getFirstVisiblePosition();
		int last = listView.getLastVisiblePosition();

		if (pos < first) listView.setSelection(pos);
		else if (pos >= last) listView.setSelection(1 + pos - (last - first));
	}


	//
	// Menu et actions
	//
	
	
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
			//		case R.id.menufeed:
			//			startActivity(new Intent(this, ActivityUdeMListFeed.class));
			//			break;
			//		case R.id.menucat:
			//			startActivity(new Intent(this, ActivityUdeMListCat.class));
			//			break;
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
			//if( !busy ) cursor.requery(); // on vient de finir une mise a jour...
			/// comment dire qu'on doit reloader les data??????
		}		
	}

	//
	// note adapter personalise avec un bindview special
	//


	class MySimpleCursorAdapter extends SimpleCursorAdapter {


		public MySimpleCursorAdapter(Context context, int layout,
				String[] from, int[] to) {
			// on passe null comme cursor comme ca il sera manage par un loader.
			super(context, layout, null, from, to,0);
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			boolean lu=c.getInt(5)==1;

			TextView tv=(TextView)view.findViewById(R.id.rowcatcount);
			long timestamp=c.getLong(3)*1000; // sec -> millisec
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
			tv.setText(relTime);
			//tv.setTypeface(null,lu?Typeface.NORMAL:Typeface.BOLD);


			View v=view.findViewById(R.id.rowfav);
			v.setVisibility( View.INVISIBLE );

			tv=(TextView)view.findViewById(R.id.rowtitle);
			tv.setText(c.getString(1));

			//tv.setBackgroundColor(lu?0xff333333:0xff000000);

			tv=(TextView)view.findViewById(R.id.rowcat);
			tv.setText(c.getString(2));

			tv=(TextView)view.findViewById(R.id.alire);
			tv.setVisibility(lu?View.GONE:View.VISIBLE);
		}
	}

	//
	// loader manager. Ce sont les callbacks....
	//


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Create a new CursorLoader with the following query parameters.		
		CursorLoader cursorLoader = new CursorLoader(this,
				UdeMContentProvider.CONTENT_URI,
				select,
				(type!=null?(type+" = "+DatabaseUtils.sqlEscapeString(selection)):null),
				null,
				DBHelper.C_TIME+" DESC");		
//		(type!=null?(type+" = "+DatabaseUtils.sqlEscapeString(selection)):null), null, null, null, DBHelper.C_TIME+" DESC");
//		startManagingCursor(cursor);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// A switch-case is useful when dealing with multiple Loaders/IDs
		switch (loader.getId()) {
		case LOADER_ID:
			// The asynchronous load is complete and the data
			// is now available for use. Only now can we associate
			// the queried Cursor with the SimpleCursorAdapter.
			adapter.swapCursor(cursor); // api11 seulement... sinon utiliser support.v4.simpleadapter
			break;
		}
		// The listview now displays the queried data.
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// For whatever reason, the Loader's data is now unavailable.
		// Remove any references to the old data by replacing it with
		// a null Cursor.
		adapter.swapCursor(null);
	}

}

