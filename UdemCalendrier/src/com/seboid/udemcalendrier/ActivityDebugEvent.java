package com.seboid.udemcalendrier;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDebugEvent extends Activity {

	int id; // item a afficher
	TextView tTitre;
	TextView tDate;
	TextView tHeure;
	WebView web;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.event);
		
		// item a afficher, ou -1 si rien de prevu
		id=this.getIntent().getIntExtra("id",-1);
		Log.d("event","event id to load is "+id);

		tTitre=(TextView)findViewById(R.id.text_titre);
		tDate=(TextView)findViewById(R.id.text_date);
		tHeure=(TextView)findViewById(R.id.text_heure);
		web=(WebView)findViewById(R.id.web_desc);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	protected void onResume() {
		super.onResume();
		new DownloadEventTask(id).execute();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}


	//
	// web asynchrone
	//
	
	class DownloadEventTask extends AsyncTask<Void,Void,EventAPI> {
		
		int id; // item a lire
		
		public DownloadEventTask(int id) {
			super();
			this.id=id;
		}
		
		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected EventAPI doInBackground(Void... arg0) {
			EventAPI event=new EventAPI(id);
			return event;
		}

		@Override
		protected void onPostExecute(EventAPI result) {
			setProgressBarIndeterminateVisibility(false);
			Toast.makeText(ActivityDebugEvent.this, result.erreur==null?("time="+result.time/1000.):result.erreur, Toast.LENGTH_SHORT).show();
			//
			// affiche!
			//
			if( result.erreur==null ) {
				tTitre.setText(result.base.get("titre"));
				tDate.setText(result.base.get("date"));
				tHeure.setText(result.base.get("heure_debut")+" a "+result.base.get("heure_fin"));
				
				web.setScrollContainer(true);
				web.setScrollbarFadingEnabled(false);
				web.setBackgroundColor(0xff000000);
				web.getSettings().setJavaScriptEnabled(false);
				web.loadDataWithBaseURL(null,"<style type=\"text/css\">body { color:"
						+"#ffffff"
						+"; background-color:"
						+"#000000"
						+" } a { color:"
						+"#8080ff"
						+"; } h2 { color:"
						+"#ffffff"
						+"; } </style><body>"
						+result.base.get("description")+"</body>", "text/html","utf-8",null);				
			}else{
				tTitre.setText(result.erreur);
			}
		}
	}
	
	
}
