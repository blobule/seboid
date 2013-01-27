package com.seboid.udem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	static final String TAG = "db";
	static final String DB_NAME = "feed.db";
	static final int DB_VERSION = 10;
	static final String TABLE = "timeline";
	static final String C_ID = "_id"; // obligatoire pour cursor...
	static final String C_TITLE = "title";
	static final String C_TIME = "time";
	static final String C_CATEGORY = "category";
	static final String C_FEED = "feed";
	static final String C_LINK = "link";
	static final String C_DESC = "description";
	static final String C_FAVORI = "favori";
	Context context;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="create table "+TABLE+" ("
				+C_ID+" integer primary key, "
				+C_TIME+" int,"
				+C_TITLE+" text,"
				+C_CATEGORY+" text,"
				+C_FEED+" text,"
				+C_LINK+" text,"
				+C_DESC+" text,"
				+C_FAVORI+" boolean)";
		db.execSQL(sql);
		Log.d(TAG,"created db:"+sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists "+TABLE);
		Log.d(TAG,"upgraded db");
		onCreate(db);
	}

	public String[] getWebContent(SQLiteDatabase db,long id) {
		Cursor c=db.rawQuery("select title,link,description from timeline where _id="+id, null);
		if( !c.moveToFirst() ) return new String[] {"(pas de titre)","","(pas de description)"};
		return new String[] {c.getString(0),c.getString(1),c.getString(2)};
	}
}
