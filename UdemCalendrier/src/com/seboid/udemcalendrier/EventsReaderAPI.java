package com.seboid.udemcalendrier;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

public class EventsReaderAPI {
	// les informations intéressantes
	//Drawable icone;


	ArrayList<HashMap<String,String>> hmList;

	// null si pas d'erreur
	String erreur;

	EventsReaderAPI(String type,String id,String start,String end) {
		erreur=null;

		//		String url="http://services.murmitoyen.com/udem/evenements/2013-03-26/2013-03-26";

		String url="http://services.murmitoyen.com/udem/"+type;
		if( !type.equals("evenements") ) url+="/"+id;
		if( !type.equals("serie") ) url+="/"+start+"/"+end;

		//		Log.d("events","loading "+url);

		hmList=new ArrayList<HashMap<String,String>>();

		JsonReader jr;

		HashMap<String,String> hm;


		try {
			// lire la page web
			HttpEntity page = getHttp(url);

			// on va filter les <img ... >
			myFilterInputStream in=new myFilterInputStream(page.getContent());

			jr = new JsonReader(new InputStreamReader(in, "UTF-8"));

			try {

				jr.beginObject();
				while (jr.hasNext()) {
					String name = jr.nextName();
					//					Log.d("json","name is "+name);
					if( name.equals("donnees") ) {
						jr.beginArray();
						while( jr.hasNext() ) {
							// un item complet
							hm=new HashMap<String,String>();

							jr.beginObject();
							while( jr.hasNext() ) {
								String n= jr.nextName();
								//								Log.d("json","name is "+n);
								String v="null";
								if( jr.peek()==JsonToken.NULL ) jr.nextNull();
								else v=jr.nextString();
								if( n.equals("titre") ) v=Html.fromHtml(v).toString();
								hm.put(n,v);
								//if( n.equals("id") ) Log.d("events","id="+v);
							}
							jr.endObject();
							// ajoute cet item
							hmList.add(hm);
							// debug output		
//							Set<String> ss = hm.keySet();
//							for( String s : ss ) {
//								Log.d("event",s+" = "+hm.get(s));
//							}
						}
						jr.endArray();


					}else jr.skipValue();

					//				if (name.equals("id")) {
					//					id = reader.nextLong();
					//				} else if (name.equals("text")) {
					//					text = reader.nextString();
					//				} else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {
					//					geo = readDoublesArray(reader);
					//				} else if (name.equals("user")) {
					//					user = readUser(reader);
					//				} else {
					//					reader.skipValue();
					//				}
				}
				jr.endObject();

			} finally {
				jr.close();
			}
			// un evenement
			//			HashMap<String,String> hm;



			/*
			//String content = EntityUtils.toString(page,HTTP.UTF_8);

			// JSON format
			JSONObject js = new JSONObject(content);
			// extraire les informations courantes
			JSONArray obs = js.getJSONArray("donnees");

			//	Log.d("event","nb child is "+obs.length());


			for(int i=0;i<obs.length();i++) {
				JSONObject event=obs.getJSONObject(i);
				//	Log.d("event",event.get("id")+" nbvalues= "+event.length());

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

				long d1=date2epoch(hm.get("date"),null);
				//	Log.d("events","date start = "+d1);

				hmList.add(hm);
			}

			 */
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

	private long date2epoch(String date,String time) {
		String str = "Jun 13 2003 23:11:52.454 UTC";
		SimpleDateFormat df;
		Date d;
		try {
			if( time!=null ) {
				df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				d=df.parse(date);
			}else{
				df=new SimpleDateFormat("yyyy-MM-dd");	    	
				d=df.parse(date+" "+time);
			}
			return(d.getTime());
		} catch( Exception e ) {
			return(0);
		}
	}

	//
	// test d'un filter stream
	//
	// filtre <img  ...... >
	//
	class myFilterInputStream extends FilterInputStream {

		int nbRead1;
//		int nbRead;

		boolean searchFrom; // true -> search for from pattern, false -> searching for to pattern
		byte[] from;
		byte[] to;

		byte[] buffrom;
		byte[] bufto;
		int nb; // nb bytes in buffer

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			super.close();
			Log.d("filter","read "+nbRead1+" bytes");
		}

		protected myFilterInputStream(InputStream in) {
			super(in);
		//	nbRead=0;
			nbRead1=0;
			from=null;
			to=null;
			try {
				from="<img".getBytes("UTF-8");
				to=">".getBytes("UTF-8");
				buffrom=new byte[from.length];
				bufto=new byte[to.length];
			} catch (UnsupportedEncodingException e) { }
			searchFrom=true;
			nb=0;
		}

		@Override
		public int read() throws IOException {
		nbRead1++;
			int i;

			if( searchFrom ) {
				// le buffer doit etre rempli de la bonne taille
				while( nb<from.length ) {
					int c=super.read();
					if( c<0 ) break;
					buffrom[nb]=(byte)c;
					nb++;
				}
				if( nb!=from.length || !Arrays.equals(buffrom,from)) {
					// eof. output first char and move the buffer left
					if( nb==0 ) return -1;
					byte k=buffrom[0];
					for(i=1;i<nb;i++) buffrom[i-1]=buffrom[i];
					nb--;
					return k;
				}
				// un match!!!!!
				searchFrom=false;
				// remplace plutot que vide le buffer
				// normalement on vide le buffer... attention
				//for(i=0;i<nb;i++) buf[i]=(byte)'x';
				nb=0; // flush le buffer. On repart a zero.
			}
			// on cherche un to et on skip tant qu'on a pas trouve
			for(;;) {
				// on cherche le to
				// le buffer doit etre rempli de la bonne taille
				while( nb<to.length ) {
					int c=super.read();
					if( c<0 ) break;
					bufto[nb]=(byte)c;
					nb++;
				}
				if( nb!=to.length || !Arrays.equals(bufto,to) ) {
					// eof. output first char and move the buffer left
					if( nb==0 ) return -1;
					byte k=bufto[0];
					for(i=1;i<nb;i++) bufto[i-1]=bufto[i];
					nb--;
					continue; // on skip et on tente le prochain char
					//return (byte)'y'; // on remplace par un autre char... normalement on skip
				}
				// un match!!!!!
				searchFrom=true;
				// remplace plutot que vide le buffer
				// normalement on vide le buffer... attention
				//for(i=0;i<nb;i++) buf[i]=(byte)'x';
				nb=0; // on vide le buffer parce qu' on skip son contenu
				break;
			}

			return read();
		}

		@Override
		public int read(byte[] buffer, int offset, int count)
				throws IOException {
//			nbRead++;
			//			int k=super.read(buffer, offset, count);

			if( count==0 ) return 0;

			int i;
			int c;
			for(i=0;i<count;i++) {
				c=read();
				if( c<0 ) break;
				buffer[offset+i]=(byte)c;
			}
			if( i==0 ) return(-1); // eof
			return i; // devrait toujours etre >0, en fait =count
		}



	}

}
