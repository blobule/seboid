package com.seboid.udem;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;



//
// cette activie affiche une page web avec un contenu de la base de donnee
//
// on recoit en parametre un "id"
//

public class ActivityUdeMWeb extends Activity  {

	private ViewPager awesomePager;
	private AwesomePagerAdapter awesomeAdapter;    
	//     private Context cxt;


	//WebView web;

	ImageButton partager;

	DBHelper dbH;
	SQLiteDatabase db;
	Cursor cursor;

	long id; // le id peut etre positif ou negatif. =0 signifie pas de id. On commence au debut.
	String where;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// pour le progress bar rotatif
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.web);

		// le titre en gros
		//TextView titreView=(TextView)findViewById(R.id.webtitre);

		// attend des parametres au demarrage: feed et nice
		Intent sender=getIntent();
		id=sender.getLongExtra("id",0);
		
		String t=sender.getStringExtra("title");
		if( t!=null ) this.setTitle("UdeM|"+t);
		
		where=sender.getStringExtra("where");
		Log.d("rssweb","where:"+where);

		//titreView.setText(nice);

		partager=(ImageButton)findViewById(R.id.detail_partager);
		partager.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int current=ActivityUdeMWeb.this.awesomePager.getCurrentItem();
				ActivityUdeMWeb.this.cursor.moveToPosition(current);
				final String title=cursor.getString(1);
				final String body=makeWebBody(false); // no style please

				Intent intent = new Intent(Intent.ACTION_SEND);
				//				intent.setType("text/plain");
				//				intent.putExtra(Intent.EXTRA_TEXT, "http://www.umontreal.ca");

				intent.setType("text/html");
				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
				intent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(body));
				startActivity(Intent.createChooser(intent, "Email:"));
				//				startActivity(Intent.createChooser(intent, "Share with"));
			}

		});


		//		web=(WebView)findViewById(R.id.web);
		//		web.setScrollContainer(true);
		//		web.setScrollbarFadingEnabled(true);
		//		web.setBackgroundColor(0xff000000 /*0xff333333*/);

		// access a la database
		dbH=new DBHelper(this);
		db=dbH.getReadableDatabase();

	}



	@Override
	protected void onResume() {
		super.onResume();
		Log.d("rss","resume!");

		// get data from database
		cursor=db.query(DBHelper.TABLE,new String[] {"_id","title","link","description","time"},where, null, null, null, DBHelper.C_TIME+" DESC");
		startManagingCursor(cursor);

		cursor.moveToFirst();
		if( id!=0 ) {
			while( !cursor.isAfterLast() && cursor.getLong(0)!=id ) cursor.moveToNext();
		}

		//		Toast.makeText(this,"found id="+id+" at pos="+cursor.getPosition(), Toast.LENGTH_LONG).show();

		awesomeAdapter = new AwesomePagerAdapter();
		awesomePager = (ViewPager) findViewById(R.id.awesomepager);
		awesomePager.setAdapter(awesomeAdapter);
		//		displayWeb();
		awesomePager.setCurrentItem(cursor.getPosition());

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// ferme le database
		db.close();
	}

	// affiche de l'info description web
	// les params[0,1,2] sont title,link,description
	public String makeWebBody(boolean withStyle) {
		final String title=cursor.getString(1);
		final String link=cursor.getString(2);
		final String description=cursor.getString(3);

		String style="<style type=\"text/css\">p { color:#ffffff; } a { color:#8080ff; } h2 { color:#ffffff; }</style>";
		String titre="<h2>"+title+"</h2>";
		String lien="<p style=\"text-align:right;\"><a href=\""+link+"\">La suite...</a>";
		return (withStyle?style:"")+titre+"<p>"+description+"</p>"+lien;
	}

	// affiche de l'info description web
	// les params[0,1,2] sont title,link,description
	public void displayWeb(WebView web) {
		final String title=cursor.getString(1);
		final String link=cursor.getString(2);
		final String description=cursor.getString(3);

		//String meta="<meta name=\"format-detection\" content=\"telephone=yes\" /><meta name=\"format-detection\" content=\"address=no\" />";
		web.getSettings().setJavaScriptEnabled(false);
		web.loadDataWithBaseURL(null,makeWebBody(true), "text/html","utf-8",null);
		//	web.setEnabled(true);

		//Toast.makeText(UdeMRssListActivity.this, htmlData,Toast.LENGTH_LONG).show();
		//web.reload(); // sinon ca ne reeaffiche pas une nouvelle page
	}



	//	  <WebView
	//      android:id="@+id/web"
	//      android:layout_width="fill_parent"
	//      android:layout_height="fill_parent"
	//      android:layout_marginTop="10dp"
	//      android:layout_weight="1" />

	private class AwesomePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return ActivityUdeMWeb.this.cursor.getCount();
		}

		/**
		 * Create the page for the given position.  The adapter is responsible
		 * for adding the view to the container given here, although it only
		 * must ensure this is done by the time it returns from
		 * {@link #finishUpdate()}.
		 *
		 * @param container The containing View in which the page will be shown.
		 * @param position The page position to be instantiated.
		 * @return Returns an Object representing the new page.  This does not
		 * need to be a View, but can be some other container of the page.
		 */
		@Override
		public Object instantiateItem(View collection, int position) {
			WebView web=new WebView(ActivityUdeMWeb.this);
			web.setScrollContainer(true);
			web.setScrollbarFadingEnabled(true);
			web.setBackgroundColor(0xff000000);

			//			TextView tv = new TextView(UdeMWebActivity.this);
			//			tv.setText("Bonjour PAUG " + position);
			//			tv.setTextColor(Color.WHITE);
			//			tv.setTextSize(30);

			ActivityUdeMWeb.this.cursor.moveToPosition(position);
			// c'est toujours le titre du prochain article...
			//UdeMWebActivity.this.setTitle(cursor.getString(1));			

			displayWeb(web);

			((ViewPager) collection).addView(web,0);

			return web;
		}

		/**
		 * Remove a page for the given position.  The adapter is responsible
		 * for removing the view from its container, although it only must ensure
		 * this is done by the time it returns from {@link #finishUpdate()}.
		 *
		 * @param container The containing View from which the page will be removed.
		 * @param position The page position to be removed.
		 * @param object The same object that was returned by
		 * {@link #instantiateItem(View, int)}.
		 */
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((WebView) view);
		}



		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((WebView)object);
		}


		/**
		 * Called when the a change in the shown pages has been completed.  At this
		 * point you must ensure that all of the pages have actually been added or
		 * removed from the container as appropriate.
		 * @param container The containing View which is displaying this adapter's
		 * page views.
		 */
		@Override
		public void finishUpdate(View arg0) {}


		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {}

	}



}