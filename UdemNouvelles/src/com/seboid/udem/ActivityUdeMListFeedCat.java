package com.seboid.udem;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.SimpleCursorTreeAdapter;

public class ActivityUdeMListFeedCat extends Activity  {

	ExpandableListView lv;
	myFeedCatAdapter adapter; // pour afficher les lignes

	DBHelper dbH;
	SQLiteDatabase db;
	Cursor cursor;

	// Mapping pour l'affiche. from contient les id des elements d'une rangees dans le mapping
	// to contient les id des elements d'interface
	static final String[] groupFrom = new String[] { "feed","count(*)"};
//	static final int[] groupTo = new int[] {/*R.id.rowtitle*/ android.R.id.text1,android.R.id.text2};
	static final int[] groupTo = new int[] {R.id.rowcat,R.id.rowcatcount };
	static final String[] childFrom = new String[] { "category","count(*)"};
	static final int[] childTo = new int[] {R.id.rowcat,R.id.rowcatcount };


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rssexpendable);

		lv=(ExpandableListView)findViewById(R.id.expandablelist);		
		lv.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//				cursor.moveToPosition(groupPosition);
//				Intent in = new Intent(UdeMRssFeedCatActivity.this, UdeMRssListActivity.class);
//				in.putExtra("type","feed");
//				in.putExtra("selection",cursor.getString(1)); // colonne 0 = _id...
//				startActivity(in);
//				return true;
				return false;
			}
		});
		lv.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//				cursor.moveToPosition(groupPosition);
//				Intent in = new Intent(UdeMRssFeedCatActivity.this, UdeMRssListActivity.class);
//				in.putExtra("type","category");
//				in.putExtra("selection",cursor.getString(1)); // colonne 0 = _id...
//				startActivity(in);
				return false;
			}
		});


		//		lv.setOnItemClickListener(new OnItemClickListener() {
		//			public void onItemClick(AdapterView parent, View view, int position, long id) {
		//				cursor.moveToPosition(position);
		//				Intent in = new Intent(UdeMRssFeedCatActivity.this, UdeMRssListActivity.class);
		//				in.putExtra("type","feed");
		//				in.putExtra("selection",cursor.getString(1)); // colonne 0 = _id...
		//				startActivity(in);
		//			}
		//		});

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
		//		cursor=db.rawQuery("select _id,feed,category,count(*) from timeline group by feed,category desc",null);
		//		Log.d("sql","col0:"+cursor.getColumnName(0));
		//		Log.d("sql","col1:"+cursor.getColumnName(1));

		startManagingCursor(cursor);

		// adapter

		//		SimpleCursorTreeAdapter(Context context, Cursor cursor,
		//	int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, 
		//	int childLayout, int lastChildLayout, String[] childFrom, int[] childTo)

		//		adapter = new SimpleCursorAdapter(this, R.layout.rowcat, cursor, from, to);
		adapter = new myFeedCatAdapter(this,cursor,
				R.layout.rowfeedgroup,R.layout.rowfeedgroup,
				/*android.R.layout.simple_expandable_list_item_2,android.R.layout.simple_expandable_list_item_1,	*/			
				/*R.layout.row,R.layout.row,*/groupFrom,groupTo,
				R.layout.rowcatgroup,R.layout.rowcatgroup,childFrom, childTo);

		//		adapter.setViewBinder(VIEW_BINDER); // pour auto definir le rendu des champs
		lv.setAdapter(adapter);
	}

	//	static final ViewBinder VIEW_BINDER = new ViewBinder() {
	//		public boolean setViewValue(View view, Cursor c, int index) {
	//			if( view.getId()!=R.id.rowdate) return false; // auto-render
	//			long timestamp=c.getLong(index)*1000; // sec -> millisec
	//			Log.d("time","time "+timestamp);
	//			CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
	//			((TextView)view).setText(relTime);
	//			return true;
	//		}
	//	};


	private class myFeedCatAdapter extends SimpleCursorTreeAdapter {

		public myFeedCatAdapter(Context context, Cursor cursor,
				int collapsedGroupLayout, int expandedGroupLayout,
				String[] groupFrom, int[] groupTo, int childLayout,
				int lastChildLayout, String[] childFrom, int[] childTo) {
			super(context, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom,
					groupTo, childLayout, lastChildLayout, childFrom, childTo);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor group) {
			String feed=group.getString(1); // feed name
			return db.rawQuery("select _id,category,count(*) from timeline where feed='"+feed+"' group by category", null);
		}

	}



}