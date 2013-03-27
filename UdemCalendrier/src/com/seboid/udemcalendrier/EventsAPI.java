package com.seboid.udemcalendrier;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
// http://services.murmitoyen.com/udem/evenements/2013-03-24/2013-03-30
// http://services.murmitoyen.com/udem/categorie/2/2013-01-26/2013-02-01/
// http://services.murmitoyen.com/udem/groupe/7/2013-01-26/2013-02-01/
// http://services.murmitoyen.com/udem/souscategorie/64/2013-01-26/2013-02-01/
//

public class EventsAPI {
	// les informations intéressantes
	//Drawable icone;

	ArrayList<HashMap<String,String>> hmList;
	
	// null si pas d'erreur
	String erreur;

	EventsAPI() {
		erreur=null;
		
		String url="http://services.murmitoyen.com/udem/evenements/2013-03-26/2013-03-26";

		hmList=new ArrayList<HashMap<String,String>>();
		
		try {
			// lire la page web
			HttpEntity page = getHttp(url);
			String content = EntityUtils.toString(page,HTTP.UTF_8);
			
			// JSON format
			JSONObject js = new JSONObject(content);
			// extraire les informations courantes
			JSONArray obs = js.getJSONArray("donnees");
			
			Log.d("event","nb child is "+obs.length());

			// un evenement
			HashMap<String,String> hm;
			
			for(int i=0;i<obs.length();i++) {
				JSONObject event=obs.getJSONObject(i);
				Log.d("event",event.get("id")+" nbvalues= "+event.length());
				
				hm=new HashMap<String,String>();

				// Html.fromHtml est supposemment lent... a verifier...
				hm.put("id",event.get("id").toString());
				hm.put("titre",Html.fromHtml(event.get("titre").toString()).toString());
				hm.put("url",event.get("url").toString());
				hm.put("description",event.get("description").toString());
				hm.put("date",event.get("date").toString());
				hm.put("heure_debut",event.get("heure_debut").toString());
				hm.put("heure_fin",event.get("heure_fin").toString());
				hm.put("date_modif",event.get("date_modif").toString());
				hm.put("type_horaire",event.get("type_horaire").toString());
				hm.put("vignette",event.get("vignette").toString());
				hm.put("image",event.get("image").toString());
				
				hmList.add(hm);
			}
			
//			temperature=obs.getString("temp_c")+"c";
//			conditions=obs.getString("weather");
//			ville = obs.getJSONObject("display_location").getString("full");
//			
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
