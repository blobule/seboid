package com.seboid.udembottin;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil {

//	//
//	// lire une page web et retourner le contenu
//	// IllegalStateException -> url mal forme...
//	//
//	public static HttpEntity getHttp(String url)
//			throws ClientProtocolException, IOException, IllegalStateException {
//		HttpClient httpClient = new DefaultHttpClient();
//		HttpGet http = new HttpGet(url);
//		HttpResponse response = httpClient.execute(http);
//		return response.getEntity();
//	}
//
//	//
//	// lire une image avec un URL
//	//
//	public static Bitmap loadHttpImage(String url)
//			throws ClientProtocolException, IOException, IllegalStateException {
//		InputStream is = getHttp(url).getContent();
//		Bitmap b = BitmapFactory.decodeStream(is);
//		// Drawable d = Drawable.createFromStream(is, "src");
//		return b;
//	}
//
//	//
//	// lire une image avec un URL
//	//
//	public static Drawable loadHttpImageDrawable(String url)
//			throws ClientProtocolException, IOException, IllegalStateException {
//		InputStream is = getHttp(url).getContent();
//		Drawable d = Drawable.createFromStream(is, "src");
//		return d;
//	}

	//
	// etat du reseau? on a acces?
	//
	public static boolean networkOK(Context ctx) {
		ConnectivityManager conMgr =  (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo net = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		boolean mobile= (net!=null)?net.isConnected():false;

		net=conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean wifi=(net!=null)?net.isConnected():false;

		return ( wifi || mobile );
	}
	
}
