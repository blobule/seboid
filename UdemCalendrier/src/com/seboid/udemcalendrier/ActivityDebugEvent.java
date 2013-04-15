package com.seboid.udemcalendrier;

import com.seboid.udemcalendrier.ActivityDebugEvents.myASyncLoader;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.ForceLoadContentObserver;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

//
// Affiche un seul evenement
//

public class ActivityDebugEvent extends FragmentActivity implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	int id; // item a afficher
	TextView tTitre;
	TextView tDate;
	TextView tHeure;
	WebView web;
	
	DBHelper dbh;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.event);

		// item a afficher, ou -1 si rien de prevu
		id = this.getIntent().getIntExtra("id", -1);
		Log.d("event", "event id to load is " + id);

		tTitre = (TextView) findViewById(R.id.text_titre);
		tDate = (TextView) findViewById(R.id.text_date);
		tHeure = (TextView) findViewById(R.id.text_heure);
		web = (WebView) findViewById(R.id.web_desc);

		// la base de donnees
		dbh = new DBHelper(this);
		db = dbh.getReadableDatabase();
		
		// notre activite va fournir les callbacks de ce loader.
		getSupportLoaderManager().initLoader(0, null,
				(android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>) this);
		
	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
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
		//new DownloadEventTask(id).execute();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

//	//
//	// web asynchrone
//	//
//
//	class DownloadEventTask extends AsyncTask<Void, Void, EventAPI> {
//
//		int id; // item a lire
//
//		public DownloadEventTask(int id) {
//			super();
//			this.id = id;
//		}
//
//		@Override
//		protected void onPreExecute() {
//			setProgressBarIndeterminateVisibility(true);
//		}
//
//		@Override
//		protected EventAPI doInBackground(Void... arg0) {
//			EventAPI event = new EventAPI(id);
//			return event;
//		}
//
//		@Override
//		protected void onPostExecute(EventAPI result) {
//
//		}
//	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	//
	// callback de loader
	//

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new myASyncLoader(this.getApplicationContext(), id,db);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		int nb = c.getCount();
		if (nb < 1) return;
		
		c.moveToFirst();
		//
		// affiche!
		//
		tTitre.setText(c.getString(c.getColumnIndex("titre")));
		tDate.setText(c.getString(c.getColumnIndex("date")));
		tHeure.setText(c.getString(c.getColumnIndex("heure_debut")) + " a "
				+ c.getString(c.getColumnIndex("heure_fin")));

		web.setScrollContainer(true);
		web.setScrollbarFadingEnabled(false);
		web.setBackgroundColor(0xff000000);
		web.getSettings().setJavaScriptEnabled(false);
		web.loadDataWithBaseURL(
				null,
				"<style type=\"text/css\">body { color:" + "#ffffff"
						+ "; background-color:" + "#000000" + " } a { color:"
						+ "#8080ff" + "; } h2 { color:" + "#ffffff"
						+ "; } </style><body>"
						+ "<img width=\"50%\" src=\""+c.getString(c.getColumnIndex("image"))+"\">"
						+ c.getString(c.getColumnIndex("description"))
						+ "</body>", "text/html", "utf-8", null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	//
	// le loader asynchrone
	//

	public static class myASyncLoader extends AsyncTaskLoader<Cursor> {
		final ForceLoadContentObserver mObserver; // en cas de forceload

		Cursor c; // le data qui est retourne

		SQLiteDatabase db;
		int id; // id de l'event

		public myASyncLoader(Context context, int id,SQLiteDatabase db) {
			super(context);
			mObserver = new ForceLoadContentObserver();
			this.db = db;
			this.id = id;
			c = null;
		}

		@Override
		public void deliverResult(Cursor data) {
			Log.d("asyncloader", "deliver results");
			//setProgressBarIndeterminateVisibility(false);

			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (c != null) {
					releaseResources(c);
				}
			}
			// pour empecher un free() sur les vieux datas (qui sont
			// reutilises...)
			Cursor old = c;
			c = data;

			// If the Loader is currently started, we can immediately
			// deliver its results.
			if (isStarted()) {
				super.deliverResult(data);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (old != null && old != c) {
				releaseResources(old);
			}
		}

		// si on a des ressources a fermer
		protected void releaseResources(Cursor cfc) {
			Log.d("async", "should releasing cursor");
			if (c != null && !c.isClosed())
				c.close();
		}

		@Override
		public Cursor loadInBackground() {
			// synchronized (this) {
			// if (isLoadInBackgroundCanceled()) {
			// throw new OperationCanceledException();
			// }
			// mCancellationSignal = new CancellationSignal();
			// }
			// try {

			// count total
			Log.d("asyncloader", "load in background");

			// le query devrait etre passe en parametre...
			String query = "select "+DBHelper.C_C_ID+","
					+DBHelper.C_TITRE+","
					+DBHelper.C_DATE+","
					+DBHelper.C_HEURE_DEBUT+","
					+DBHelper.C_HEURE_FIN+","
					+DBHelper.C_DESCRIPTION+","
					+DBHelper.C_IMAGE
					+" from " + DBHelper.TABLE_E + " where _id=" + id;

			Cursor c = db.rawQuery(query, null);

			if (c != null) {
				// Ensure the cursor window is filled
				c.getCount();
				registerContentObserver(c, mObserver);
			}

			return c;
			// } finally {
			// synchronized (this) {
			// mCancellationSignal = null;
			// }
			// }
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
			//setProgressBarIndeterminateVisibility(true);

			Log.d("async", "on start loading");
			if (c != null) {
				deliverResult(c); // already available!
			}
			if (takeContentChanged() || c == null)
				forceLoad();
		}

		@Override
		protected void onStopLoading() {
			Log.d("async", "on stop loading (not stopping, really...)");
			// cancelLoad();
		}

		@Override
		public void onCanceled(Cursor c) {
			if (c != null && !c.isClosed())
				c.close();
		}

		@Override
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			if (c != null && !c.isClosed())
				c.close();
			c = null;
		}
	}

}
