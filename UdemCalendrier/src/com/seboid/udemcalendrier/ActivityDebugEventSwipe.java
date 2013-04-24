package com.seboid.udemcalendrier;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

//
// Affiche un seul evenement
// + swipe
//

public class ActivityDebugEventSwipe extends FragmentActivity implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	int id; // id de depart
	
	private ViewPager swipePager;
	private SwipeAdapter swipeAdapter;
	
	LayoutInflater inflater;
	
	DBHelper dbh;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//		setContentView(R.layout.event);	
		setContentView(R.layout.event_swipe);


		swipePager = (ViewPager) findViewById(R.id.awesomepager2);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		// item a afficher, ou -1 si rien de prevu
		id = this.getIntent().getIntExtra("id", -1);
		Log.d("event", "event id to load is " + id);



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
		getMenuInflater().inflate(R.menu.event, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_event_add:
			Intent in=swipeAdapter.getCurrentEventInfo(swipePager.getCurrentItem());
			startActivity(in);
			break;
//		case R.id.menuhelp:
//			instructions();
//			break;
		}
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
		
		// c contient notre id quelque part...
		int idx=c.getColumnIndex("_id");

		// trouve le bon id
		
		long delay=System.currentTimeMillis();
		c.moveToFirst();
		while( !c.isAfterLast() ) {
			if( c.getLong(idx)==id ) break;
			c.moveToNext();
		}
		if( c.isAfterLast() ) {
			Log.d("blub","pas trouve le id!!!!!!!!!!!!!");
			c.moveToFirst();
		}
		delay=System.currentTimeMillis()-delay;

		int pos=c.getPosition();
		Log.d("detail","load finished. got "+c.getCount()+" items. find id "+id+" at pos "+pos+" in "+delay+"ms");
		
			// creation du pageadapter!
		swipeAdapter = new SwipeAdapter(c);
		
		swipePager.setAdapter(swipeAdapter);
		Log.d("detail","1 swipePager has the adapter. current item pos is "+swipePager.getCurrentItem());
		swipePager.setCurrentItem(pos);
		Log.d("detail","2 swipePager has the adapter. current item pos is "+swipePager.getCurrentItem());
		swipePager.setCurrentItem(pos,true);
		Log.d("detail","3 swipePager has the adapter. current item pos is "+swipePager.getCurrentItem());
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
					+DBHelper.C_IMAGE+","
					+DBHelper.C_EPOCH_DEBUT
					+" from " + DBHelper.TABLE_E + " order by epoch_debut asc"; // on ramasse tous les evenements

//					+" from " + DBHelper.TABLE_E + " where _id=" + id;

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
	
	
	
	//
	// swipeAdapter
	//
	// cet adapteur affiche un curseur par jour a afficher...
	// on va dire que c'est 7 jours
	//
	private class SwipeAdapter extends PagerAdapter implements OnItemClickListener {

		// un listevie et un adapter par item... null -> pas encore vu...
		// on doit
//		ListView[] listv;
//		SimpleCursorAdapter[] adapter;
		Cursor c;

		public SwipeAdapter(Cursor c) {
			super();
//			listv = new ListView[getCount()];
//			adapter = new SimpleCursorAdapter[getCount()];
			this.c=c;
			Log.d("swipe","swipe adapter created.");
		}

		@Override
		public int getCount() {
			Log.d("swipe","swipe adapter has "+c.getCount()+" items");
			return c.getCount(); // ????? on doit chercher pour tous les events du bon genre...
		}

		/**
		 * Create the page for the given position. The adapter is responsible
		 * for adding the view to the container given here, although it only
		 * must ensure this is done by the time it returns from
		 * {@link #finishUpdate()}.			int id; // item a afficher

		 * 
		 * @param container
		 *            The containing View in which the page will be shown.
		 * @param position
		 *            The page position to be instantiated.
		 * @return Returns an Object representing the new page. This does not
		 *         need to be a View, but can be some other container of the
		 *         page.
		 */
		@Override
		public Object instantiateItem(View collection, int position) {
			Log.d("detail", "instantiate view @ " + position);

			LinearLayout detail = (LinearLayout) inflater.inflate(
					R.layout.event, null);

			c.moveToPosition(position);
			
			//
			// affiche!
			//
			TextView tTitre;
			TextView tDate;
			TextView tHeure;
			WebView web;
			
			tTitre = (TextView) detail.findViewById(R.id.text_titre);
			tDate = (TextView) detail.findViewById(R.id.text_date);
			tHeure = (TextView) detail.findViewById(R.id.text_heure);
			web = (WebView) detail.findViewById(R.id.web_desc);
			
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
							+ "; background-color:" + "#ffffff" + " } p { color:#000000; } a { color:"
							+ "#5050ff" + "; } h2 { color:" + "#440000"
							+ "; } </style><body>"
							/*+ "<h2>epoch start "+ c.getLong(c.getColumnIndex("epoch_debut"))+"</h2>"*/
							+ "<img width=\"50%\" src=\""+c.getString(c.getColumnIndex("image"))+"\">"
							+ "<p>" + c.getString(c.getColumnIndex("description")) + "</p>"
							+ "</body>", "text/html", "utf-8", null);
			
			
			Log.d("detail", c.getString(c.getColumnIndex("description")) );

			((ViewPager) collection).addView(detail, 0);
			return detail;
		}

		/**
		 * Remove a page for the given position. The adapter is responsible for
		 * removing the view from its container, although it only must ensure
		 * this is done by the time it returns from {@link #finishUpdate()}.
		 * 
		 * @param container
		 *            The containing View from which the page will be removed.
		 * @param position
		 *            The page position to be removed.
		 * @param object
		 *            The same object that was returned by
		 *            {@link #instantiateItem(View, int)}.
		 */
		@Override
		public void destroyItem(View collection, int position, Object view) {
			Log.d("detail", "remove view @ " + position);
			((ViewPager) collection).removeView((LinearLayout) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((LinearLayout) object);
		}

		/**
		 * Called when the a change in the shown pages has been completed. At
		 * this point you must ensure that all of the pages have actually been
		 * added or removed from the container as appropriate.
		 * 
		 * @param container
		 *            The containing View which is displaying this adapter's
		 *            page views.
		 */
		@Override
		public void finishUpdate(ViewGroup vg) {
			int pos = ((ViewPager) vg).getCurrentItem();
			Log.d("detail", "finishUpdate says we are at pos " + pos);
		}

		@Override
		public void startUpdate(ViewGroup arg0) {
		}

		//
		// pour les clics sur les items
		//

		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position,
				long id) {
//			Intent in = new Intent(getApplicationContext(),
//					ActivityDebugEvent.class);
			// le view doit avoir un tag qui contient le id...
//			in.putExtra("id", (int) id);
//			startActivity(in);
		}

		//
		// pour constituer un evenement de calendrier a ajouter a l'agenda
		//
		public Intent getCurrentEventInfo(int position) {
			c.moveToPosition(position);

			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, c.getLong(c.getColumnIndex("epoch_debut")));
			intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false); // a faire
//			intent.putExtra(CalendarContract.ExtendedProperties.RRULE, "FREQ=YEARLY"); // a faire
			intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, c.getLong(c.getColumnIndex("epoch_debut"))+2*3600*1000L); // a faire
			intent.putExtra(Events.TITLE, c.getString(c.getColumnIndex("titre")));

			// Make it a recurring Event                    
			//intent.putExtra(Events.RRULE, "FREQ=WEEKLY;COUNT="+Integer.valueOf(No.getText().toString())+";"
			//+"WKST=SU;BYDAY="+days);

			// Making it private and shown as busy
			// intent.putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
			// intent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
			String desc=Html.fromHtml(c.getString(c.getColumnIndex("description"))).toString();
			
//			String desc=
//					"<style type=\"text/css\">body { color:" + "#ffffff"
//							+ "; background-color:" + "#000000" + " } a { color:"
//							+ "#8080ff" + "; } h2 { color:" + "#ffffff"
//							+ "; } </style><body>"
//							+ "<h2>epoch start "+ c.getLong(c.getColumnIndex("epoch_debut"))+"</h2>"
//							+ "<img width=\"50%\" src=\""+c.getString(c.getColumnIndex("image"))+"\">"
//							+ c.getString(c.getColumnIndex("description"))
//							+ "</body>";
			intent.putExtra(Events.DESCRIPTION, desc);

			return intent;
		}
		
		
	}

	
	
	
	
	
	
	

}
