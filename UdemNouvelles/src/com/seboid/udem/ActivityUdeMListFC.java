package com.seboid.udem;

import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

// list feed et categories ensembles...

public class ActivityUdeMListFC extends Activity  {


	SharedPreferences preferences;
	SharedPreferences.Editor prefeditor;
	
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

		// marche pas...
		preferences=PreferenceManager.getDefaultSharedPreferences(this);
		prefeditor=preferences.edit();
		//preferences=getPreferences(MODE_PRIVATE);
		
		lv=(ListView)findViewById(R.id.list);

		// access a la database
		dbH=new DBHelper(this);
		db=dbH.getReadableDatabase();
	}

	public void actionCheck(View v) {
		String feed=(String)v.getTag();
		Boolean use=((CheckBox)v).isChecked();
		Toast.makeText(this, "actionCheck! feed is "+feed+" val="+use, Toast.LENGTH_LONG).show();
		// pour l'instant on garde ca simple... on doit faire "reload" pour voir le resultat.
		// c'est le mem editor qui doit faire le put et le apply... donc pas de pref.edit().put
		prefeditor.putBoolean(feed, use);
		prefeditor.apply();
		
//		 SharedPreferences settings = getSharedPreferences(GAME_PREFERENCES, MODE_PRIVATE);
//	        SharedPreferences.Editor prefEditor = settings.edit();
//	        prefeditor.putString("UserName", "John Doe");
//	        prefEditor.putInt("UserAge", 22);
//	        prefEditor.commit();
	}
	
	public void actionFeed(View v) {
		String tag=(String)v.getTag();
		//Toast.makeText(this, "actionFeed! tage is "+tag, Toast.LENGTH_LONG).show();
		Intent in = new Intent(this, ActivityUdeMNouvelles.class);
		in.putExtra("type","feed");
		in.putExtra("selection",tag);
		in.putExtra("title",feedName.get(tag));
		startActivity(in);
	}

	public void actionCat(View v) {
		String tag=(String)v.getTag();
		//Toast.makeText(this, "actionCat! tag is "+tag, Toast.LENGTH_LONG).show();
		Intent in = new Intent(this, ActivityUdeMNouvelles.class);
		in.putExtra("type","category");
		in.putExtra("selection",tag);
		in.putExtra("title",tag);
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

		// on va definir un count de 0 pour les feed absents
		Set<String> keys=feedName.keySet();
		for(String i : keys) {
			Log.d("cursor","reset feed "+i);
			feedCount.put(i,0);
		}
		
		Cursor c=db.rawQuery("select _id,feed,count(*) from timeline group by feed", null);

		c.moveToFirst();
		while( !c.isAfterLast() ) {
			Log.d("cursor","feed "+c.getString(1)+" = "+c.getInt(2));
			feedCount.put(c.getString(1),c.getInt(2));
			c.moveToNext();
		}
		c.close();

		
		cursor=db.rawQuery("select _id,feed,category,count(*) from timeline group by category order by feed asc, category asc", null);

		// on veut ajouter deux rangees au cursor
		MatrixCursor extras = new MatrixCursor(new String[] { "_id", "feed","category","count(*)" });
		// ajoute les feed qui on 0 elements
		int j=1;
		for(String i : feedCount.keySet()) {
			if( feedCount.get(i)==0 ) {
				extras.addRow(new String[] { ""+j, i,"cat","0" });
			}
			j++;
		}
		Cursor[] cursors = { cursor, extras };
		Cursor extendedCursor = new MergeCursor(cursors);
		
		startManagingCursor(cursor);

		// adapter
		adapter = new MySimpleCursorAdapter(this, R.layout.rowcat, extendedCursor /*cursor*/, from, to);
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

			CheckBox cb=(CheckBox)view.findViewById(R.id.rowfeedcheck);
			TextView vf=(TextView)view.findViewById(R.id.rowfeed);
			TextView vfc=(TextView)view.findViewById(R.id.rowfeedcount);
			TextView vc=(TextView)view.findViewById(R.id.rowcat);
			TextView vcc=(TextView)view.findViewById(R.id.rowcatcount);

			Log.d("cursor","position="+cursor.getPosition());

			// 1 = feed, 2=category, 3=count
			String feed=cursor.getString(1);
			String cat=cursor.getString(2);
			String nbCat=cursor.getString(3);
			
			// preferences du feed
			boolean use = preferences.getBoolean(feed, false);

			// on voit la categorie seulement si >0 elements
			int n=Integer.parseInt(nbCat);
			vc.setVisibility(n>0?View.VISIBLE:View.GONE);
			vcc.setVisibility(n>0?View.VISIBLE:View.GONE);


			// ... comment
			int nbItem=ActivityUdeMListFC.this.feedCount.get(feed);
			cb.setChecked(use);
			vf.setText(ActivityUdeMListFC.this.feedName.get(feed)/*+"<"+cursor.getPosition()+">"*/);
			vfc.setText(""+nbItem);

			cb.setVisibility(View.VISIBLE);
			vf.setVisibility(View.VISIBLE);
			vfc.setVisibility(nbItem>0?View.VISIBLE:View.INVISIBLE);

			// verifier si le precedent item a le meme feed
			if( cursor.getPosition()>0 ) {
				cursor.moveToPrevious();
				if( cursor.getString(1).equals(feed) ) {
					// on est pareil comme le precedent 
					cb.setVisibility(View.GONE);
					vf.setVisibility(View.GONE);
					vfc.setVisibility(View.GONE);
				}
				cursor.moveToNext();
			}

			vc.setText(cat);
			vcc.setText(nbCat);
			
			// ajuste les tags au cas ou on clic
			cb.setTag(feed);
			view.findViewById(R.id.feedinfo).setTag(feed);
			view.findViewById(R.id.catinfo).setTag(cat);
		}
	}
}




