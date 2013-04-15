package com.seboid.udemcalendrier;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

//
// super swiper par jour...
//

public class ActivityDebugEventsSwipe extends FragmentActivity {

	private ViewPager swipePager;
	private SwipeAdapter swipeAdapter;

	ImageCache imageCache; // link url et image
	ImageLoaderQueue imageQ;

	LayoutInflater inflater;

	// ListView listv;
	// SimpleCursorAdapter adapter;

	// arrive directement de Extra dans l'intent
	final String[] fromRef = { "_id", "titre", "date", "vignette" };
	final int[] toRef = { R.id.text3, R.id.text1, R.id.text2, R.id.vignette };
	final String queryRef = "select _id,titre,date,vignette from "
			+ DBHelper.TABLE_E + " order by date asc";
	final String titleRef = "Events";

	String[] from;
	int[] to;
	String query;
	int layout; // d'une rangee
	String title;
	int type; // 0=events, 1=categories

	final int LOADER_ID = 0;

	// database
	DBHelper dbh;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Intent in = getIntent();

		from = in.getStringArrayExtra("from");
		if (from == null)
			from = fromRef;

		to = in.getIntArrayExtra("to");
		if (to == null)
			to = toRef;

		query = in.getStringExtra("query");
		if (query == null)
			query = queryRef;

		layout = in.getIntExtra("layout", R.layout.events_row);

		title = in.getStringExtra("title");
		if (title == null)
			title = titleRef;

		type = in.getIntExtra("type", 0); // 0 is events, 1 is categories

		setTitle(title);

		setContentView(R.layout.events_swipe);

		swipeAdapter = new SwipeAdapter();
		swipePager = (ViewPager) findViewById(R.id.awesomepager);
		swipePager.setAdapter(swipeAdapter);
		// swipePager.setCurrentItem(0);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//
		// affiche!
		//

		dbh = new DBHelper(this);
		db = dbh.getReadableDatabase();

		// la cache pour les images
		imageCache = new ImageCache();
		imageQ = new ImageLoaderQueue();
		imageQ.start();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
		// new DownloadEventsTask().execute();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	// @Override
	// public void onItemClick(AdapterView<?> arg0, View v, int position, long
	// id) {
	// Toast.makeText(ActivityDebugEventsSwipe.this, "item "+position+":"+id,
	// Toast.LENGTH_LONG).show();
	// if( type==0 ) {
	// Intent in=new
	// Intent(ActivityDebugEventsSwipe.this,ActivityDebugEvent.class);
	// // le view doit avoir un tag qui contient le id...
	// in.putExtra("id",(int)id);
	// startActivity(in);
	// }else if( type==1 ) {
	// // categories -> events avec un like
	// Intent in=new
	// Intent(ActivityDebugEventsSwipe.this,ActivityDebugEventsSwipe.class);
	// // le view doit avoir un tag qui contient le id...
	// in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_categories like '%:"+id+":%' order by date asc");
	// startActivity(in);
	// }else if( type==2 ) {
	// // souscategories -> events avec un like
	// Intent in=new
	// Intent(ActivityDebugEventsSwipe.this,ActivityDebugEventsSwipe.class);
	// // le view doit avoir un tag qui contient le id...
	// in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_souscategories like '%:"+id+":%' order by date asc");
	// startActivity(in);
	// }else if( type==3 ) {
	// // groupes -> events avec un like
	// Intent in=new
	// Intent(ActivityDebugEventsSwipe.this,ActivityDebugEventsSwipe.class);
	// // le view doit avoir un tag qui contient le id...
	// in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_groupes like '%:"+id+":%' order by date asc");
	// startActivity(in);
	// }else if( type==4 || type==5 ) {
	// // lieux -> events avec ce lieu
	// Intent in=new
	// Intent(ActivityDebugEventsSwipe.this,ActivityDebugEventsSwipe.class);
	// // le view doit avoir un tag qui contient le id...
	// in.putExtra("query","select _id,titre,date from "+DBHelper.TABLE_E+" where ids_lieux like '%:"+id+":%' order by date asc");
	// startActivity(in);
	// // }else if( type==5 ) {
	// // Cursor c=adapter.getCursor();
	// // c.moveToPosition(position);
	// // double lat=c.getDouble(2);
	// // double lng=c.getDouble(3);
	// // //Uri uri =
	// Uri.parse("geo:0,0?q=22.99948365856307,72.60040283203125 (Maninagar)");
	// // // Uri uri =
	// Uri.parse("geo:0,0?q="+lat+","+lng);//+" ("+c.getString(1)+")");
	// // Uri uri = Uri.parse("geo:"+lat+","+lng);//+" ("+c.getString(1)+")");
	// //
	// // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	// // startActivity(intent);
	// }
	// }

	//
	// web asynchrone
	//

	//
	// Cursor async loader
	//

	public static class myASyncLoader extends AsyncTaskLoader<Cursor> {
		final ForceLoadContentObserver mObserver; // en cas de forceload

		Cursor c; // le data qui est retourne

		DBHelper dbh;
		SQLiteDatabase db;
		String query_where;
		String[] query_from;

		public myASyncLoader(Context context, String[] query_from,
				String query_where, SQLiteDatabase db) {
			super(context);
			mObserver = new ForceLoadContentObserver();
			this.db = db;
			this.query_where = query_where;
			this.query_from = query_from;
			c = null;
		}

		@Override
		public void deliverResult(Cursor data) {
			Log.d("asyncloader", "deliver results");

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
			// Cursor c=db.rawQuery(query, null);
			// String[] query_columns = { "_id", "titre", "date", "vignette" };
			Cursor c = db.query(DBHelper.TABLE_E, query_from, query_where,
					null, null, null, "date asc");
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
	private class SwipeAdapter extends PagerAdapter implements
			LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

		// un listevie et un adapter par item... null -> pas encore vu...
		// on doit
		ListView[] listv;
		SimpleCursorAdapter[] adapter;

		public SwipeAdapter() {
			super();
			listv = new ListView[getCount()];
			adapter = new SimpleCursorAdapter[getCount()];
		}

		@Override
		public int getCount() {
			return 7;
		}

		/**
		 * Create the page for the given position. The adapter is responsible
		 * for adding the view to the container given here, although it only
		 * must ensure this is done by the time it returns from
		 * {@link #finishUpdate()}.
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
					R.layout.events, null);

			// Log.d("detail", "inflate "+position+" ok");

			TextView jour = (TextView) detail.findViewById(R.id.jour);
			jour.setText(TempsUtil.aujourdhuiNom(position) + " ("
					+ TempsUtil.aujourdhui(position) + ")");

			listv[position] = (ListView) detail
					.findViewById(R.id.listViewEvents);
			listv[position].setOnItemClickListener(this);

			// pas de cursor pour cet adapter car il viendra du loader...
			adapter[position] = new SimpleCursorAdapter(
					ActivityDebugEventsSwipe.this, layout, null, from, to, 0);
			listv[position].setAdapter(adapter[position]);

			// notre swipeadapter va fournir les callbacks de ce loader.
			// le loader id est la position
			getSupportLoaderManager()
					.initLoader(
							position, // le id est la position dans la liste des
										// jours
							null,
							(android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>) this);

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
		// callback de loader... pour chaque page on va avoir un loader
		// possiblement
		//

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle b) {
			long time_start = TempsUtil.aujourdhuiMilli() + loaderId * 24
					* 3600 * 1000; // loaderid est le nb de jour a partir
									// d'aujourd'hui
			long time_end = time_start + 24 * 3600 * 1000 - 1000; // loaderid
																	// est le nb
																	// de jour a
																	// partir
																	// d'aujourd'hui
			String where = DBHelper.C_EPOCH_DEBUT + ">" + time_start + " and "
					+ DBHelper.C_EPOCH_DEBUT + "<" + time_end;
			return new myASyncLoader(ActivityDebugEventsSwipe.this, from,
					where, db);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
			// les deux infos fraichement loadees
			int pos = loader.getId();
			adapter[pos].swapCursor(c);
			// on veut controler l'affichage des rangee...
			adapter[pos].setViewBinder(new ViewBinder() {
				@Override
				public boolean setViewValue(View v, Cursor c, int colonne) {
					// Log.d("binder",
					// "col "+colonne+":"+c.getColumnIndex("vignette"));
					if (colonne == c.getColumnIndex("vignette")) {
						String url = c.getString(colonne);
						Log.d("binder", "vignette " + url);
						ImageView iv = (ImageView) v;
						Bitmap b = imageCache.getBitmap(url);
						if (b != null)
							iv.setImageBitmap(b);
						else {
							// ajoute directement dans la queue de "a lire"
							iv.setTag(url); // associe cet url avec cet image
											// (peut changer si recyclage)
							imageQ.addTask(iv);
							// iv.setImageResource(R.drawable.ic_launcher); //
							// temporaire
							// // while
							// // loading
							// iv.setTag(url); // on va pouvoir detecter le
							// // recyclage
							// new ImageLoadTask(iv).execute();
						}
						return true;
					}
					return false;
				}
			});
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
			int pos = loader.getId();
			adapter[pos].swapCursor(null);
		}

		//
		// pour les clics sur les items
		//

		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position,
				long id) {
			Intent in = new Intent(getApplicationContext(),
					ActivityDebugEvent.class);
			// le view doit avoir un tag qui contient le id...
			in.putExtra("id", (int) id);
			startActivity(in);
		}

	}

	//
	// cache pour les icones
	//
	public class ImageCache {
		private HashMap<String, Bitmap> hm;

		public ImageCache() {
			hm = new HashMap<String, Bitmap>();
		}

		// return null si pas d'image en cache
		public Bitmap getBitmap(String url) {
			return hm.get(url);
		}

		public void rememberBitMap(String url, Bitmap b) {
			hm.put(url, b);
			Log.d("cache", "nb images = " + hm.size());
		}
	}

	//
	// loader asynchrone d'images...
	//
	// on suppose que le tag du ImageView est l'URL a lire.
	// si le view est recycle, ce n'est pas grave... on laisse tomber.
	//
	//
	//
	private class ImageLoadTask extends AsyncTask<String, String, Bitmap> {
		private final ImageView iv;
		private final String url;

		public ImageLoadTask(ImageView iv) {
			this.iv = iv;
			url = (String) iv.getTag();
		}

		@Override
		protected void onPreExecute() {
			if (url != null) {
				Log.d("asyncimage", "loading " + url);
			}
		}

		@Override
		protected Bitmap doInBackground(String... param) {
			if (url == null)
				return null;
			try {
				final Bitmap b = loadHttpImage(url);
				// iv.post(new Runnable() {
				// public void run() { iv.setImageBitmap(b); };
				// });
				return b;
			} catch (ClientProtocolException e) {
			} catch (IOException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			Log.d("asyncimage", "done loading " + url);
			if (result == null)
				return;
			imageCache.rememberBitMap(url, result); // ne pas reloader 2 fois la
													// meme image
			String newurl = (String) iv.getTag();
			if (newurl.equals(url)) {
				iv.setImageBitmap(result);
			} else {
				Log.d("asyncimage", "recycled");
			}
		}

		//
		// lire une page web et retourner le contenu
		//
		private HttpEntity getHttp(String url) throws ClientProtocolException,
				IOException {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet http = new HttpGet(url);
			HttpResponse response = httpClient.execute(http);
			return response.getEntity();
		}

		//
		// lire une image avec un URL
		//
		private Bitmap loadHttpImage(String url)
				throws ClientProtocolException, IOException {
			InputStream is = getHttp(url).getContent();
			Bitmap b = BitmapFactory.decodeStream(is);
			// Drawable d = Drawable.createFromStream(is, "src");
			return b;
		}

	}

	class ImageLoaderQueue {
		private LinkedList<ImageView> tasks;
		private Thread thread;
		private boolean running;
		private Runnable internalRunnable;

		// cache d'images
		private HashMap<String, Bitmap> cache;

		private final String ME = "TaskQueue";

		private class InternalRunnable implements Runnable {
			public void run() {
				internalRun();
			}
		}

		public ImageLoaderQueue() {
			tasks = new LinkedList<ImageView>();
			internalRunnable = new InternalRunnable();
			cache = new HashMap<String, Bitmap>();
		}

		public void start() {
			if (!running) {
				thread = new Thread(internalRunnable);
				thread.setDaemon(true);
				running = true;
				thread.start();
			}
		}

		public void stop() {
			running = false;
		}

		// called from UI
		public void addTask(ImageView iv) {
			if (iv == null)
				return;
			// check cache
			String url = (String) iv.getTag();
			if (url == null)
				return;
			if (cache.containsKey(url)) {
				iv.setImageBitmap(cache.get(url));
				return;
			}
			// must load the image...
			if (!running)
				start();
			synchronized (tasks) {
				tasks.addLast(iv);
				tasks.notify(); // notify any waiting threads
			}
		}

		private ImageView getNextTask() {
			int s;
			synchronized (tasks) {
				s = tasks.size();
			}
			Log.d(ME, "getNextTask " + s + " todo");
			synchronized (tasks) {
				if (tasks.isEmpty()) {
					try {
						tasks.wait();
					} catch (InterruptedException e) {
						Log.e(ME, "Task interrupted", e);
						stop();
					}
				}
				return tasks.removeFirst();
			}
		}

		// in thread
		private void internalRun() {
			while (running) {
				final ImageView iv = getNextTask();
				final String url = (String) iv.getTag();
				if (url == null)
					continue;
				// check cache again

				final Bitmap tmp;
				synchronized(cache) { tmp=cache.get(url); }
				if( tmp!=null ) {
					Log.d(ME, "in cache " + url);
					iv.post(new Runnable() {
						public void run() {
							if (((String) iv.getTag()).equals(url))
								iv.setImageBitmap(tmp);
						};
					});
					continue;
				}
				Log.d(ME, "loading  " + url);
				try {
					final Bitmap b = loadHttpImage(url);
					// update UI thread... only if tag did not change
					iv.post(new Runnable() {
						public void run() {
							if (((String) iv.getTag()).equals(url))
								iv.setImageBitmap(b);
						};
					});
					// update cache
					synchronized(cache) {
						cache.put(url, b);
					}
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			}
		}

		//
		// lire une page web et retourner le contenu
		//
		private HttpEntity getHttp(String url) throws ClientProtocolException,
				IOException {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet http = new HttpGet(url);
			HttpResponse response = httpClient.execute(http);
			return response.getEntity();
		}

		//
		// lire une image avec un URL
		//
		private Bitmap loadHttpImage(String url)
				throws ClientProtocolException, IOException {
			InputStream is = getHttp(url).getContent();
			Bitmap b = BitmapFactory.decodeStream(is);
			// Drawable d = Drawable.createFromStream(is, "src");
			return b;
		}

	}

}
