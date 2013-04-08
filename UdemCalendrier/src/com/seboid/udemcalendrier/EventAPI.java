package com.seboid.udemcalendrier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.text.Html;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

//
// une classe qui charge une page web et parse le contenu JSON
// NOTE: cette classe NE DOIT PAS toucher à l'interface
//
// http://services.murmitoyen.com/udem/evenements/141154
//

public class EventAPI {
	// base
	HashMap<String,String> base;
	// lieu
	ArrayList<HashMap<String,String>> lieuList;
	// categorie
	ArrayList<HashMap<String,String>> catList;
	// groupe
	ArrayList<HashMap<String,String>> groupeList;
	// souscategorie
	ArrayList<HashMap<String,String>> souscatList;

	// null si pas d'erreur
	String erreur;
	long time;

	EventAPI(int id) {
		int i;

		erreur=null;
		String url="http://services.murmitoyen.com/udem/evenement/"+id;

		Log.d("event","loading '"+url+"'");
		
		time=System.currentTimeMillis();

		base=null;
		lieuList=new ArrayList<HashMap<String,String>>();
		catList=new ArrayList<HashMap<String,String>>();
		groupeList=new ArrayList<HashMap<String,String>>();
		souscatList=new ArrayList<HashMap<String,String>>();

		JsonReader jr;

		try {
			// lire la page web
			HttpEntity page = getHttp(url);

			// on va filter les <img ... /> parce qu' elles sont base64 et trop grosses
			myFilterInputStream in=new myFilterInputStream(page.getContent(),"<img","/>");

			jr = new JsonReader(new InputStreamReader(in, "UTF-8"));

			try {
				jr.beginObject();
				while (jr.hasNext()) {
					String n1 = jr.nextName();
					Log.d("event","got >"+n1+"<");
					if( n1.equals("donnees") ) {
						jr.beginObject();
						while( jr.hasNext() ) {
							String n2 = jr.nextName();
							if( n2.equals("base") ) {
								jr.beginArray();
								if( jr.hasNext() ) base=readGeneric(jr); // on prend seulement le premier
								while( jr.hasNext() ) jr.skipValue();
								jr.endArray();
							}else if( n2.equals("lieu") ) {
								jr.beginArray();
								while( jr.hasNext() ) lieuList.add(readGeneric(jr));
								jr.endArray();
							}else if( n2.equals("categories") ) {
								jr.beginArray();
								while( jr.hasNext() ) catList.add(readGeneric(jr));
								jr.endArray();
							}else if( n2.equals("groupes") ) {
								jr.beginArray();
								while( jr.hasNext() ) groupeList.add(readGeneric(jr));
								jr.endArray();
							}else if( n2.equals("souscategories") ) {								
								jr.beginArray();
								while( jr.hasNext() ) souscatList.add(readGeneric(jr));
								jr.endArray();
							}else jr.skipValue();
						}
						jr.endObject();
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

		//
		// champs calcules...
		//
		// on ajoute a la base une liste des lieux, cat, groupe, souscat id en string
		//
		String z;
		String lieux=null;
		for( HashMap<String,String> hm : lieuList ) {
			if( (z=hm.get("id_lieu"))!=null ) { if( lieux==null ) lieux=z; else lieux+=","+z; }
		}
		String categories=null;
		for( HashMap<String,String> hm : catList ) {
			if( (z=hm.get("id_categorie"))!=null ) {
				if( categories==null ) categories=z; else categories+=":"+z;
			}
		}
		String groupes=null;
		for( HashMap<String,String> hm : groupeList ) {
			if( (z=hm.get("id_groupe"))!=null ) {
				if( groupes==null ) groupes=z; else groupes+=":"+z;
			}
		}
		String souscategories=null;
		for( HashMap<String,String> hm : souscatList ) {
			if( (z=hm.get("id_categorie"))!=null ) {
				if( souscategories==null ) souscategories=z; else souscategories+=":"+z;
			}
		}

		if( base==null ) {
			Log.d("event","erreur loading event");
			erreur="Erreur url='"+url+"'";
			return;
		}
		
		// ajoute a la base
		base.put("ids_lieux",":"+lieux+":");
		base.put("ids_categories",":"+categories+":");
		base.put("ids_groupes",":"+groupes+":");
		base.put("ids_souscategories",":"+souscategories+":");		

		// les heures de depart et fin en long
		base.put("epoch_debut", Long.toString(TempsUtil.dateHeure2epoch(base.get("date"),base.get("heure_debut"),true)));
		base.put("epoch_fin", Long.toString(TempsUtil.dateHeure2epoch(base.get("date"),base.get("heure_fin"),false)));
		base.put("epoch_modif", Long.toString(TempsUtil.dateHeure2epoch(base.get("date_modif"))));
		
		time=System.currentTimeMillis()-time;
		
		// debug output
		Set<String> ss;

//		for( String s : base.keySet() ) Log.d("event","base:"+s+"="+base.get(s));
//		for( HashMap<String,String> hm : lieuList ) {
//			for( String s : hm.keySet() ) Log.d("event","lieu:"+s+"="+hm.get(s));
//		}
//		for( HashMap<String,String> hm : catList ) {
//			for( String s : hm.keySet() ) Log.d("event","cat:"+s+"="+hm.get(s));
//		}
//		for( HashMap<String,String> hm : groupeList ) {
//			for( String s : hm.keySet() ) Log.d("event","groupe:"+s+"="+hm.get(s));
//		}
//		for( HashMap<String,String> hm : souscatList ) {
//			for( String s : hm.keySet() ) Log.d("event","souscat:"+s+"="+hm.get(s));
//		}

	}

	//
	// JSON: read generic object data into hashmap
	//
	HashMap<String,String> readGeneric(JsonReader jr) throws IOException {
		HashMap<String,String> hm=new HashMap<String,String>();

		jr.beginObject();
		while( jr.hasNext() ) {
			String n= jr.nextName();
			String v="null"; // les null deviennent des "null" pour l'instant
			if( jr.peek()==JsonToken.NULL ) jr.nextNull();
			else v=jr.nextString();
			if( !n.equals("description") ) v=Html.fromHtml(v).toString(); // convertir &eacute; en e aigu utf8.. enleve les tags html
			hm.put(n,v);
		}
		jr.endObject();
		// process certains champs...
		//long d1=date2epoch(hm.get("date"),null);		
		return hm;
	}


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


}
