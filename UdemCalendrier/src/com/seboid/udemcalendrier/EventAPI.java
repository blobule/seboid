package com.seboid.udemcalendrier;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.text.Html;
import android.util.Log;

//
// une classe qui charge une page web et parse le contenu JSON
// NOTE: cette classe NE DOIT PAS toucher à l'interface
//
// http://services.murmitoyen.com/udem/evenements/141154
//

public class EventAPI {
	// les info interessantes
	HashMap<String,String> hm;

	// null si pas d'erreur
	String erreur;

	EventAPI() {
		int i;

		erreur=null;
		String url="http://services.murmitoyen.com/udem/evenement/141154";

		hm=new HashMap<String,String>();

		try {
			// lire la page web
			HttpEntity page = getHttp(url);
			String content = EntityUtils.toString(page,HTTP.UTF_8);

			// JSON format
			JSONObject js = new JSONObject(content);
			// extraire les informations courantes
			JSONObject obs = js.getJSONObject("donnees");

			Log.d("event","nb keys is "+obs.length());

			//
			// base
			//
			JSONObject base=obs.getJSONArray("base").getJSONObject(0); // premier seulement

			Log.d("event","base id="+base.get("id")+" nbval="+base.length());

			String tags[] = {"id","description","contact_tel","contact_url","serie","date","heure_debut"
					,"heure_fin","type_horaire","vignette","image"};
			String tagsH[] = {"titre","contact_nom","contact_courriel","cout"};

			for(i=0;i<tags.length;i++)
				hm.put(tags[i],base.getString(tags[i]));
			for(i=0;i<tagsH.length;i++)
				hm.put(tagsH[i],Html.fromHtml(base.getString(tagsH[i])).toString());
			base=null;

			//
			// lieu
			//
			JSONObject lieu=obs.getJSONArray("lieu").getJSONObject(0); // premier seulement

			tags = new String[] {"id_lieu","code_postal","latitude","longitude" };
			tagsH = new String[] {"lieu_nom","salle","adresse","adresse2","ville","province","pays"};

			for(i=0;i<tags.length;i++)
				hm.put(tags[i],lieu.getString(tags[i]));
			for(i=0;i<tagsH.length;i++)
				hm.put(tagsH[i],Html.fromHtml(lieu.getString(tagsH[i])).toString());
			lieu=null;

			//
			// categories
			//
			JSONObject cat=obs.getJSONArray("categories").getJSONObject(0); // premier seulement

			hm.put("id_categorie",cat.getString("id_categorie"));
			hm.put("categorie_nom",Html.fromHtml(cat.getString("categorie_nom")).toString());
			cat=null;

			// groupes
			JSONObject groupe=obs.getJSONArray("groupes").getJSONObject(0); // premier seulement

			hm.put("id_groupe",groupe.getString("id_groupe"));
			hm.put("groupe_nom",Html.fromHtml(groupe.getString("groupe_nom")).toString());
			groupe=null;
			
			// souscategories
			JSONObject scat=obs.getJSONArray("souscategories").getJSONObject(0); // premier seulement

			// attention ici on change le nom du stockage du tag
			hm.put("id_souscategorie",scat.getString("id_categorie"));
			hm.put("souscategorie_nom",Html.fromHtml(scat.getString("categorie_nom")).toString());
			scat=null;

			// debug output		
			Set<String> ss = hm.keySet();
			for( String s : ss ) {
				Log.d("event",s+" = "+hm.get(s));
			}

			//			long epoch= Long.parseLong(obs.getString("observation_epoch"));
			//			depuis=android.text.format.DateUtils.getRelativeTimeSpanString(epoch*1000);
			//			
			//			String iconName = obs.getString("icon");
			//			if( iconName!=null ) {
			//				Calendar cal= Calendar.getInstance();
			//				int h=cal.get(Calendar.HOUR_OF_DAY);
			//				// on devrait comparer a astronomy:sunset et sunrise
			//				icone=loadHttpImage("http://icons.wxug.com/i/c/a/"+((h>16 || h<7)?"nt_":"")+iconName+".gif");
			//			}
		} catch (ClientProtocolException e) {
			erreur="erreur http(protocol):"+e.getMessage();
		} catch (IOException e) {
			erreur="erreur http(IO):"+e.getMessage();
		} catch (ParseException e) {
			erreur="erreur JSON(parse):"+e.getMessage();
		} catch (JSONException e) {
			erreur="erreur JSON:"+e.getMessage();
		}
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
