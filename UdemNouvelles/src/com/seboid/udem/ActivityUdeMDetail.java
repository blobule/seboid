package com.seboid.udem;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



//
// cette activie affiche une page d'info avec un contenu de la base de donnee
//
// on recoit en parametre un "id"
//
// plutot que de creer un webview, on va afficher une image et un textview scrollable
//

public class ActivityUdeMDetail extends Activity  {

	private ViewPager awesomePager;
	private AwesomePagerAdapter awesomeAdapter;    

	ImageButton partager;

	DBHelper dbH;
	SQLiteDatabase db;
	Cursor cursor;

	// pour gerer la lecture (ce qui est lu ou pas lu)
	Lecture lecture[];

	// cache pour les images
	// ["link"] -> Drawable
	HashMap<String,Drawable> imageCache;

	long id; // <0 ou >0 pour le id. ==0 -> pas de id specifie.
	String where;

	LayoutInflater inflater;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// pour le progress bar rotatif
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.detail);

		// le titre en gros
		//TextView titreView=(TextView)findViewById(R.id.webtitre);

		// attend des parametres au demarrage: id , where, and title
		Intent sender=getIntent();
		id=sender.getLongExtra("id",0);
		Log.d("detail","looking for id="+id);

		String t=sender.getStringExtra("title");
		if( t!=null ) {
			this.setTitle("UdeM | "+t);
			Log.d("detail","title is "+t);
		}
		this.setTitleColor(0xff0047b6);


		where=sender.getStringExtra("where");
		Log.d("rssweb","where:"+where);

		inflater = (LayoutInflater)   getSystemService(Context.LAYOUT_INFLATER_SERVICE); 

		// initialise la cache d'images
		imageCache=new HashMap<String,Drawable>();

		//titreView.setText(nice);

		partager=(ImageButton)findViewById(R.id.detail_partager);
//		partager.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				int current=ActivityUdeMDetail.this.awesomePager.getCurrentItem();
//				ActivityUdeMDetail.this.cursor.moveToPosition(current);
//				final String title=cursor.getString(1);
//				final String body=makeWebBody(false); // no style please
//
//				Intent intent = new Intent(Intent.ACTION_SEND);
//				//				intent.setType("text/plain");
//				//				intent.putExtra(Intent.EXTRA_TEXT, "http://www.umontreal.ca");
//
//				intent.setType("text/html");
//				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
//				intent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(body));
//				startActivity(Intent.createChooser(intent, "Email:"));
//				//				startActivity(Intent.createChooser(intent, "Share with"));
//			}
//
//		});


		//		web=(WebView)findViewById(R.id.web);
		//		web.setScrollContainer(true);
		//		web.setScrollbarFadingEnabled(true);
		//		web.setBackgroundColor(0xff000000 /*0xff333333*/);

		// access a la database
		dbH=new DBHelper(this);


	}



	@Override
	protected void onResume() {
		super.onResume();
		Log.d("rss","resume!");

		db=dbH.getReadableDatabase();

		// get data from database
		cursor=db.query(DBHelper.TABLE,new String[] {
				DBHelper.C_ID,
				DBHelper.C_TITLE,
				DBHelper.C_LINK,
				DBHelper.C_DESC,
				DBHelper.C_TIME,
				DBHelper.C_LONGDESC,
				DBHelper.C_IMAGE,
				DBHelper.C_LU
		},where, null, null, null, DBHelper.C_TIME+" DESC");
		startManagingCursor(cursor);

		// on va avoir besoin des ID de toutes les rangeees... pour gerer le LU
		lecture = new Lecture[cursor.getCount()];
		int i;
		for(i=0;i<cursor.getCount();i++) {
			cursor.moveToPosition(i);
			lecture[i]=new Lecture(cursor.getLong(0),cursor.getInt(7)==1);
		}

		// on trouve le bon point de depart dans la liste...
		cursor.moveToFirst();
		if( id!=0 ) {
			while( !cursor.isAfterLast() && cursor.getLong(0)!=id ) cursor.moveToNext();
		}

		//Toast.makeText(this,"found id="+id+" at pos="+cursor.getPosition(), Toast.LENGTH_LONG).show();

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

	//	// affiche de l'info description web
	//	// les params[0,1,2] sont title,link,description
	//	public String makeWebBody(boolean withStyle) {
	//		final String title=cursor.getString(1);
	//		final String link=cursor.getString(2);
	//		final String description=cursor.getString(3);
	//
	//		String style="<style type=\"text/css\">p { color:#ffffff; } a { color:#8080ff; } h2 { color:#ffffff; }</style>";
	//		String titre="<h2>"+title+"</h2>";
	//		String lien="<p style=\"text-align:right;\"><a href=\""+link+"\">La suite...</a>";
	//		return (withStyle?style:"")+titre+"<p>"+description+"</p>"+lien;
	//	}
	//
	//	// affiche de l'info description web
	//	// les params[0,1,2] sont title,link,description
	//	public void displayWeb(WebView web) {
	//		final String title=cursor.getString(1);
	//		final String link=cursor.getString(2);
	//		final String description=cursor.getString(3);
	//
	//		//String meta="<meta name=\"format-detection\" content=\"telephone=yes\" /><meta name=\"format-detection\" content=\"address=no\" />";
	//		web.getSettings().setJavaScriptEnabled(false);
	//		web.loadDataWithBaseURL(null,makeWebBody(true), "text/html","utf-8",null);
	//		//	web.setEnabled(true);
	//
	//		//Toast.makeText(UdeMRssListActivity.this, htmlData,Toast.LENGTH_LONG).show();
	//		//web.reload(); // sinon ca ne reeaffiche pas une nouvelle page
	//	}



	//	  <WebView
	//      android:id="@+id/web"
	//      android:layout_width="fill_parent"
	//      android:layout_height="fill_parent"
	//      android:layout_marginTop="10dp"
	//      android:layout_weight="1" />

	private class AwesomePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return ActivityUdeMDetail.this.cursor.getCount();
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
			Log.d("detail", "instantiate view @ "+position);

			LinearLayout detail = (LinearLayout)inflater.inflate(R.layout.detail_page,null);

			//Log.d("detail", "inflate "+position+" ok");

			TextView titre=(TextView)detail.findViewById(R.id.detail_titre);
			ImageView image=(ImageView)detail.findViewById(R.id.detail_image);
			ProgressBar loading=(ProgressBar)detail.findViewById(R.id.detail_loading);
			WebView web=(WebView)detail.findViewById(R.id.detail_web);

			web.setScrollContainer(true);
			web.setScrollbarFadingEnabled(false);
			web.setBackgroundColor(0xff000000);

			//			TextView tv = new TextView(UdeMWebActivity.this);
			//			tv.setText("Bonjour PAUG " + position);
			//			tv.setTextColor(Color.WHITE);
			//			tv.setTextSize(30);

			//			TextView debug=(TextView)detail.findViewById(R.id.detail_addr);
			//			debug.setText("("+image+")");

			ActivityUdeMDetail.this.cursor.moveToPosition(position);

			String imgURL=cursor.getString(6);

			Log.d("rss","image is >"+imgURL+"<");

			if( imgURL.equals("") ) {
				// pas d'image!!!
				image.setVisibility(View.GONE);
				loading.setVisibility(View.GONE);
			}else{
				// une image!
				if( imageCache.containsKey(imgURL) ) {
					image.setImageDrawable(imageCache.get(imgURL));
					image.setVisibility(View.VISIBLE);
					loading.setVisibility(View.GONE);
				}else{
					// load l'image en background.
					new DownloadImageTask(image,loading).execute(cursor.getString(6));
				}
			}

			//
			// on suppose que l'etat de lecture ne changera pas de l'exterieur
			//

			titre.setText(cursor.getString(1));
			//image.setText(cursor.getString(6));
			//contenu.setText(cursor.getString(5));
			//contenu.setMovementMethod(new ScrollingMovementMethod());			

			web.getSettings().setJavaScriptEnabled(false);
			web.loadDataWithBaseURL(null,"<style type=\"text/css\">body { color:#ffffff; background-color:#000000 } a { color:#8080ff; } h2 { color:#ffffff; } </style><body>"
					+cursor.getString(5)+"</body>", "text/html","utf-8",null);


			// c'est toujours le titre du prochain article...
			//UdeMWebActivity.this.setTitle(cursor.getString(1));			

			detail.setTag(cursor.getString(1));

			((ViewPager) collection).addView(detail,0);

			return detail;
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
			Log.d("detail","remove view @ "+position);
			((ViewPager) collection).removeView((LinearLayout) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((LinearLayout)object);
		}


		/**
		 * Called when the a change in the shown pages has been completed.  At this
		 * point you must ensure that all of the pages have actually been added or
		 * removed from the container as appropriate.
		 * @param container The containing View which is displaying this adapter's
		 * page views.
		 */
		@Override
		public void finishUpdate(ViewGroup vg) {
			int pos=((ViewPager)vg).getCurrentItem();
			Log.d("detail","finishUpdate says we are at pos "+pos);
			//
			// mark the current item as "LU=1"
			//
			// ajouter LU a la base de donnee
			if( !lecture[pos].lu ) {
				lecture[pos].lu=true;
				ContentValues val = new ContentValues();
				val.clear();
				val.put(DBHelper.C_LU, true);
				try {
					db.update(DBHelper.TABLE, val, DBHelper.C_ID+"="+lecture[pos].id , null);
				} catch ( SQLException e ) {
					Log.d("detail","probleme de update dans la DB :-(");
				}
			}
		}


		//		@Override
		//		public void restoreState(Parcelable arg0, ClassLoader arg1) {}
		//
		//		@Override
		//		public Parcelable saveState() {
		//			return null;
		//		}

		@Override
		public void startUpdate(ViewGroup arg0) {}

	}

	// tout ce qui suit devrait etre dans un asynctask!!!!!!

	//
	// lire une page web et retourner le contenu
	//
	private HttpEntity getHttp(String url) throws ClientProtocolException, IOException
	{
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet http = new HttpGet(url);
		HttpResponse response = httpClient.execute(http);
		return response.getEntity();
	}


	//
	// lire une image avec un URL
	//
	private Drawable loadHttpImage(String url) throws ClientProtocolException, IOException {
		InputStream is = getHttp(url).getContent();
		Drawable d = Drawable.createFromStream(is, "src");
		return d;
	}


	///
	/// un asynctask pour lire une image...
	/// ca pourrait planter si on detruit l'interface View avant que le load soit fini...
	///
	/// <in,progress,out>

	private class DownloadImageTask extends AsyncTask<String, Integer, Drawable> {
		ImageView image;
		ProgressBar loading;
		DownloadImageTask(ImageView image,ProgressBar loading) {
			this.image=image;
			this.loading=loading;
		}
		protected void onPreExecute() {
			image.setVisibility(View.INVISIBLE); // pas GONE, comme ca on garde l'espace...
			loading.setVisibility(View.VISIBLE);
		}
		protected Drawable doInBackground(String... urls) {        	
			try {
				Drawable img=loadHttpImage(urls[0]);
				// ajuste la cache d'images
				// est-ce que c'est le bon endroit pour faire ca??????
				// est-ce qu' on a besoin d'un lock sur le hashmap???
				ActivityUdeMDetail.this.imageCache.put(urls[0],img);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
				return img;
			} catch (ClientProtocolException e) {
			} catch (IOException e) {
			} 
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Drawable result) {
			if( result!=null ) {
				image.setImageDrawable(result);
				image.setVisibility(View.VISIBLE);
				loading.setVisibility(View.GONE);

				//Animation fadeInAnimation = AnimationUtils.loadAnimation(ActivityUdeMDetail.this, R.anim.fadein);
				//image.startAnimation(fadeInAnimation);



			}
		}
	}

	// pour gerer la lecture LU/pas LU
	class Lecture {
		public long id;
		public boolean lu;
		Lecture(long id,boolean lu) { this.id=id;this.lu=lu; }
	}


}