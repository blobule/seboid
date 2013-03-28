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

	class DownloadEventsTask extends AsyncTask<Void,Void,EventsReaderAPI> {
		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected EventsReaderAPI doInBackground(Void... arg0) {
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
			now.set(now.toMillis(true)+7*24*3600*1000);
			Log.d("events","next week is "+now.format("%y-%m-%d"));

			
//			textViewDay.setText(today.monthDay);             // Day of the month (0-31)
//			textViewMonth.setText(today.month);              // Month (0-11)
//			textViewYear.setText(today.year);                // Year 
//			textViewTime.setText(today.format("%k:%M:%S"));  // Current time
			
			EventsReaderAPI e;
			long d=System.currentTimeMillis();
			// le 2013-03-27 -> contient un super gros description <img base64 >
			e=new EventsReaderAPI("evenements",null,"2013-03-27","2013-04-02");
			d-=System.currentTimeMillis();
			Log.d("event","week load is "+d/1000.0+" sec");

//			long total=0;
//			
//			d=System.currentTimeMillis();
//			e=new EventsAPI("evenements",null,"2013-03-27","2013-03-27");
//			d-=System.currentTimeMillis();total+=d;
//			Log.d("event","single day is "+d/1000.0+" sec");
//			
//			d=System.currentTimeMillis();
//			e=new EventsAPI("evenements",null,"2013-03-28","2013-03-28");
//			d-=System.currentTimeMillis();total+=d;
//			Log.d("event","single day is "+d/1000.0+" sec");
//
//			d=System.currentTimeMillis();
//			e=new EventsAPI("evenements",null,"2013-03-29","2013-03-29");
//			d-=System.currentTimeMillis();total+=d;
//			Log.d("event","single day is "+d/1000.0+" sec");
//
//			d=System.currentTimeMillis();
//			e=new EventsAPI("evenements",null,"2013-03-30","2013-03-30");
//			d-=System.currentTimeMillis();total+=d;
//			Log.d("event","single day is "+d/1000.0+" sec");
//
//			d=System.currentTimeMillis();
//			e=new EventsAPI("evenements",null,"2013-03-31","2013-03-31");
//			d-=System.currentTimeMillis();total+=d;
//			Log.d("event","single day is "+d/1000.0+" sec");
//
//			d=System.currentTimeMillis();
//			e=new EventsAPI("evenements",null,"2013-04-01","2013-04-01");
//			d-=System.currentTimeMillis();total+=d;
//			Log.d("event","single day is "+d/1000.0+" sec");
//
//			d=System.currentTimeMillis();
//			e=new EventsAPI("evenements",null,"2013-04-02","2013-04-02");
//			d-=System.currentTimeMillis();total+=d;
//			Log.d("event","single day is "+d/1000.0+" sec");
//
//			Log.d("event","week total is "+total/1000.0+" sec");
			
			return e;
		}

		@Override
		protected void onPostExecute(EventsReaderAPI result) {
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
