package com.seboid.udem;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

// list feed et categories ensembles...




public class ActivityUdeMListFC extends Activity  {

	public static final HashMap<String,String> feedName;
	static {
		feedName = new HashMap<String,String>();
		feedName.put("recherche","La Recherche");
		feedName.put("enseignement","Enseignement");
		feedName.put("campus","Campus");
		feedName.put("international","International");
		feedName.put("culture","Culture");
		feedName.put("sports","Sports");
		feedName.put("multimedia","Multimédia");
		feedName.put("revue-de-presse","Revue de presse");
	}


	ListView lv;
	SimpleCursorAdapter adapter; // pour afficher les lignes

	DBHelper dbH;
	SQLiteDatabase db;
	Cursor cursor;

	// une petite hasmap contenant le nombre d'item dans chaque feed...
	HashMap<String,Integer> feedCount;

	// Mapping pour l'affiche. from contient les id des elements d'une rangees dans le mapping
	// to contient les id des elements d'interface
	static final String[] from = new String[] { "feed","category","count(*)"};
	static final int[] to = new int[] {R.id.feedinfo,R.id.rowcat,R.id.rowcatcount };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss);

		lv=(ListView)findViewById(R.id.list);	
		/***
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				cursor.moveToPosition(position);
				//				Intent in = new Intent(UdeMRssCatActivity.this, UdeMRssListActivity.class);
				//				in.putExtra("type","category");
				//				in.putExtra("selection",cursor.getString(1)); // colonne 0 = _id...
				//				startActivity(in);

				Intent in = new Intent(ActivityUdeMListFC.this, ActivityUdeMWeb.class);
				in.putExtra("where","category = '"+cursor.getString(1)+"'");
				in.putExtra("title",cursor.getString(1));
				startActivity(in);
			}
		});
		 ***/

		// access a la database
		dbH=new DBHelper(this);
		db=dbH.getReadableDatabase();
	}

	public void actionFeed(View v) {
		String tag=(String)v.getTag();
		Toast.makeText(this, "actionFeed! tage is "+tag, Toast.LENGTH_LONG).show();
		Intent in = new Intent(this, ActivityUdeMNouvelles.class);
		in.putExtra("type","feed");
		in.putExtra("selection",tag);
		startActivity(in);
	}

	public void actionCat(View v) {
		String tag=(String)v.getTag();
		Toast.makeText(this, "actionCat! tag is "+tag, Toast.LENGTH_LONG).show();
		Intent in = new Intent(this, ActivityUdeMNouvelles.class);
		in.putExtra("type","category");
		in.putExtra("selection",tag);
		startActivity(in);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();		// ferme le database
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.d("rss","resume!");

		//
		// on compte le nombre d'articles pour chaque feed
		//
		feedCount=new HashMap<String,Integer>();
		Cursor c=db.rawQuery("select _id,feed,count(*) from timeline group by feed", null);

		c.moveToFirst();
		while( !c.isAfterLast() ) {
			//Log.d("cursor","feed "+c.getString(1)+" = "+c.getInt(2));
			feedCount.put(c.getString(1),c.getInt(2));
			c.moveToNext();
		}
		c.close();

		cursor=db.rawQuery("select _id,feed,category,count(*) from timeline group by category order by feed asc, category asc", null);

		startManagingCursor(cursor);

		// adapter
		adapter = new MySimpleCursorAdapter(this, R.layout.rowcat, cursor, from, to);
		//adapter.setViewBinder(VIEW_BINDER); // pour auto definir le rendu des champs
		lv.setAdapter(adapter);
	}

	//	ViewBinder VIEW_BINDER = new ViewBinder() {
	//		public boolean setViewValue(View view, Cursor c, int index) {
	//			if( view.getId()==R.id.feedinfo) {
	//				TextView tv=(TextView)view.findViewById(R.id.rowfeed);
	//				TextView tvc=(TextView)view.findViewById(R.id.rowfeedcount);
	//				String feed=c.getString(index);
	//			
	//				if( tv!=null ) tv.setText(feed);
	//				if( tvc!=null ) tvc.setText(ActivityUdeMListFC.this.feedCount.get(feed));
	//				return true;
	//			}
	//			return false;
	//		}
	//	};

	//
	// On va se definir notre propre adapter... ca va simplfier la chose...
	//
	class MySimpleCursorAdapter extends SimpleCursorAdapter {
		Context context;
		int layout;
		LayoutInflater inflater;

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			this.context=context;
			this.layout=layout;
			inflater = (LayoutInflater)   getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}



		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//super.bindView(view, context, cursor);

			TextView vf=(TextView)view.findViewById(R.id.rowfeed);
			TextView vfc=(TextView)view.findViewById(R.id.rowfeedcount);
			TextView vc=(TextView)view.findViewById(R.id.rowcat);
			TextView vcc=(TextView)view.findViewById(R.id.rowcatcount);

			Log.d("cursor","position="+cursor.getPosition());

			// 1 = feed, 2=category, 3=count
			String feed=cursor.getString(1);
			String cat=cursor.getString(2);
			String nbCat=cursor.getString(3);


			vf.setText(ActivityUdeMListFC.this.feedName.get(feed)/*+"<"+cursor.getPosition()+">"*/);
			vfc.setText(""+ActivityUdeMListFC.this.feedCount.get(feed));

			vf.setVisibility(View.VISIBLE);
			vfc.setVisibility(View.VISIBLE);

			// verifier si le precedent item a le meme feed
			if( cursor.getPosition()>0 ) {
				cursor.moveToPrevious();
				if( cursor.getString(1).equals(feed) ) {
					// on est pareil comme le precedent 
					vf.setVisibility(View.GONE);
					vfc.setVisibility(View.GONE);
				}
				cursor.moveToNext();
			}

			vc.setText(cat);
			vcc.setText(nbCat);
			
			// ajuste les tags au cas ou on clic
			view.findViewById(R.id.feedinfo).setTag(feed);
			view.findViewById(R.id.catinfo).setTag(cat);
		}
	}
}




