package com.seboid.udemcalendrier;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ActivityDebugEvents extends Activity implements OnItemClickListener {

	ListView listv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.events);

		listv=(ListView)findViewById(R.id.listViewEvents);

		listv.setOnItemClickListener(this);

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
		new DownloadEventsTask().execute();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		//Toast.makeText(ActivityDebugEvents.this, "item "+position+":"+id, Toast.LENGTH_LONG).show();
		Intent in=new Intent(ActivityDebugEvents.this,ActivityDebugEvent.class);
		startActivity(in);
	}


	//
	// web asynchrone
	//

	class DownloadEventsTask extends AsyncTask<Void,Void,EventsAPI> {
		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected EventsAPI doInBackground(Void... arg0) {
			EventsAPI events=new EventsAPI();
			return events;
		}

		@Override
		protected void onPostExecute(EventsAPI result) {
			setProgressBarIndeterminateVisibility(false);
			Toast.makeText(ActivityDebugEvents.this, "done", Toast.LENGTH_SHORT).show();
			//
			// affiche!
			//
			String[] from= {"titre","date","heure_debut","heure_fin"};
			int[] to= { R.id.text1,R.id.text2,R.id.text3,R.id.text4};
			listv.setAdapter( new SimpleAdapter(ActivityDebugEvents.this, result.hmList,
					R.layout.events_row,from,to) );
		}
	}


}
