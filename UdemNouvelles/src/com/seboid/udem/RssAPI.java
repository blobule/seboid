package com.seboid.udem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

//
// une classe qui charge une page web et parse le contenu XML feed rss
// NOTE: cette classe NE PEUT PAS toucher à l'interface
//
// c'est cette classe qui est utilisee par ServiceRss
//

public class RssAPI {
	// null si pas d'erreur
	String erreur;

	// une liste de maps
	ArrayList<HashMap<String, Object>> data;
	
//	HashMap<String,Integer> categoryIcons;

	// le feed peut etre un des deux sorte de feed de l' UdeM..
	// par exemple, pour campus:
	// "http://www.nouvelles.umontreal.ca/"+"campus"+"/rss.html"
	// "http://www.nouvelles.umontreal.ca/index.php?option=com_ijoomla_rss&act=xml&sec="+"5"+"&feedtype=RSS2.0"
	
	RssAPI(String urlRss) {
		//Log.d("rss","loading "+urlRss);		
		erreur=null;

		URL url;
		try {
			url = new URL(urlRss);

			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			if( conn.getResponseCode() != HttpURLConnection.HTTP_OK ) {
				erreur="pas de connexion";
				return;
			}

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document doc;
			doc = db.parse(url.openStream());
			doc.getDocumentElement().normalize();

			// va chercher les items
			NodeList items = doc.getElementsByTagName("item");

			data=new ArrayList<HashMap<String, Object>>();
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss ZZZZZ",Locale.CANADA);

			for(int i=0;i<items.getLength();i++) {
				Node item = items.item(i);
				if(item.getNodeType() != Node.ELEMENT_NODE) {
					erreur="illegal node type "+item.getNodeType();
					break;
				}
				Element e = (Element)item;
				NodeList n;
				HashMap<String,Object> hm=new HashMap<String,Object>();
				n = e.getElementsByTagName("title");
				hm.put("title",n.item(0).getChildNodes().item(0).getNodeValue());
				n = e.getElementsByTagName("link");
				hm.put("link", n.item(0).getChildNodes().item(0).getNodeValue());
				n = e.getElementsByTagName("description");
				hm.put("description", n.item(0).getChildNodes().item(0).getNodeValue());
				n = e.getElementsByTagName("category");
				hm.put("category",  n.item(0).getChildNodes().item(0).getNodeValue());
				n = e.getElementsByTagName("pubDate");
				hm.put("pubDate",n.item(0).getChildNodes().item(0).getNodeValue());
				n = e.getElementsByTagName("image");
				if( n.getLength()>0 ) {
					String iurl = n.item(0).getChildNodes().item(0).getNodeValue();
					// parfois un url contient des espaces brutes...
					hm.put("image",iurl.replace(" ","%20"));
				}else hm.put("image","");

				// process la date
				// on saute la journee au debut de la date
				Date date = sdf.parse((String) hm.get("pubDate"),new ParsePosition(5));
				hm.put("time",(int)(date.getTime()/1000)); // en secondes
				CharSequence cs=android.text.format.DateUtils.getRelativeTimeSpanString(date.getTime());
				hm.put("since",cs);
				
				data.add(hm);
			}

//			Log.d("rss","loading done. ("+items.getLength()+")");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			erreur=e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			erreur=e.getMessage();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			erreur=e.getMessage();
		} catch (SAXException e) {
			e.printStackTrace();
			erreur=e.getMessage();
		}

	}


}
