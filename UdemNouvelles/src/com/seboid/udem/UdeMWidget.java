package com.seboid.udem;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UdeMWidget extends AppWidgetProvider {

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.d("rsswidget","Deleted!");
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d("rsswidget","Disabled!");
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d("rsswidget","Enabled!");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d("rsswidget","Receive!");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.d("rsswidget","Update!");
	}	

}
