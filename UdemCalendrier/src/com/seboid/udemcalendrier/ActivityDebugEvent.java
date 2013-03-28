package com.seboid.udemcalendrier;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDebugEvent extends Activity {

	TextView tvId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.event);
		
		tvId=(TextView)findViewById(R.id.data_id);
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
		new DownloadEventTask().execute();
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
		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected EventAPI doInBackground(Void... arg0) {
			EventAPI events=new EventAPI("180047");
			return events;
		}

		@Override
		protected void onPostExecute(EventAPI result) {
			setProgressBarIndeterminateVisibility(false);
			Toast.makeText(ActivityDebugEvent.this, result.erreur==null?("time="+result.time/1000.):result.erreur, Toast.LENGTH_SHORT).show();
			//
			// affiche!
			//
			tvId.setText(result.base.get("id"));
		}
	}
	
	
}
