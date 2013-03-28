package com.seboid.udemcalendrier;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
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
			//EventsAPI events=new EventsAPI("evenements",null,"2013-03-27","2013-03-27");
			//EventsAPI events=new EventsAPI("categorie","2","2013-03-27","2013-03-30");
			//EventsAPI events=new EventsAPI("groupe","7","2013-03-27","2013-03-30");
			//EventsAPI events=new EventsAPI("souscategorie","7","2013-03-27","2013-03-30");
			//EventsAPI events=new EventsAPI("serie","cfd4b81e5657376b6",null,null);

			// aujourd'hui a dans une semaine
			Time now = new Time(Time.getCurrentTimezone());
			now.setToNow();
			//now.allDay=true;
			//now.normalize(true);
			Log.d("events","today is "+now.format("%y-%m-%d"));
			now.set(now.toMillis(true)+6*24*3600*1000);
			Log.d("events","next week is "+now.format("%Y-%m-%d"));
			
			EventsAPI e;
		
			// le 2013-03-27 -> contient un super gros description <img base64 >
			e=new EventsAPI("evenements",null,"2013-03-27","2013-04-02");
			return e;
		}

		@Override
		protected void onPostExecute(EventsAPI result) {
			setProgressBarIndeterminateVisibility(false);
			//Toast.makeText(ActivityDebugEvents.this, "done", Toast.LENGTH_SHORT).show();
			
			//Log.d("event","week load is "+result.time/1000.0+" sec");
			Toast.makeText(ActivityDebugEvents.this,"Load time is "+result.time/1000.0+" sec",Toast.LENGTH_LONG).show();

			//
			// affiche!
			//
			String[] from= {"titre","date","description","type_horaire"};
			int[] to= { R.id.text1,R.id.text2,R.id.text3,R.id.text4};
			listv.setAdapter( new SimpleAdapter(ActivityDebugEvents.this, result.hmList,
					R.layout.events_row,from,to) );
		}
	}


}
