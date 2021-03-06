package com.seboid.udem;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class UdeMContentProvider extends ContentProvider {

	// le database
	DBHelper dbh;

	// pour les URI
	private static final String AUTHORITY = "com.seboid.udem.nouvelles";
	public static final int NOUVELLES = 10;
	public static final int NOUVELLE_ID = 20;


	private static final String BASE_PATH = "nouvelles";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	// mime type... pas super utile pour l'instant....
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/nouvelles";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/nouvelle";

	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		// content://com.seboid.udem.nouvelles/nouvelles   -> toutes les nouvelles
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, NOUVELLES);
		// on utilise * parce que # ne supporte que des nombres positifs... :-(
		// content://com.seboid.udem.nouvelles/nouvelles/1654235   -> une nouvelle en particulier
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", NOUVELLE_ID);
	}


	@Override
	public boolean onCreate() {
		dbh = new DBHelper(getContext());
		Log.d("contentprovider","created.");
		return false;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = dbh.getWritableDatabase();
		int rowsDeleted = 0;
		Log.d("contentprovider","delete "+uri);

		switch (uriType) {
		case NOUVELLES:
			rowsDeleted = sqlDB.delete(DBHelper.TABLE, selection,
					selectionArgs);
			break;
		case NOUVELLE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(DBHelper.TABLE,
						DBHelper.C_ID + "=" + id, 
						null);
			} else {
				rowsDeleted = sqlDB.delete(DBHelper.TABLE,
						DBHelper.C_ID + "=" + id 
						+ " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d("contentprovider","insert "+uri);

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = dbh.getWritableDatabase();
		int rowsDeleted = 0;
		long id = 0;
		switch (uriType) {
		case NOUVELLES:
			try {
				id = sqlDB.insertOrThrow(DBHelper.TABLE, null, values);
			} catch ( SQLException e ) {
				/* deja une cle dans la bd... insert failed */
				return null; }
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return 	Uri.parse(BASE_PATH + "/" + values.get(DBHelper.C_ID) );
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		Log.d("contentprovider","query "+uri);
		
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(DBHelper.TABLE);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case NOUVELLES:
			break;
		case NOUVELLE_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(DBHelper.C_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = dbh.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d("contentprovider","update "+uri);

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = dbh.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case NOUVELLES:
			rowsUpdated = sqlDB.update(DBHelper.TABLE,values,selection,selectionArgs);
			break;
		case NOUVELLE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(DBHelper.TABLE, values,
						DBHelper.C_ID + "=" + id, 
						null);
			} else {
				rowsUpdated = sqlDB.update(DBHelper.TABLE, 
						values,
						DBHelper.C_ID + "=" + id 
						+ " and " 
						+ selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = {
				DBHelper.C_ID,
				DBHelper.C_TITLE,
				DBHelper.C_TIME,
				DBHelper.C_CATEGORY,
				DBHelper.C_FEED,
				DBHelper.C_LINK,
				DBHelper.C_DESC,
				DBHelper.C_LONGDESC,
				DBHelper.C_IMAGE,
				DBHelper.C_LU,
				DBHelper.C_FAVORI };
				
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}


}
