package com.seboid.udemcalendrier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.text.Html;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;



//
// une classe qui charge une page web et parse le contenu JSON
// NOTE: cette classe NE DOIT PAS toucher à l'interface
//
// http://services.murmitoyen.com/udem/evenements/2013-03-24/2013-03-30
// http://services.murmitoyen.com/udem/categorie/2/2013-01-26/2013-02-01/
// http://services.murmitoyen.com/udem/groupe/7/2013-01-26/2013-02-01/
// http://services.murmitoyen.com/udem/souscategorie/64/2013-01-26/2013-02-01/
//
// on a aussi les series (tag d'un evenement en particulier)
// http://services.murmitoyen.com/udem/serie/cfd4b81e5657376b6
// (dans une serie, tous les evenements sont identiques, sauf pour les dates... ce sont de vrais
// evenements qui doivent etre loader dans la bd. Ensuite on peut faire des select pour ramasser tout ca...
//
// http://services.murmitoyen.com/udem/<type>/<id>/<start>/<end>
//
// type=evenements, id=null, start/end obligatoire
// type=categorie|groupe|souscategorie, id, start/end obligatoire
// type=serie, id, start=null,end=null
//
// Load les data dans la bd.
//
// http://developer.android.com/reference/android/util/JsonReader.html
//

public class EventsAPI {
	// les informations intéressantes
	//Drawable icone;

	long time;

	ArrayList<HashMap<String,String>> hmList;

	// null si pas d'erreur
	String erreur;

	EventsAPI(String type,String id,String start,String end) {
		erreur=null;

		time=System.currentTimeMillis();

		//		String url="http://services.murmitoyen.com/udem/evenements/2013-03-26/2013-03-26";

		String url="http://services.murmitoyen.com/udem/"+type;
		if( !type.equals("evenements") ) url+="/"+id;
		if( !type.equals("serie") ) url+="/"+start+"/"+end;

		//		Log.d("events","loading "+url);

		hmList=new ArrayList<HashMap<String,String>>();

		JsonReader jr;


		try {
			// lire la page web
			HttpEntity page = NetUtil.getHttp(url);

			// on va filter les <img ... /> parce qu' elles sont base64 et trop grosses
			myFilterInputStream in=new myFilterInputStream(page.getContent(),"<img","/>");

			jr = new JsonReader(new InputStreamReader(in, "UTF-8"));

			try {
				jr.beginObject();
				while (jr.hasNext()) {
					String name = jr.nextName();
					//					Log.d("json","name is "+name);
					if( name.equals("donnees") ) {
						jr.beginArray();
						while( jr.hasNext() ) {
							hmList.add(readBase(jr));
						}
						jr.endArray();
					}else jr.skipValue();
				}
				jr.endObject();
			} finally {
				jr.close();
			}
		} catch (ClientProtocolException e) {
			erreur="erreur http(protocol):"+e.getMessage();
		} catch (IOException e) {
			erreur="erreur http(IO):"+e.getMessage();
		} catch (ParseException e) {
			erreur="erreur JSON(parse):"+e.getMessage();
		} 


		time=System.currentTimeMillis()-time;
	}

	//
	// JSON: donnees
	//
	//	id, titre, url, description, date, heure_debut, heure_fin,
	//  date_modif, type_horaire, vignette, image
	//
	HashMap<String,String> readBase(JsonReader jr) throws IOException {
		HashMap<String,String> hm=new HashMap<String,String>();
		jr.beginObject();
		while( jr.hasNext() ) {
			String n= jr.nextName();
			String v="null"; // les null deviennent des "null" pour l'instant
			if( jr.peek()==JsonToken.NULL ) jr.nextNull();
			else v=jr.nextString();
			// on ne doit pas convertir la description...
			if( !n.equals("description") ) v=Html.fromHtml(v).toString();
			hm.put(n,v);
		}
		jr.endObject();
		// process certains champs...
		//long d1=date2epoch(hm.get("date"),null);
		//
		// champs calcules
		//
		
		// les heures de depart et fin en long
		hm.put("epoch_debut", Long.toString(TempsUtil.dateHeure2epoch(hm.get("date"),hm.get("heure_debut"),true)));
		hm.put("epoch_fin", Long.toString(TempsUtil.dateHeure2epoch(hm.get("date"),hm.get("heure_fin"),false)));
		hm.put("epoch_modif", Long.toString(TempsUtil.dateHeure2epoch(hm.get("date_modif"))));
		
		return hm;
	}






}
