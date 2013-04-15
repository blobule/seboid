package com.seboid.udemcalendrier;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityDebugEvents extends FragmentActivity implements OnItemClickListener,
LoaderManager.LoaderCallbacks<Cursor> {

	ListView listv;
	SimpleCursorAdapter adapter;
		
	// arrive directement de Extra dans l'intent
	final String[] fromRef= {"_id","titre","date"};
	final int[] toRef= { R.id.text3,R.id.text1,R.id.text2 };
	final String queryRef = "select _id,titre,date from "+DBHelper.TABLE_E+" order by date asc";
	final String titleRef = "Events";

	String[] from;
	int[] to;
	String query;
	int layout; // d'une rangee
	String title;
	int type; // 0=events, 1=categories
	
	final int LOADER_ID=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		

		Intent in=getIntent();
		
		from=in.getStringArrayExtra("from");
		if( from==null ) from=fromRef;
		
		to=in.getIntArrayExtra("to");
		if( to==null ) to=toRef;
		
		query=in.getStringExtra("query");
		if( query==null ) query=queryRef;
		
		layout=in.getIntExtra("layout", R.layout.events_row);
		
		title=in.getStringExtra("title");
		if( title==null ) title=titleRef;
		
		type=in.getIntExtra("type", 0); // 0 is events, 1 is categories
		
		setTitle(title);
		
		setContentView(R.layout.events);
		
		listv=(ListView)findViewById(R.id.listViewEvents);

		listv.setOnItemClickListener(this);

		
		
		//
		// affiche!
		//
		adapter=new SimpleCursorAdapter(ActivityDebugEvents.this, layout, null,from,to,0);
		listv.setAdapter( adapter );
		
		// notre activite va fournir les callbacks de ce loader.
		getSupportLoaderManager().initLoader(LOADER_ID, null,
				(android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>) this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	protected void onResume() {
		super.onResume();
		//new DownloadEventsTask().execute();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Toast.makeText(ActivityDebugEvents.this, "item "+position+":"+id, Toast.LENGTH_LONG).show();
		if( type==0 ) {
			Intent in=new Intent(ActivityDebugEvents.this,ActivityDebugEvent.class);
			// le view doit avoir un tag qui contient le id...
			in.putExtra("id",(int)id);
			startActivity(in);
		}else if( type==1 ) {
			// categories -> events avec un like
			Intent in=new Intent(ActivityDebugEvents.this,ActivityDebugEvents.class);
			// le view doit avoir un tag qui contient le id...
			in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_categories like '%:"+id+":%' order by date asc");
			startActivity(in);
		}else if( type==2 ) {
			// souscategories -> events avec un like
			Intent in=new Intent(ActivityDebugEvents.this,ActivityDebugEvents.class);
			// le view doit avoir un tag qui contient le id...
			in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_souscategories like '%:"+id+":%' order by date asc");
			startActivity(in);
		}else if( type==3 ) {
			// groupes -> events avec un like
			Intent in=new Intent(ActivityDebugEvents.this,ActivityDebugEvents.class);
			// le view doit avoir un tag qui contient le id...
			in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_groupes like '%:"+id+":%' order by date asc");
			startActivity(in);
		}else if( type==4 ) {
			// lieux -> events avec ce lieu
			Intent in=new Intent(ActivityDebugEvents.this,ActivityDebugEvents.class);
			// le view doit avoir un tag qui contient le id...
			in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_lieux like '%:"+id+":%' order by date asc");
			startActivity(in);
		}else if( type==5 ) {
			if( isGoogleMapsInstalled() ) {
			Cursor c=adapter.getCursor();
			c.moveToPosition(position);
			double lat=c.getDouble(2);
			double lng=c.getDouble(3);
			//Uri uri = Uri.parse("geo:0,0?q=22.99948365856307,72.60040283203125 (Maninagar)");
//			Uri uri = Uri.parse("geo:0,0?q="+lat+","+lng);//+" ("+c.getString(1)+")");
			Uri uri = Uri.parse("geo:"+lat+","+lng);//+" ("+c.getString(1)+")");

			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			}else{
				Toast.makeText(this, "Pas de google map api!",Toast.LENGTH_LONG).show();
			}
		}
	}

	
	//
	// web asynchrone
	//

//	class DownloadEventsTask extends AsyncTask<Void,Void,EventsAPI> {
//		@Override
//		protected void onPreExecute() {
//			setProgressBarIndeterminateVisibility(true);
//		}
//
//		@Override
//		protected EventsAPI doInBackground(Void... arg0) {
//			//EventsAPI events=new EventsAPI("evenements",null,"2013-03-27","2013-03-27");
//			//EventsAPI events=new EventsAPI("categorie","2","2013-03-27","2013-03-30");
//			//EventsAPI events=new EventsAPI("groupe","7","2013-03-27","2013-03-30");
//			//EventsAPI events=new EventsAPI("souscategorie","7","2013-03-27","2013-03-30");
//			//EventsAPI events=new EventsAPI("serie","cfd4b81e5657376b6",null,null);
//
//			// aujourd'hui a dans une semaine
//			Time now = new Time(Time.getCurrentTimezone());
//			now.setToNow();
//			//now.allDay=true;
//			//now.normalize(true);
//			Log.d("events","today is "+now.format("%y-%m-%d"));
//			now.set(now.toMillis(true)+6*24*3600*1000);
//			Log.d("events","next week is "+now.format("%Y-%m-%d"));
//
//			EventsAPI e;
//
//			// le 2013-03-27 -> contient un super gros description <img base64 >
//			e=new EventsAPI("evenements",null,"2013-03-27","2013-04-02");
//			return e;
//		}
//
//		@Override
//		protected void onPostExecute(EventsAPI result) {
//			setProgressBarIndeterminateVisibility(false);
//			//Toast.makeText(ActivityDebugEvents.this, "done", Toast.LENGTH_SHORT).show();
//
//			//Log.d("event","week load is "+result.time/1000.0+" sec");
//			Toast.makeText(ActivityDebugEvents.this,"Load time is "+result.time/1000.0+" sec",Toast.LENGTH_LONG).show();
//
//		}
//	}

	//
	// loader callbacks
	//

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle b) {
		return new myASyncLoader(this.getApplicationContext(),query);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		// les deux infos fraichement loadees
		adapter.swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
	
	//
	// Cursor async loader
	//

	public static class myASyncLoader extends AsyncTaskLoader<Cursor> {
		final ForceLoadContentObserver mObserver; // en cas de forceload

		Cursor c; // le data qui est retourne

		DBHelper dbh;
		SQLiteDatabase db;
		String query;

		public myASyncLoader(Context context,String query) {
			super(context);
			mObserver = new ForceLoadContentObserver();			
			dbh=new DBHelper(context);
			db=dbh.getReadableDatabase();
			this.query=query;
			c=null;
		}

		@Override
		public void deliverResult(Cursor data) {
			Log.d("asyncloader","deliver results");

			if (isReset()) {
				// An async query came in while the loader is stopped.  We
				// don't need the result.
				if (c != null) { releaseResources(c); }
			}
			// pour empecher un free() sur les vieux datas (qui sont reutilises...)
			Cursor old=c;
			c=data;

			// If the Loader is currently started, we can immediately
			// deliver its results.
			if (isStarted()) { super.deliverResult(data); }

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (old != null && old!=c ) { releaseResources(old); }
		}

		// si on a des ressources a fermer
		protected void releaseResources(Cursor cfc) {
			Log.d("async","should releasing cursor");
			if( c!=null && !c.isClosed() ) c.close();
		}

		@Override
		public Cursor loadInBackground() {
			//			 synchronized (this) {
			//		            if (isLoadInBackgroundCanceled()) {
			//		                throw new OperationCanceledException();
			//		            }
			//		            mCancellationSignal = new CancellationSignal();
			//		        }
			//		        try {

			// count total
			Log.d("asyncloader","load in background");

			// le query devrait etre passe en parametre...
			Cursor c=db.rawQuery(query, null);

			if( c != null) {
				// Ensure the cursor window is filled
				c.getCount();
				registerContentObserver(c, mObserver);
			}

			return c;
			//		        } finally {
			//		            synchronized (this) {
			//		                mCancellationSignal = null;
			//		            }
			//		        }
		}

		/**
		 * Registers an observer to get notifications from the content provider
		 * when the cursor needs to be refreshed.
		 */
		void registerContentObserver(Cursor cursor, ContentObserver observer) {
			cursor.registerContentObserver(mObserver);
		}

		@Override
		protected void onStartLoading() {
			// TODO Auto-generated method stub
			super.onStartLoading();
			Log.d("async","on start loading");
			if( c!=null ) {
				deliverResult(c); // already available!
			}
			if( takeContentChanged() || c==null ) forceLoad();
		}

		@Override
		protected void onStopLoading() {
			Log.d("async","on stop loading (not stopping, really...)");
			//cancelLoad();
		}

		@Override
		public void onCanceled(Cursor c) {
			if( c!=null && !c.isClosed() ) c.close();
		}

		@Override
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			if( c!=null && !c.isClosed()) c.close();
			c=null;
		}


	}


	public boolean isGoogleMapsInstalled()
	{
	    try
	    {
	        ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
	        return true;
	    } 
	    catch(PackageManager.NameNotFoundException e)
	    {
	        return false;
	    }
	}



}
