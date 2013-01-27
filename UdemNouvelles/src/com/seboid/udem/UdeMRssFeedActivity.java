package com.seboid.udem;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class UdeMRssFeedActivity extends Activity  {

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

	// Mapping pour l'affiche. from contient les id des elements d'une rangees dans le mapping
	// to contient les id des elements d'interface
	static final String[] from = new String[] { "feed","count(*)"};
	static final int[] to = new int[] {R.id.rowcat,R.id.rowdate };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss);

		
		lv=(ListView)findViewById(R.id.list);		
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				cursor.moveToPosition(position);
				//				Intent in = new Intent(UdeMRssFeedActivity.this, UdeMRssListActivity.class);
				//				in.putExtra("type","feed");
				//				in.putExtra("selection",cursor.getString(1)); // colonne 0 = _id...
				//				startActivity(in);
				Intent in = new Intent(UdeMRssFeedActivity.this, UdeMWebActivity.class);
				in.putExtra("where","feed = '"+cursor.getString(1)+"'");
				in.putExtra("title",feedName.get(cursor.getString(1)));
				startActivity(in);
			}
		});

		// access a la database
		dbH=new DBHelper(this);
		db=dbH.getReadableDatabase();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();		// ferme le database
	}


	@Override
	protected void onResume() {
		super.onResume();

		cursor=db.rawQuery("select _id,feed,count(*) from timeline group by feed", null);
		//		Log.d("sql","col0:"+cursor.getColumnName(0));
		//		Log.d("sql","col1:"+cursor.getColumnName(1));

		startManagingCursor(cursor);

		// adapter
		adapter = new SimpleCursorAdapter(this, R.layout.rowcat, cursor, from, to);
		adapter.setViewBinder(VIEW_BINDER); // pour arranger en direct le nom des feeds
		lv.setAdapter(adapter);
	}

	//
	// arrange l'affiche des feeds...
	//
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		public boolean setViewValue(View view, Cursor c, int index) {
			if( view.getId()!=R.id.rowcat) return false; // auto-render
			((TextView)view).setText(UdeMRssFeedActivity.feedName.get(c.getString(1)));
			return true;
		}
	};

	
	
	//		static final ViewBinder VIEW_BINDER = new ViewBinder() {
	//			public boolean setViewValue(View view, Cursor c, int index) {
	//				if( view.getId()!=R.id.rowdate) return false; // auto-render
	//				long timestamp=c.getLong(index)*1000; // sec -> millisec
	//				Log.d("time","time "+timestamp);
	//				CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
	//				((TextView)view).setText(relTime);
	//				return true;
	//			}
	//		};


}