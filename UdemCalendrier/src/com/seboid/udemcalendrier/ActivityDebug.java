package com.seboid.udemcalendrier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ActivityDebug extends Activity implements OnClickListener {

	Button eventsB;
	Button majB;
	Button resetB;
	Button catB;
	Button souscatB;
	Button groupeB;
	Button lieuxB;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		
		eventsB=(Button)findViewById(R.id.buttonEvents);
		eventsB.setOnClickListener(this);
		
		majB=(Button)findViewById(R.id.buttonMAJ);
		majB.setOnClickListener(this);
		
		resetB=(Button)findViewById(R.id.buttonReset);
		resetB.setOnClickListener(this);

		catB=(Button)findViewById(R.id.buttonCat);
		catB.setOnClickListener(this);
		
		souscatB=(Button)findViewById(R.id.buttonSCat);
		souscatB.setOnClickListener(this);

		groupeB=(Button)findViewById(R.id.buttonGroupes);
		groupeB.setOnClickListener(this);

		lieuxB=(Button)findViewById(R.id.buttonLieux);
		lieuxB.setOnClickListener(this);


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		
		Intent in;
		switch( v.getId() ) {
		case R.id.buttonMAJ:
			Toast.makeText(ActivityDebug.this, "service is "+ServiceMiseAJour.class.getName(),Toast.LENGTH_LONG).show();
			in = new Intent(ActivityDebug.this,ServiceMiseAJour.class);
			ActivityDebug.this.startService(in);
			break;
		case R.id.buttonEvents:
//			in = new Intent(this,ActivityDebugEvents.class);
			in = new Intent(this,ActivityDebugEventsSwipe.class);
			startActivity(in);
			break;
		case R.id.buttonReset:
			DBHelper dbh=new DBHelper(this);
			dbh.resetDB();
			break;
		case R.id.buttonCat:
			in = new Intent(this,ActivityDebugEvents.class);
			{
			String[] from= {"_id","desc"};
			int[] to= { R.id.text_t2,R.id.text_t1 };

			in.putExtra("from", from);
			in.putExtra("to",to);
			in.putExtra("query","select _id,desc from "+DBHelper.TABLE_C+" order by _id asc");
			in.putExtra("title","Categories");
			in.putExtra("layout", R.layout.categories_row);
			in.putExtra("type",1); // categories
			startActivity(in);
			}
			break;
		case R.id.buttonSCat:
			in = new Intent(this,ActivityDebugEvents.class);
			{
			String[] from= {"_id","desc"};
			int[] to= { R.id.text_t2,R.id.text_t1 };

			in.putExtra("from", from);
			in.putExtra("to",to);
			in.putExtra("query","select _id,desc from "+DBHelper.TABLE_SC+" order by _id asc");
			in.putExtra("title","Sous-Categories");
			in.putExtra("layout", R.layout.categories_row);
			in.putExtra("type",2); // sous-categories
			startActivity(in);
			}
			break;
		case R.id.buttonGroupes:
			in = new Intent(this,ActivityDebugEvents.class);
			{
			String[] from= {"_id","desc"};
			int[] to= { R.id.text_t2,R.id.text_t1 };

			in.putExtra("from", from);
			in.putExtra("to",to);
			in.putExtra("query","select _id,desc from "+DBHelper.TABLE_G+" order by desc asc");
			in.putExtra("title","Groupes");
			in.putExtra("layout", R.layout.categories_row);
			in.putExtra("type",3); // groupes
			startActivity(in);
			}
			break;
		case R.id.buttonLieux:
			in = new Intent(this,ActivityDebugEvents.class);
			{
			String[] from= {"_id","desc"};
			int[] to= { R.id.text_t2,R.id.text_t1 };

			in.putExtra("from", from);
			in.putExtra("to",to);
			in.putExtra("query","select _id,desc,latitude,longitude from "+DBHelper.TABLE_L+" order by _id asc");
			in.putExtra("title","Lieux");
			in.putExtra("layout", R.layout.categories_row);
			in.putExtra("type",/*4*/5); // lieux 4=show events, 5=show map
			startActivity(in);
			}
			break;

		}
	}

}
