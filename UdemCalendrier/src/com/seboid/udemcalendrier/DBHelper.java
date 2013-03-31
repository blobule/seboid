package com.seboid.udemcalendrier;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//
// Base de donnee pour le calendrier UdeM
//
// table Events -> un evenement complet
//           champs ajoutes: ids_groupes, ids_lieux, ids_categories, ids_souscategories -> contiennent des listes de id
//                         + epoch_debut et epoch_fin
// table Categories, Groupes, SousCategories -> id + description
// table Lieux -> id + autres champs
//


public class DBHelper extends SQLiteOpenHelper {
	static final String TAG = "db";

	static final String DB_NAME = "calendrier.db";
	static final int DB_VERSION = 7;


	// categories
	static final String TABLE_C = "categories";
	static final String C_C_ID = "_id"; // obligatoire pour cursor...
	static final String C_C_DESC = "desc";

	// souscategories
	static final String TABLE_SC = "souscategories";
	static final String C_SC_ID = "_id"; // obligatoire pour cursor...
	static final String C_SC_DESC = "desc";

	// groupes
	static final String TABLE_G = "groupes";
	static final String C_G_ID = "_id"; // obligatoire pour cursor...
	static final String C_G_DESC = "desc";

	// lieux
	static final String TABLE_L = "lieux";
	static final String C_L_ID = "_id"; // obligatoire pour cursor... (id_lieu)
	static final String C_L_DESC = "desc"; // (lieu_nom)
	static final String C_L_SALLE = "salle";
	static final String C_L_ADRESSE = "adresse";
	static final String C_L_ADRESSE2 = "adresse2";
	static final String C_L_VILLE = "ville";
	static final String C_L_PROVINCE = "province";
	static final String C_L_PAYS = "pays";
	static final String C_L_CODEPOSTAL = "code_postal";
	static final String C_L_LATITUDE = "latitude";
	static final String C_L_LONGITUDE = "longitude";

	// events
	static final String TABLE_E = "events";

	static final String C_ID = "_id"; // obligatoire pour cursor...
	static final String C_TITRE = "titre";
	static final String C_DESCRIPTION = "description";
	static final String C_CONTACT_NOM = "contact_nom";
	static final String C_CONTACT_COURRIEL = "contact_courriel";
	static final String C_CONTACT_TEL = "contact_tel";
	static final String C_CONTACT_URL = "contact_url";
	static final String C_SERIE = "serie";
	static final String C_COUT = "cout";
	static final String C_DATE = "date";
	static final String C_HEURE_DEBUT="heure_debut";
	static final String C_HEURE_FIN="heure_fin";
	static final String C_DATE_MODIF="date_modif";
	static final String C_TYPE_HORAIRE="type_horaire";
	static final String C_VIGNETTE="vignette";
	static final String C_IMAGE="image";
	static final String C_EPOCH_DEBUT="epoch_debut";
	static final String C_EPOCH_FIN="epoch_fin";
	static final String C_IDS_LIEUX="ids_lieux";
	static final String C_IDS_GROUPES="ids_groupes";
	static final String C_IDS_CATEGORIES="ids_categories";
	static final String C_IDS_SOUSCATEGORIES="ids_souscategories";

	Context context;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="create table "+TABLE_E+" ("
				+C_ID+" integer primary key,"
				+C_TITRE+" text,"
				+C_DESCRIPTION+" text,"
				+C_CONTACT_NOM+" text,"
				+C_CONTACT_COURRIEL+" text,"
				+C_CONTACT_TEL+" text,"
				+C_CONTACT_URL+" text,"
				+C_SERIE+" text,"
				+C_COUT+" text,"
				+C_DATE+" text,"
				+C_HEURE_DEBUT+" text,"
				+C_HEURE_FIN+" text,"
				+C_DATE_MODIF+" text,"
				+C_TYPE_HORAIRE+" text,"
				+C_VIGNETTE+" text,"
				+C_IMAGE+" text,"
				+C_EPOCH_DEBUT+" long,"
				+C_EPOCH_FIN+" long,"
				+C_IDS_LIEUX+" text,"
				+C_IDS_GROUPES+" text,"
				+C_IDS_CATEGORIES+" text,"
				+C_IDS_SOUSCATEGORIES+" text)";
		db.execSQL(sql);
		sql="create table "+TABLE_C+" ("
				+C_C_ID+" integer primary key,"
				+C_C_DESC+" text)";
		db.execSQL(sql);
		sql="create table "+TABLE_SC+" ("
				+C_SC_ID+" integer primary key,"
				+C_SC_DESC+" text)";
		db.execSQL(sql);
		sql="create table "+TABLE_G+" ("
				+C_SC_ID+" integer primary key,"
				+C_SC_DESC+" text)";
		db.execSQL(sql);
		sql="create table "+TABLE_L+" ("
				+C_L_ID+" integer primary key,"
				+C_L_DESC+" text,"
				+C_L_SALLE+" text,"
				+C_L_ADRESSE+" text,"
				+C_L_ADRESSE2+" text,"
				+C_L_VILLE+" text,"
				+C_L_PROVINCE+" text,"
				+C_L_PAYS+" text,"
				+C_L_CODEPOSTAL+" text,"
				+C_L_LATITUDE+" float,"
				+C_L_LONGITUDE+" float)";
		db.execSQL(sql);

		Log.d(TAG,"created db:"+sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists "+TABLE_E);
		db.execSQL("drop table if exists "+TABLE_C);
		db.execSQL("drop table if exists "+TABLE_SC);
		db.execSQL("drop table if exists "+TABLE_G);
		db.execSQL("drop table if exists "+TABLE_L);
		Log.d(TAG,"upgraded db");
		onCreate(db);
	}

	//	public String[] getWebContent(SQLiteDatabase db,long id) {
	//		Cursor c=db.rawQuery("select title,link,description,longdescription,image from timeline where _id="+id, null);
	//		if( !c.moveToFirst() ) return new String[] {"(pas de titre)","","(pas de description)"};
	//		return new String[] {c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4)};
	//	}

	//
	// utility stuff
	//

	public void resetDB() {
		SQLiteDatabase db;
		db=getWritableDatabase();

		int k=db.delete(DBHelper.TABLE_E, null, null);
		db.delete(DBHelper.TABLE_C, null, null);
		db.delete(DBHelper.TABLE_SC, null, null);
		db.delete(DBHelper.TABLE_G, null, null);
		db.delete(DBHelper.TABLE_L, null, null);
		Log.d(TAG,"deleted "+k+" items.");

		db.close();
	}

}
