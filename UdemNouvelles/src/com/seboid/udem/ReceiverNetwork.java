package com.seboid.udem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ReceiverNetwork extends BroadcastReceiver {
	@Override
	public void onReceive(Context ctx, Intent in) {
		boolean netDown = in.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false);
		String info = in.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO);
		
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo ni = cm.getActiveNetworkInfo();
	    String type;
	    if( ni!=null ) type=ni.getTypeName();
	    else type="(no network)";
	    //type will be WIFI or mobile, typically.
		if( netDown ) {
			Toast.makeText(ctx, "Network Down ("+type+")", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(ctx, "Network Up ("+type+")", Toast.LENGTH_LONG).show();
		}
	}
}
